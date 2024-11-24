package com.apicatalog.vc.fnc;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.ecdsa.ECDSARdfc2019Suite;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.DataIntegrityProofDraft;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class IssueECDSARdfc2019P256 extends IssueFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final SignatureSuite SUITE = new ECDSARdfc2019Suite();

    static final URI VERIFICATION_METHOD = P256KeyPairProvider.getVerificationMethod();

    static final Issuer ISSUER = SUITE.createIssuer(P256KeyPairProvider.getKeyPair()).loader(LOADER);

    static final URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
            .setDatabaseId("iron-vc-demo")
            .build()
            .getService();

    public IssueECDSARdfc2019P256() {
        super(ISSUER, STORAGE, DB);
    }

    @Override
    protected ProofDraft getProofDraft(IssuanceRequest issuanceRequest) throws HttpFunctionError {

        // proof draft
        DataIntegrityProofDraft draft = issuer.createDraft(VERIFICATION_METHOD);

        draft.purpose(ASSERTION_PURPOSE);
        draft.created(Instant.now());
        draft.expires(draft.created().plus(21, ChronoUnit.DAYS));

        return draft;
    }
}
