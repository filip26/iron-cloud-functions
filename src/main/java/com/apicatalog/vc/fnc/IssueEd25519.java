package com.apicatalog.vc.fnc;

import java.util.Map.Entry;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class IssueEd25519 implements HttpFunction {

//    final static DocumentLoader LOADER = new Ed25519ContextLoader();
//
//    final static VerificationKeyProvider RESOLVERS = defaultResolvers(LOADER);
//
//    public final static Ed25519Signature2020 SUITE = new Ed25519Signature2020();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        response.setStatusCode(200);
        response.setContentType("text/plain");

        try (var writer = response.getWriter()) {
            
            var key = System.getenv("ED_PRIVATE_KEY_TEST_1");
            
            writer.write(key == null ? "null" : key);

            writer.write('\n');
            for (Entry<String, String> entry : System.getenv().entrySet()) {
                writer.write(entry.getKey() + " -> " + entry.getValue());
            }

        }
        // TODO Auto-generated method stub

        // proof draft
//        final Ed25519Signature2020ProofDraft draft = Ed25519Signature2020.createDraft(
//                testCase.verificationMethod,
//                URI.create("https://w3id.org/security#assertionMethod"));
//
//        draft.created(testCase.created);
//        draft.domain(testCase.domain);
//        draft.challenge(testCase.challenge);
//        draft.nonce(testCase.nonce);
//
//        final JsonObject issued = SUITE.createIssuer(getKeys(keyPairLocation, LOADER))
//                .loader(LOADER)
//                .sign(testCase.input, draft);
//

//        response.setStatusCode(201); // created
//        response.putHeader("content-type", "application/ld+json");
//        response.end(signed.toString());

    }

//    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {
//
//        return MethodSelector.create()
//                .with(MethodPredicate.methodId(
//                        // accept only https Ed25519VerificationKey2020
//                        m -> m.getScheme().equalsIgnoreCase("https")),
//                        new Ed25519VerificationKey2020Provider(loader))
//
//                // accept did:key
//                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
//                        ControllableKeyProvider.of(new DidKeyResolver(MulticodecDecoder.getInstance(KeyCodec.ED25519_PUBLIC_KEY, KeyCodec.ED25519_PRIVATE_KEY))))
//
//                .build();
//    }

}
