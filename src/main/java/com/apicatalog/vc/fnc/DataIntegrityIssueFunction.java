package com.apicatalog.vc.fnc;

import java.net.URI;

import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vcdi.DataIntegrityProofDraft;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Storage;

public abstract class DataIntegrityIssueFunction extends IssueFunction {

    protected final URI method;

    protected DataIntegrityIssueFunction(Issuer issuer, Storage storage, Firestore db, final URI method) {
        super(issuer, storage, db);
        this.method = method;
    }

    @Override
    protected ProofDraft getProofDraft(IssuanceRequest issuanceRequest) throws HttpFunctionError {

        // proof draft
        DataIntegrityProofDraft draft = issuer.createDraft(method);

        draft.purpose(issuanceRequest.purpose());
        draft.created(issuanceRequest.created());
        draft.expires(issuanceRequest.expires());
        draft.challenge(issuanceRequest.challenge());
        draft.domain(issuanceRequest.domain());
        draft.nonce(issuanceRequest.nonce());

        return draft;
    }
}
