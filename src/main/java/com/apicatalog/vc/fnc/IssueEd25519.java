package com.apicatalog.vc.fnc;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.ed25519.Ed25519ContextLoader;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.vc.issuer.Issuer;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.json.JsonObject;

public class IssueEd25519 extends IssueFunction implements HttpFunction {

    static final DocumentLoader LOADER = new Ed25519ContextLoader();

    static final Ed25519Signature2020 SUITE = new Ed25519Signature2020();

    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();

    static final URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);

    static final Storage STORAGE = StorageOptions.newBuilder().build().getService();

    public IssueEd25519() {
        super(STORAGE);
    }

    @Override
    protected JsonObject process(IssuanceRequest issuanceRequest) throws HttpFunctionError {
        // proof draft
        var draft = SUITE.createDraft(VERIFICATION_METHOD, ASSERTION_PURPOSE);

        draft.created(Instant.now());
        draft.expires(draft.created().plus(21, ChronoUnit.DAYS));

        try {

            return ISSUER.sign(issuanceRequest.credential(), draft);

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (SigningError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.getCode().name()));
        }
    }
}
