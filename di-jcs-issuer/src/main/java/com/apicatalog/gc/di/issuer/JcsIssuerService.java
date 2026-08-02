package com.apicatalog.gc.di.issuer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import com.apicatalog.crypto.gc.kms.KmsAsymmetricSigner;
import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.di.suite.StandardCryptoSuite;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.MapAdapter;
import com.apicatalog.trust.lexical.MapProofCursor;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.http.HttpMethods;
import com.google.cloud.ServiceOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;

public class JcsIssuerService implements HttpFunction {

    private static final Logger LOG = Logger.getLogger(JcsIssuerService.class.getName());

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

    private static final StandardCryptoSuite CRYPTOSUITE;
    private static final String SIGNATURE_ALGORITHM;
    private static final AsymmetricSigner SIGNER;
    private static final LexicalModel LEXICAL_MODEL;

    static {
        var location = System.getenv("KMS_LOCATION");
        var keyRing = System.getenv("KMS_KEY_RING");
        var keyId = System.getenv("KMS_KEY_ID");

        var version = System.getenv().getOrDefault("KMS_KEY_VERSION", "1");

        VERIFICATION_METHOD = System.getenv("VERIFICATION_METHOD");

        if (location == null || keyRing == null || keyId == null || VERIFICATION_METHOD == null) {
            throw new IllegalStateException("Incomplete environment configuration");
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

        var modelBuilder = DataIntegrity.createLexicalModel(Model.C14N_JCS)
                .proofProperty(DataIntegrity.VOCAB_PROOF_KEY)
                .c14n(Jcs::canonize)
                .adapter(MapAdapter::newInstance)
                .cursor(MapProofCursor::newInstance);

        int keyLength = -1;

        switch (publicKey.getAlgorithm()) {
        case EC_SIGN_P256_SHA256:
            SIGNATURE_ALGORITHM = ECDSA2019.P256;
            SIGNER = KmsAsymmetricSigner.newP256Instance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = ECDSA2019.withJCS();
            keyLength = ECDSA2019.P256_PUBLIC_KEY_SIZE;
            break;

        case EC_SIGN_P384_SHA384:
            SIGNATURE_ALGORITHM = ECDSA2019.P384;
            SIGNER = KmsAsymmetricSigner.newP384Instance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = ECDSA2019.withJCS();
            keyLength = ECDSA2019.P384_PUBLIC_KEY_SIZE;
            break;

        case EC_SIGN_ED25519:
            SIGNATURE_ALGORITHM = EdDSA2022.ALGORITHM;
            SIGNER = KmsAsymmetricSigner.newEd25519Instance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = EdDSA2022.withJCS();
            keyLength = EdDSA2022.PUBLIC_KEY_SIZE;
            break;

        case PQ_SIGN_ML_DSA_44:
            SIGNATURE_ALGORITHM = MLDSA2024.ALGORITHM_44;
            SIGNER = KmsAsymmetricSigner.newDSAInstance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = MLDSA2024.get44withJCS();
            keyLength = MLDSA2024.PUBLIC_KEY_SIZE;
            break;

        case PQ_SIGN_SLH_DSA_SHA2_128S:
            SIGNATURE_ALGORITHM = SLHDSA2024.ALGORITHM_SHA2_128s;
            SIGNER = KmsAsymmetricSigner.newDSAInstance(KMS_RESOURCE, KMS)::sign;
            CRYPTOSUITE = SLHDSA2024.get128withJCS();
            keyLength = SLHDSA2024.SHA2_128S_PUBLIC_KEY_SIZE;
            break;

        default:
            throw new IllegalArgumentException("Unsupported key algorithm: " + publicKey.getAlgorithm());
        }

        LEXICAL_MODEL = modelBuilder.proof(CRYPTOSUITE).build();

        LOG.info("Initialized for %s with %s (%d bytes)".formatted(
                CRYPTOSUITE.id(),
                KMS_RESOURCE,
                keyLength));
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        response.appendHeader("Access-Control-Allow-Origin", "*");

        if ("OPTIONS".equals(request.getMethod())) {
            response.appendHeader("Access-Control-Allow-Methods", "POST");
            response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
            response.appendHeader("Access-Control-Max-Age", "3600");
            response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
            return;
        }

        if (!HttpMethods.POST.equalsIgnoreCase(request.getMethod())) {
            response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
            return;
        }

        // TODO validate and log content type
        IO.println(request.getContentType().orElse(null));
        Map<String, Object> document = null;
        IssueRequest issueRequest = null;

        try (var parser = Jackson2Parser.newParser(request.getInputStream(), JSON_FACTORY)) {

            document = Tree.read(parser);
            issueRequest = IssueRequest.from(document);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        IO.println(document);

        var documentContext = ContextAwareResolver.getContexts(document);

        var proofDraft = CRYPTOSUITE.createProofDraft();

        proofDraft.context(documentContext);

        if (issueRequest.options() != null) {
            issueRequest.options().init(issueRequest.context(), proofDraft);
        }

        proofDraft.verificationMethod(VERIFICATION_METHOD);

        var updater = LEXICAL_MODEL.createUpdater(issueRequest.document());

        var payload = updater.createPayload();

        payload.withProofs(proofDraft.previous());

        var sha256 = MessageDigest.getInstance("SHA-256");

        var proof = proofDraft.sign(
                SIGNATURE_ALGORITHM,
                SIGNER,
                _ -> sha256::digest,
                payload.digestible());

        updater.addProof(proofDraft.context(), DataIntegrityProof.compact(proof, true));

        var signed = updater.compacted();

//        //TODO
//        var p = (Map)signed.get("proof");
//        var x = LinkedHashMap.newLinkedHashMap(p.size() + 1);
//        
//        ((Map) p).put("@context", issueRequest.document().get("@context"));
//
        IO.println(signed);

        try (var writer = Jackson2Emitter.newEmitter(response.getOutputStream(), JSON_FACTORY)) {
            response.setStatusCode(HttpStatus.SC_OK);
            response.setContentType("application/json");
            Tree.write(signed, writer);

        } catch (Exception e) {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
