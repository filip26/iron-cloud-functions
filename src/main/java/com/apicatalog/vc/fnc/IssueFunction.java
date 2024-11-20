package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;

import jakarta.json.JsonObject;

public abstract class IssueFunction extends HttpJsonFunction implements HttpFunction {

    protected final Storage storage;

    public IssueFunction(Storage storage) {
        super("POST", HttpURLConnection.HTTP_CREATED);
        this.storage = storage;
    }

    @Override
    protected JsonObject process(final JsonObject json) throws HttpFunctionError {

        try {
            var issuanceRequest = IssuanceRequest.of(json);

            var signed = process(issuanceRequest);

            String name = UUID.randomUUID().toString();

            BlobStorage.createBlob(storage, "issued/vc/" + name, signed);

            return signed;
            
        } catch (IOException e) {
            throw new HttpFunctionError(e, e.getMessage());
        }
    }

    protected abstract JsonObject process(IssuanceRequest issuanceRequest) throws HttpFunctionError;

}
