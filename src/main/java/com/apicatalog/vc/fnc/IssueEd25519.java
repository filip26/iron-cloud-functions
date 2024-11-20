package com.apicatalog.vc.fnc;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.ed25519.Ed25519ContextLoader;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;
import com.apicatalog.vc.issuer.Issuer;
import com.google.cloud.functions.HttpFunction;

import jakarta.json.JsonObject;

public class IssueEd25519 extends HttpJsonFunction implements HttpFunction {

    final static DocumentLoader LOADER = new Ed25519ContextLoader();

    final static Ed25519Signature2020 SUITE = new Ed25519Signature2020();

    final static KeyPair KEY_PAIR = getKeyPair();

    final static URI VERIFICATION_METHOD = getVerificationMethod();

    final static URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    final static Issuer ISSUER = SUITE.createIssuer(KEY_PAIR).loader(LOADER);

    public IssueEd25519() {
        super("POST", HttpURLConnection.HTTP_CREATED);
    }

    @Override
    protected JsonObject process(final JsonObject json) throws HttpFunctionError {

        var issuanceRequest = IssuanceRequest.of(json);

        // proof draft
        var draft = Ed25519Signature2020.createDraft(VERIFICATION_METHOD, ASSERTION_PURPOSE);

        draft.created(Instant.now());
        
        if (issuanceRequest.credentialId() != null) {
            draft.id(issuanceRequest.credentialId());
        }

        try {

            return ISSUER.sign(issuanceRequest.credential(), draft);

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (SigningError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.getCode().name()));
        }
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
}
