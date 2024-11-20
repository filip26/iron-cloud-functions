package com.apicatalog.vc.fnc;

import java.io.StringReader;
import java.net.URI;
import java.time.Instant;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.ed25519.Ed25519ContextLoader;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020ProofDraft;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020Provider;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.method.resolver.ControllableKeyProvider;
import com.apicatalog.vc.method.resolver.MethodPredicate;
import com.apicatalog.vc.method.resolver.MethodSelector;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

public class IssueEd25519 implements HttpFunction {

    final static DocumentLoader LOADER = new Ed25519ContextLoader();

    final static Ed25519Signature2020 SUITE = new Ed25519Signature2020();

    final static KeyPair KEY_PAIR = getKeyPair();

    final static URI VERIFICATION_METHOD = getVerificationMethod();

    final static URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    final static Issuer ISSUER = SUITE.createIssuer(KEY_PAIR).loader(LOADER);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try (var writer = response.getWriter()) {

            // proof draft
            final Ed25519Signature2020ProofDraft draft = Ed25519Signature2020.createDraft(
                    VERIFICATION_METHOD, ASSERTION_PURPOSE);

            draft.created(Instant.now());
            
            JsonObject data = null;

            try (JsonParser parser = Json.createParser(new StringReader(TEST))) {
                parser.next();
                data = parser.getObject();

                JsonObject signed = ISSUER.sign(data, draft);

                response.setStatusCode(201);
                response.setContentType("application/ld+json");
                writer.write(signed.toString());

            } catch (SigningError | DocumentError e) {
                response.setStatusCode(400);
                response.setContentType("text/plain");
                writer.write(e.getMessage());
                writer.write('\n');
                
            } catch (Exception e) {
                response.setStatusCode(500);
                response.setContentType("text/plain");
                writer.write(e.getMessage());
                writer.write('\n');

            }

        }

        // TODO Auto-generated method stub

//        draft.domain(testCase.domain);
//        draft.challenge(testCase.challenge);
//        draft.nonce(testCase.nonce);
//

//                .sign(testCase.input, draft);
//

//        response.setStatusCode(201); // created
//        response.putHeader("content-type", "application/ld+json");
//        response.end(signed.toString());

    }

    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {

        return MethodSelector.create()
                .with(MethodPredicate.methodId(
                        // accept only https Ed25519VerificationKey2020
                        m -> m.getScheme().equalsIgnoreCase("https")),
                        new Ed25519VerificationKey2020Provider(loader))

                // accept did:key
                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
                        ControllableKeyProvider.of(new DidKeyResolver(MulticodecDecoder.getInstance(KeyCodec.ED25519_PUBLIC_KEY, KeyCodec.ED25519_PRIVATE_KEY))))

                .build();
    }

    static final KeyPair getKeyPair() {

        var publicKey = "z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7";
        var privateKey = System.getenv("ED_PRIVATE_KEY_TEST_1");

        var id = URI.create("did:key:" + publicKey + "#" + publicKey);

        return GenericMultikey.of(
                id,
                URI.create("did:key:" + publicKey),
                new GenericMulticodecKey(
                        KeyCodec.ED25519_PUBLIC_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(publicKey))),
                new GenericMulticodecKey(
                        KeyCodec.ED25519_PRIVATE_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(privateKey))));
    }

    final static URI getVerificationMethod() {

        var publicKey = "z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7";

        return URI.create("did:key:" + publicKey + "#" + publicKey);
    }

    static String TEST = "{\n"
            + "  \"@context\": \"https://www.w3.org/ns/credentials/v2\",\n"
            + "  \"id\": \"https://apicatalog/com/vc/test-credentials#0001\",\n"
            + "  \"type\": \"VerifiableCredential\",\n"
            + "  \"issuer\": \"https://github.com/filip26/iron-verifiable-credentials/issuer/1\",\n"
            + "  \"credentialSubject\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\"\n"
            + "}";

}
