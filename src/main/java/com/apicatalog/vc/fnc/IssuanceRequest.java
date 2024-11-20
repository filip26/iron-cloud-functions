package com.apicatalog.vc.fnc;

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
        String credentialId
        ) {
    
    public static IssuanceRequest of(final JsonObject json) {
        
        JsonObject credential = json.getJsonObject("credential");
        
        Collection<String> mandatoryPointers = Collections.emptyList();
        String credentialId = null;
        
        
        JsonValue options = json.get("options");
        if (JsonUtils.isObject(options)) {
            JsonValue id = options.asJsonObject().get("credentialId");
            if (JsonUtils.isString(id)) {
                credentialId = ((JsonString)id).getString();
            }
        }
        
        return new IssuanceRequest(credential, mandatoryPointers, credentialId);
    }
    
    
}
