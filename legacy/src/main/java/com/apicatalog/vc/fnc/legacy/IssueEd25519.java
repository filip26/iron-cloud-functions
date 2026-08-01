package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class IssueEd25519 extends IssueFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new Ed25519ContextLoader();
//
//    static final Ed25519Signature2020 SUITE = new Ed25519Signature2020();
//
//    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();
//
//    static final URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");
//
//    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);
//
//    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
//
//    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
//            .setDatabaseId("iron-vc-demo")
//            .build()
//            .getService();
//
//    public IssueEd25519() {
//        super(ISSUER, STORAGE, DB);
//    }
//
//    @Override
//    protected ProofDraft getProofDraft(IssueRequest issuanceRequest) throws HttpFunctionError {
//        // proof draft
//        Ed25519Signature2020ProofDraft draft = issuer.createDraft(VERIFICATION_METHOD);
//
//        draft.purpose(issuanceRequest.purpose());
//        draft.created(issuanceRequest.created());
//        draft.expires(issuanceRequest.expires());
//        draft.challenge(issuanceRequest.challenge());
//        draft.domain(issuanceRequest.domain());
//        draft.nonce(issuanceRequest.nonce());
//
//        return draft;
//    }
}
