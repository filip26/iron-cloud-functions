package com.apicatalog.vc.fnc;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.eddsa.EdDSARdfc2022Suite;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class IssueEdDSARdfc2022 extends IssueFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final EdDSARdfc2022Suite SUITE = new EdDSARdfc2022Suite();

    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();

    static final URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);

    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
            .setDatabaseId("iron-vc-demo")
            .setProjectId("api-catalog")
            .build()
            .getService();
    
    public IssueEdDSARdfc2022() {
        super(ISSUER, STORAGE, DB);
    }

    @Override
    protected ProofDraft getProofDraft(IssuanceRequest issuanceRequest) throws HttpFunctionError {

        // proof draft
        var draft = SUITE.createDraft(VERIFICATION_METHOD, ASSERTION_PURPOSE);

        draft.created(Instant.now());
        draft.expires(draft.created().plus(21, ChronoUnit.DAYS));

        return draft;
    }
}
