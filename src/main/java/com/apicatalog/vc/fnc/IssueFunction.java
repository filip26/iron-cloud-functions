package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.model.DocumentError;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

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
            var request = IssuanceRequest.of(json);

            String uuid = UUID.randomUUID().toString();

            var draft = getProofDraft(request);

            var document = request.document();

            // TODO better
            if (request.credential() 
                    && !document.containsKey("proof") 
                    && !document.containsKey("credentialStatus")) {
                var status = getStatus();

                if (status != null) {
                    // TODO decouple from vc model
                    document = Json.createObjectBuilder(document).add("credentialStatus", status).build();
                }
            }

            signed = issuer.sign(document, draft);

            var log = write("gs://" + BlobStorage.BUCKET_NAME + "/issued/" + uuid, issuer, draft, JsonLdContext.strings(signed));

            BlobStorage.createBlob(storage, "issued/" + uuid, signed);

            log.get();

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (CryptoSuiteError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));

        } catch (IOException e) {
            throw new HttpFunctionError(e, e.getMessage());

        } catch (InterruptedException | ExecutionException e) {
            /* ignore */
        }

        return signed;
    }

    protected abstract ProofDraft getProofDraft(IssuanceRequest issuanceRequest) throws HttpFunctionError;

    protected JsonStructure getStatus() throws HttpFunctionError {
        return null;
    }

    protected ApiFuture<DocumentReference> write(String gsid, Issuer issuer, ProofDraft draft, Collection<String> context) {

        Map<String, Object> data = new HashMap<>();

        data.put("cryptosuite", issuer.cryptosuite().name());
        data.put("keyLength", issuer.cryptosuite().keyLength());

        data.put("draft", toProofDraftData(draft));

        data.put("context", context);
        data.put("source", gsid);

        return db.collection("issued").add(data);
    }

    protected static Map<String, Object> toProofDraftData(ProofDraft draft) {

        Map<String, Object> data = new HashMap<>(6);

        if (draft.type() != null) {
            data.put("type", draft.type());
        }
        if (draft.id() != null) {
            data.put("id", draft.id().toString());
        }
        if (draft.purpose() != null) {
            data.put("purpose", draft.purpose().toString());
        }
        if (draft.method() != null && draft.method().id() != null) {
            data.put("method", draft.method().id().toString());
        }
        if (draft.created() != null) {
            data.put("created", draft.created());
        }
        if (draft.expires() != null) {
            data.put("expires", draft.expires());
        }
        return data;
    }

}
