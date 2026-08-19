package com.apicatalog.gc.di.issuer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import com.apicatalog.crypto.gc.kms.KmsAsymmetricSigner;
import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.di.suite.StandardCryptoSuite;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.semantic.GraphAccessor;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.GraphUpdater;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpStatusCodes;
import com.google.cloud.ServiceOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;

public class RDFCIssuerService implements HttpFunction {

    @FunctionalInterface
    private static interface Issuer {
        Map<String, ?> issue(
                Map<String, Object> document,
                IssueRequest issueRequest,
                Digestor.Factory digestFactory) throws SignatureException;
    }

    private static final Logger LOG = Logger.getLogger(RDFCIssuerService.class.getName());

    /**
     * Reusable KMS client to minimize latency during "warm" starts. Initialized
     * once per container instance.
     */
    private static final KeyManagementServiceClient KMS;

    // Static initialization
    private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();

    // Environment variables
    private static final String VERIFICATION_METHOD;

    // Static configuration detected at startup
    private static final String KMS_RESOURCE;

    private static final Issuer ISSUER;
    private static final StandardCryptoSuite CRYPTOSUITE;

    private static final String DIGEST_NAME;
    private static final String SIGNATURE_ALGORITHM;
    private static final AsymmetricSigner SIGNER;
    private static final SemanticModel MODEL;

    static {
        var env = System.getenv();

        var proofType = env.get("PROOF_TYPE");

        var location = env.get("KMS_LOCATION");
        var keyRing = env.get("KMS_KEY_RING");
        var keyId = env.get("KMS_KEY_ID");

        var version = env.getOrDefault("KMS_KEY_VERSION", "1");

        VERIFICATION_METHOD = env.get("VERIFICATION_METHOD");

        if (proofType == null
                || location == null
                || keyRing == null
                || keyId == null
                || VERIFICATION_METHOD == null) {
            throw new IllegalStateException(
                    """
                    Missing environment configuration:
                    PROOF_TYPE: %s
                    KMS_LOCATION: %s
                    KMS_KEY_RING: %s
                    KMS_KEY_ID: %s
                    VERIFICATION_METHOD: %s
                    """.formatted(proofType, location, keyRing, keyId, VERIFICATION_METHOD));
        }

        var project = ServiceOptions.getDefaultProjectId();
        KMS_RESOURCE = CryptoKeyVersionName.format(
                project,
                location,
                keyRing,
                keyId,
                version);

        try {
            KMS = KeyManagementServiceClient.create();

            // Ensure client is closed when the JVM shuts down
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (KMS != null) {
                    KMS.close();
                }
            }));

        } catch (IOException e) {
            throw new IllegalStateException("KMS initialization failed", e);
        }

        // IAM Validation: Verify KMS
        var kmsPermissions = KMS.testIamPermissions(KMS_RESOURCE,
                List.of("cloudkms.cryptoKeyVersions.viewPublicKey",
                        "cloudkms.cryptoKeyVersions.useToSign"));
        if (kmsPermissions.getPermissionsList().size() < 2) {
            throw new IllegalStateException("Missing KMS permissions: " + kmsPermissions);
        }

        // Get key algorithm from a public key, cloudkms.publicKeyViewer is least
        // permissive
        final var publicKey = KMS.getPublicKey(KMS_RESOURCE);

        var modelBuilder = DataIntegrity.newSematicModel(Model.C14N_RDFC)
                .proofPredicate(DataIntegrity.PREDICATE_PROOF)
                .expand(RDFCIssuerService::expand)
                .tordf(RDFCIssuerService::toRDF)
                .c14n(RDFCIssuerService::createRDFC)
                .accessor(GraphAccessor::newInstance)
                .updater(GraphUpdater::new)
                .cursor(GraphProofCursor::newInstance)
                .payload(GraphPayloadGenerator::new);

        int keyLength = -1;

        switch (publicKey.getAlgorithm()) {
        case EC_SIGN_P256_SHA256:
            if (!DataIntegrityProof.TYPE_NAME.equals(proofType)) {
                throw new IllegalStateException(
                        """
                        Unsupported PROOF_TYPE: %s for key algorithm: %s.
                        """.formatted(proofType, publicKey.getAlgorithm()));
            }
            ISSUER = RDFCIssuerService::issueDataIntegrityProof;
            DIGEST_NAME = Digestor.SHA_256;
            SIGNATURE_ALGORITHM = ECDSA2019.P256;
            SIGNER = KmsAsymmetricSigner.newP256Instance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = ECDSA2019.withRDFC();
            keyLength = ECDSA2019.P256_PUBLIC_KEY_SIZE;
            MODEL = modelBuilder.cryptosuite(CRYPTOSUITE).build();
            break;

        case EC_SIGN_P384_SHA384:
            if (!DataIntegrityProof.TYPE_NAME.equals(proofType)) {
                throw new IllegalStateException(
                        """
                        Unsupported PROOF_TYPE: %s for key algorithm: %s.
                        """.formatted(proofType, publicKey.getAlgorithm()));
            }
            ISSUER = RDFCIssuerService::issueDataIntegrityProof;
            DIGEST_NAME = Digestor.SHA_384;
            SIGNATURE_ALGORITHM = ECDSA2019.P384;
            SIGNER = KmsAsymmetricSigner.newP384Instance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = ECDSA2019.withRDFC();
            keyLength = ECDSA2019.P384_PUBLIC_KEY_SIZE;
            MODEL = modelBuilder.cryptosuite(CRYPTOSUITE).build();
            break;

        case EC_SIGN_ED25519:
            if (Ed25519Signature2020.TYPE_NAME.equals(proofType)) {
                ISSUER = RDFCIssuerService::issueEd25519Signature2020;
                SIGNATURE_ALGORITHM = Ed25519Signature2020.SIGNATURE_ALGORITHM;
                DIGEST_NAME = Ed25519Signature2020.HASH_ALGORITHM;
                CRYPTOSUITE = null;
                keyLength = Ed25519Signature2020.PUBLIC_KEY_SIZE;
                MODEL = modelBuilder.Ed25519Signature2020().build();

            } else if (DataIntegrityProof.TYPE_NAME.equals(proofType)) {
                ISSUER = RDFCIssuerService::issueDataIntegrityProof;
                SIGNATURE_ALGORITHM = EdDSA2022.ALGORITHM;
                CRYPTOSUITE = EdDSA2022.withRDFC();
                DIGEST_NAME = Digestor.SHA_256;
                keyLength = EdDSA2022.PUBLIC_KEY_SIZE;
                MODEL = modelBuilder.cryptosuite(CRYPTOSUITE).build();

            } else {
                throw new IllegalStateException(
                        """
                        Unsupported PROOF_TYPE: %s. Expected %s or %s.
                        """.formatted(proofType, DataIntegrityProof.TYPE_NAME, Ed25519Signature2020.TYPE_NAME));
            }

            SIGNER = KmsAsymmetricSigner.newEd25519Instance(KMS_RESOURCE, KMS)::sign;
            break;

        case PQ_SIGN_ML_DSA_44:
            if (!DataIntegrityProof.TYPE_NAME.equals(proofType)) {
                throw new IllegalStateException(
                        """
                        Unsupported PROOF_TYPE: %s for key algorithm: %s.
                        """.formatted(proofType, publicKey.getAlgorithm()));
            }
            ISSUER = RDFCIssuerService::issueDataIntegrityProof;
            DIGEST_NAME = Digestor.SHA_256;
            SIGNATURE_ALGORITHM = MLDSA2024.ALGORITHM_44;
            SIGNER = KmsAsymmetricSigner.newDSAInstance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = MLDSA2024.get44withRDFC();
            keyLength = MLDSA2024.PUBLIC_KEY_SIZE;
            MODEL = modelBuilder.cryptosuite(CRYPTOSUITE).build();
            break;

        case PQ_SIGN_SLH_DSA_SHA2_128S:
            if (!DataIntegrityProof.TYPE_NAME.equals(proofType)) {
                throw new IllegalStateException(
                        """
                        Unsupported PROOF_TYPE: %s for key algorithm: %s.
                        """.formatted(proofType, publicKey.getAlgorithm()));
            }
            ISSUER = RDFCIssuerService::issueDataIntegrityProof;
            DIGEST_NAME = Digestor.SHA_256;
            SIGNATURE_ALGORITHM = SLHDSA2024.ALGORITHM_SHA2_128s;
            SIGNER = KmsAsymmetricSigner.newDSAInstance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = SLHDSA2024.get128withRDFC();
            keyLength = SLHDSA2024.SHA2_128S_PUBLIC_KEY_SIZE;
            MODEL = modelBuilder.cryptosuite(CRYPTOSUITE).build();
            break;

        default:
            throw new IllegalArgumentException("Unsupported key algorithm: " + publicKey.getAlgorithm());
        }

        if (CRYPTOSUITE != null) {
            LOG.info("Initialized for %s (%s) with %s (%d bytes)".formatted(
                    proofType,
                    CRYPTOSUITE.id(),
                    KMS_RESOURCE,
                    keyLength));
        } else {
            LOG.info("Initialized for %s with %s (%d bytes)".formatted(
                    proofType,
                    KMS_RESOURCE,
                    keyLength));
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        response.appendHeader("Access-Control-Allow-Origin", "*");

        if (HttpMethods.OPTIONS.equals(request.getMethod())) {
            response.appendHeader("Access-Control-Allow-Methods", "POST");
            response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
            response.appendHeader("Access-Control-Max-Age", "3600");
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_NO_CONTENT);
            return;
        }

        if (!HttpMethods.POST.equalsIgnoreCase(request.getMethod())) {
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_METHOD_NOT_ALLOWED);
            return;
        }

        // TODO validate and log content type
        IO.println(request.getContentType().orElse(null));
        Map<String, Object> document = null;
        IssueRequest issueRequest = null;

        try (var parser = Jackson2Parser.newParser(request.getInputStream(), JSON_FACTORY)) {

            document = Tree.read(parser);
            issueRequest = IssueRequest.from(document);

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_BAD_REQUEST);
        }

        Map<String, ?> signed = null;

        try {
            var sha = MessageDigest.getInstance(DIGEST_NAME);

            signed = ISSUER.issue(document, issueRequest, _ -> sha::digest);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_BAD_REQUEST);

        } catch (Throwable e) {
            LOG.log(Level.SEVERE, e, e::getMessage);
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
        }

//        //TODO
//        var p = (Map)signed.get("proof");
//        var x = LinkedHashMap.newLinkedHashMap(p.size() + 1);
//        
//        ((Map) p).put("@context", issueRequest.document().get("@context"));
//
        IO.println(signed);

        try (var writer = Jackson2Emitter.newEmitter(response.getOutputStream(), JSON_FACTORY)) {
            response.setStatusCode(HttpStatus.SC_OK);
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_OK);
            response.setContentType("application/json");
            Tree.write(signed, writer);

        } catch (Throwable e) {
            LOG.log(Level.SEVERE, e, e::getMessage);
            response.setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
        }
    }

    private static final Map<String, ?> issueDataIntegrityProof(
            Map<String, Object> document,
            IssueRequest issueRequest,
            Digestor.Factory digestFactory) throws SignatureException {
        IO.println(document);

        var documentContext = ContextAwareResolver.getContexts(document);

        var proofDraft = CRYPTOSUITE.newProofDraft();

        proofDraft.context(documentContext);

        if (issueRequest.options() != null) {
            proofDraft.options(issueRequest.options().proofDraft());
        }

        if (proofDraft.purpose() == null) {
            proofDraft.purpose(Proof.Purpose.ASSERTION);
        }

        if (proofDraft.created() == null) {
            proofDraft.created(Instant.now());
        }

        proofDraft.verificationMethod(VERIFICATION_METHOD);

        if (proofDraft.hasRequired()) {
            throw new IllegalArgumentException("Proof draft is missing required properties.");
        }

        var updater = MODEL.createUpdater(issueRequest.document());

        var payload = updater.createPayload();

        payload.withProofs(proofDraft.previous());

        var proof = proofDraft.sign(
                SIGNATURE_ALGORITHM,
                SIGNER,
                digestFactory,
                payload.digestible());

        updater.addProof(proofDraft.context(), DataIntegrityProof.compact(proof, true));

        return updater.compacted();
    }

    private static final Map<String, ?> issueEd25519Signature2020(
            Map<String, Object> document,
            IssueRequest issueRequest,
            Digestor.Factory digestFactory) throws SignatureException {
        IO.println(document);

        var proofDraft = issueRequest.options() != null
                ? Ed25519Signature2020.newDraft(issueRequest.options().proofDraft())
                : Ed25519Signature2020.newDraft();

        var documentContext = ContextAwareResolver.getContexts(document);

        if (proofDraft.context() == null || proofDraft.context().isEmpty()) {
            proofDraft.context(documentContext);
        }

        proofDraft.verificationMethod(VERIFICATION_METHOD);

        if (proofDraft.purpose() == null) {
            proofDraft.purpose(Proof.Purpose.ASSERTION);
        }

        if (proofDraft.created() == null) {
            proofDraft.created(Instant.now());
        }

        if (proofDraft.hasRequired()) {
            throw new IllegalArgumentException("Proof draft is missing required properties.");
        }

        var updater = MODEL.createUpdater(document);

        var payload = updater.createPayload();

        var proof = Ed25519Signature2020.generateProof(
                SIGNER,
                digestFactory,
                proofDraft,
                payload.digestible());

        updater.addProof(proofDraft.context(), Ed25519Signature2020.compact(proof));
        return updater.compacted();
    }

    private static final void toRDF(Object document, final SemanticModel.QuadConsumer consumer) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var options = new JsonLdOptions();
            options.setUndefinedTermsPolicy(ProcessingPolicy.Fail);

            var toRdf = JsonLd.toRdf(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())))
                    .options(options);
//                    .loader(ContextLoader.getInstance())
            ;

            // TODO remove with rdf-api 2.0.0
            toRdf.provide(new RdfQuadConsumer() {

                @Override
                public RdfQuadConsumer quad(String subject, String predicate, String object, String datatype,
                        String language,
                        String direction, String graph) {

                    consumer.accept(subject, predicate, object, datatype, language, direction, graph);
                    return this;
                }
            });

        } catch (IOException | JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }

    private static final Collection<Object> expand(Map<String, Object> document) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var source = bos.toByteArray();
            IO.println("EXPAND " + new String(source));
            var options = new JsonLdOptions();
            options.setUndefinedTermsPolicy(ProcessingPolicy.Fail);

            var expanded = JsonLd.expand(JsonDocument.of(new ByteArrayInputStream(source)))
                    .options(options)
                    .undefinedTermsPolicy(ProcessingPolicy.Warn)
//                    .loader(ContextLoader.getInstance()).get();
                    .get();

            try (var parser = Jackson2Parser.newParser(new ByteArrayInputStream(expanded.toString().getBytes()),
                    JsonFactory.builder().build())) {
                return Tree.read(parser);
            }

        } catch (IOException | JsonLdError e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private static final RdfcPrcessor createRDFC() {
        return new RdfcPrcessor(); // TODO reuse one instance across
    }

    private static class RdfcPrcessor implements GraphCanonizer {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RdfCanon canon = RdfCanon.create("SHA-256");

        @Override
        public byte[] canonize() {

            bos.reset();

            canon.provide(line -> {
                try {
                    bos.write(line.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            return bos.toByteArray();
        }

        @Override
        public void accept(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph) {
            canon.quad(subject, predicate, object, datatype, language, direction, graph);
        }
    }
}
