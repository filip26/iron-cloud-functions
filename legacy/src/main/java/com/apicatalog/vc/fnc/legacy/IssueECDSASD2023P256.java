package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class IssueECDSASD2023P256 extends DataIntegrityIssueFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final SignatureSuite SUITE = new ECDSASD2023Suite();
//
//    static final URI VERIFICATION_METHOD = P256KeyPairProvider.getVerificationMethod();
//
//    static final Issuer ISSUER = SUITE.createIssuer(P256KeyPairProvider.getKeyPair()).loader(LOADER);
//
//    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
//
//    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
//            .setDatabaseId("iron-vc-demo")
//            .build()
//            .getService();
//
//    public IssueECDSASD2023P256() {
//        super(ISSUER, STORAGE, DB, VERIFICATION_METHOD);
//    }
//
//    @Override
//    protected ProofDraft getProofDraft(IssueRequest issuanceRequest) throws HttpFunctionError {
//        var draft = (ECDSASD2023Draft) super.getProofDraft(issuanceRequest);
//        draft.selectors(issuanceRequest.mandatoryPointers());
//        try {
//            draft.useGeneratedHmacKey(32);
//            draft.useGeneratedProofKeys();
//            return draft;
//        } catch (CryptoSuiteError e) {
//            throw new HttpFunctionError(e, "Internal");
//        }
//    }
}
