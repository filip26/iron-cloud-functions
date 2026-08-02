package com.apicatalog.vc.fnc.legacy;

public abstract class DataIntegrityIssueFunction extends IssueFunction {

//    protected final URI method;
//
//    protected DataIntegrityIssueFunction(Issuer issuer, Storage storage, Firestore db, final URI method) {
//        super(issuer, storage, db);
//        this.method = method;
//    }
//
//    @Override
//    protected ProofDraft getProofDraft(IssueRequest issuanceRequest) throws HttpFunctionError {
//
//        // proof draft
//        DataIntegrityDraft draft = issuer.createDraft(method);
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
