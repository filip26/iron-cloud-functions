package com.apicatalog.vc.fnc;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public record IssuanceRequest(
        JsonObject credential,
        // options
        Collection<String> mandatoryPointers,
        URI credentialId) {

    static final String OPTIONS = "options";
    static final String CREDENTIAL = "credential";
    public static final String OPTION_MANDATORY_POINTERS = "mandatoryPointers";
    public static final String OPTION_CREDENTIAL_ID = "credentialId";

    public static IssuanceRequest of(final JsonObject json) {

        JsonObject credential = json.getJsonObject(CREDENTIAL);

        Collection<String> mandatoryPointers = Collections.emptyList();
        String credentialId = null;

        JsonValue options = json.get(OPTIONS);
        if (JsonUtils.isObject(options)) {
            JsonValue id = options.asJsonObject().get(OPTION_CREDENTIAL_ID);
            if (JsonUtils.isString(id)) {
                credentialId = ((JsonString) id).getString();
            }
        }

        return new IssuanceRequest(credential, mandatoryPointers, URI.create(credentialId));
    }
}
