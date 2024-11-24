package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;

import jakarta.json.JsonObject;

public abstract class IssueFunction extends HttpJsonFunction implements HttpFunction {

    protected final Issuer issuer;
    protected final Storage storage;
    protected final Firestore db;

    public IssueFunction(Issuer issuer, Storage storage, Firestore db) {
        super("POST", HttpURLConnection.HTTP_CREATED);
        this.issuer = issuer;
        this.storage = storage;
        this.db = db;
    }

    @Override
    protected JsonObject process(final JsonObject json) throws HttpFunctionError {

        JsonObject signed = null;

        try {
            var issuanceRequest = IssuanceRequest.of(json);

            String uuid = UUID.randomUUID().toString();

            var draft = getProofDraft(issuanceRequest);

            signed = issuer.sign(issuanceRequest.credential(), draft);

            var log = write(uuid, issuer, JsonLdContext.strings(signed));

            BlobStorage.createBlob(storage, "issued/" + uuid, JSON.createObjectBuilder()
                    .add("suite", issuer.cyptosuite().id())
                    .add("signed", signed)
                    .build());

            log.get();

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (SigningError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.getCode().name()));

        } catch (IOException e) {
            throw new HttpFunctionError(e, e.getMessage());

        } catch (InterruptedException | ExecutionException e) {
            /* ignore */
        }

        return signed;
    }

    protected abstract ProofDraft getProofDraft(IssuanceRequest issuanceRequest) throws HttpFunctionError;

    protected ApiFuture<DocumentReference> write(String uuid, Issuer issuer, Collection<String> context) {
        CollectionReference docRef = db.collection("issued");
        Map<String, Object> data = new HashMap<>();
        data.put("cryptosuite", issuer.cyptosuite().id());
        data.put("keyLength", issuer.cyptosuite().keyLength());
        data.put("context", context);
        data.put("gsid", uuid);

        ApiFuture<DocumentReference> result = docRef.add(data);
        return result;
    }

}
