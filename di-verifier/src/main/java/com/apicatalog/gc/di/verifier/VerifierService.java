package com.apicatalog.gc.di.verifier;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.crypto.bc.BCEd25519Verifier;
import com.apicatalog.crypto.bc.BCMLDSAVerifier;
import com.apicatalog.crypto.bc.BCSLHDSAVerifier;
import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.did.resolver.MultiKeyResolver;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.MapAdapter;
import com.apicatalog.trust.lexical.MapProofCursor;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.proof.ProofVerifier;
import com.apicatalog.trust.semantic.GraphAdapter;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.GraphUpdater;
import com.apicatalog.trust.semantic.SemanticModel;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.http.HttpMethods;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class VerifierService implements HttpFunction {

    private static final Logger LOG = Logger.getLogger(VerifierService.class.getName());

    // Static initialization
    private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();

    private static final LexicalModel LEXICAL_MODEL = DataIntegrity.createLexicalModel(Model.C14N_JCS)
            .proofProperty(DataIntegrity.VOCAB_PROOF_KEY)
            .proof(EdDSA2022.withJCS())
            .proof(ECDSA2019.withJCS())
            .proof(MLDSA2024.get44withJCS())
            .proof(SLHDSA2024.get128withJCS())
            .c14n(Jcs::canonize)
            .adapter(MapAdapter::newInstance)
            .cursor(MapProofCursor::newInstance)
            .build();

    private static final SemanticModel SEMANTIC_MODEL = DataIntegrity.createSematicModel(Model.C14N_RDFC)
            .proofPredicate(DataIntegrity.VOCAB_PROOF_URI)
            .proof(EdDSA2022.withRDFC())
            .proof(ECDSA2019.withRDFC())
            .proof(MLDSA2024.get44withRDFC())
            .proof(SLHDSA2024.get128withRDFC())
            .Ed25519Signature2020()
//FIXME
//            .expand(Resources::expand)
//            .tordf(Resources::toRDF)
//            .c14n(Resources::createRDFC)
            .adapter(GraphAdapter::newInstance)
            .updater(GraphUpdater::new)
            .cursor(GraphProofCursor::newInstance)
            .payload(GraphPayloadGenerator::new)
            .build();

    private static final ContextAwareResolver MODEL_RESOLVER = ContextAwareResolver.newBuilder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty),
                    // in processing preferences order
                    LEXICAL_MODEL,
                    SEMANTIC_MODEL)
            .build();

    private static final DidKeyResolver DID_KEY_RESOLVER = DidKeyResolver.newBuilder()
            .multibaseDecoder(MultibaseDecoder.getInstance(
                    Multibase.BASE_58_BTC,
                    Multibase.BASE_64_URL)::decode)
            .multikey()
            .build();

    private static final MultiKeyResolver MULTIKEY_RESOLVER = MultiKeyResolver.newBuilder()
            .codec(EdDSA2022.ALGORITHM, KeyCodec.ED25519_PUBLIC.varint())
            .codec(ECDSA2019.P256, KeyCodec.P256_PUBLIC.varint())
            .codec(ECDSA2019.P384, KeyCodec.P384_PUBLIC.varint())
            .codec(MLDSA2024.ALGORITHM_44, KeyCodec.MLDSA_44_PUBLIC.varint())
            .codec(SLHDSA2024.ALGORITHM_SHA2_128s, KeyCodec.SLHDSA_SHA2_128S_PUBLIC.varint())
            .methodResolver(DidKey.METHOD_NAME, DID_KEY_RESOLVER)
            .documentResolver(DidKey.METHOD_NAME, DID_KEY_RESOLVER)
            .build();

    private static final ProofVerifier PROOF_VERIFIER = ProofVerifier.newBuilder()
            .publicKeyResolver(MULTIKEY_RESOLVER::getPublicKey)
            .verifier(EdDSA2022.ALGORITHM, BCEd25519Verifier.getInstance()::verify)
            .verifier(ECDSA2019.P256, BCECDSAVerifier.getP256Instance()::verify)
            .verifier(ECDSA2019.P384, BCECDSAVerifier.getP384Instance()::verify)
            .verifier(MLDSA2024.ALGORITHM_44, BCMLDSAVerifier.get44Instance()::verify)
            .verifier(SLHDSA2024.ALGORITHM_SHA2_128s, BCSLHDSAVerifier.get128sInstance()::verify)
            .digestFactory(sha -> data -> {
                try {
                    return MessageDigest.getInstance(sha).digest(data);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException(e);
                }
            })
            .build();

    // Static configuration detected at startup

    static {

//        LOG.info("Initialized for %s with %s (%d bytes)".formatted(
//                CRYPTOSUITE.id(),
//                KMS_RESOURCE,
//                keyLength));
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
        VerifyRequest issueRequest = null;

        try (var parser = Jackson2Parser.newParser(request.getInputStream(), JSON_FACTORY)) {

            document = Tree.read(parser);
            issueRequest = VerifyRequest.from(document);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        IO.println(document);

//        try (var writer = Jackson2Emitter.newEmitter(response.getOutputStream(), JSON_FACTORY)) {
//            response.setStatusCode(HttpStatus.SC_OK);
//            response.setContentType("application/json");
//            Tree.write(signed, writer);
//
//        } catch (Exception e) {
//            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//        }
    }

}
