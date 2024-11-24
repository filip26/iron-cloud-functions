package com.apicatalog.vc.fnc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public record VerifyRequest(
        JsonObject credential,
        // options
        Collection<String> mandatoryPointers,
        URI credentialId) {

    static final String OPTIONS = "options";
    
    static final String CREDENTIAL = "credential";

    static final String OPTION_MANDATORY_POINTERS = "mandatoryPointers";

    static final String OPTION_CREDENTIAL_ID = "credentialId";

    public static VerifyRequest of(final JsonObject json) {

        JsonObject credential = json.getJsonObject(CREDENTIAL);

        Collection<String> mandatoryPointers = Collections.emptyList();
        String credentialId = null;

        var options = json.get(OPTIONS);
        if (JsonUtils.isObject(options)) {

            // credential id
            var id = options.asJsonObject().get(OPTION_CREDENTIAL_ID);
            if (JsonUtils.isString(id)) {
                credentialId = ((JsonString) id).getString();
            }

            // mandatory pointers
            var pointers = options.asJsonObject().get(OPTION_MANDATORY_POINTERS);
            if (JsonUtils.isArray(pointers)) {
                mandatoryPointers = new ArrayList<>(pointers.asJsonArray().size());
                for (var pointer : pointers.asJsonArray()) {
                    if (JsonUtils.isNotString(pointer)) {
                        throw new IllegalArgumentException("An invalid mandatory pointer [" + pointer + "], exptected string value.");
                    }
                    mandatoryPointers.add(((JsonString) pointer).getString());
                }
            }
        }

        return new VerifyRequest(
                credential,
                mandatoryPointers,
                credentialId != null && !credentialId.isEmpty()
                        ? URI.create(credentialId)
                        : null);
    }
}
