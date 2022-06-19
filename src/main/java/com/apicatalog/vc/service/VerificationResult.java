package com.apicatalog.vc.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VerificationResult extends JsonObject {

    private static final String PROPERTY_CHECKS = "checks";
    private static final String PROPERTY_WARNINGS = "warnings";
    private static final String PROPERTY_ERRORS = "errors";
    
    public VerificationResult() {
        put(PROPERTY_CHECKS, new JsonArray());
        put(PROPERTY_WARNINGS, new JsonArray());
        put(PROPERTY_ERRORS, new JsonArray());
    }
    
    public VerificationResult addCheck(String  name) {
        getJsonArray(PROPERTY_CHECKS).add(name);
        return this;
    }
    
    public VerificationResult addError(String  name) {
        getJsonArray(PROPERTY_ERRORS).add(name);
        return this;
    }    
    
}
