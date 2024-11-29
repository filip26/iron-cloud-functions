package com.apicatalog.vc.fnc;

import java.net.URI;
import java.time.format.DateTimeParseException;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public record VerificationRequest(
        JsonObject verifiable,
        // options
        URI purpose,
        String challenge,
        String domain,
        String nonce) {

    static final String CREDENTIAL = "verifiableCredential";
    static final String PRESENTATION = "verifiablePresentation";
    
    static final String OPTIONS = "options";

    static final String OPTION_DOMAIN = "domain";
    static final String OPTION_CHALLENGE = "challenge";
    static final String OPTION_NONCE = "nonce";
    static final String OPTION_PURPOSE = "expectedProofPurpose";
    static final String OPTION_EXPIRES = "expires";

    public static VerificationRequest of(final JsonObject json) {

        JsonObject document = json;
        
        // unwrap
        if (json.containsKey(CREDENTIAL)) {
            document = json.getJsonObject(CREDENTIAL);
            
        } else if (json.containsKey(PRESENTATION)) {
            document = json.getJsonObject(PRESENTATION);
        }

        URI purpose = null;
        String challenge = null;
        String domain = null;
        String nonce = null;

        var jsonOptions = json.get(OPTIONS);
        if (JsonUtils.isObject(jsonOptions)) {

            JsonObject options = jsonOptions.asJsonObject();

            purpose = getUri(options, OPTION_PURPOSE, purpose);
            challenge = getString(options, OPTION_CHALLENGE, challenge);
            domain = getString(options, OPTION_DOMAIN, domain);
            nonce = getString(options, OPTION_NONCE, nonce);
        }

        return new VerificationRequest(
                document,
                purpose,
                challenge,
                domain,
                nonce);
    }

    static String getString(JsonObject source, String term, String defaultValue) {
        try {
            var jsonValue = source.get(term);
            if (JsonUtils.isString(jsonValue)) {
                return ((JsonString) jsonValue).getString();
            }
        } catch (DateTimeParseException e) {

        }
        return defaultValue;
    }

    static URI getUri(JsonObject source, String term, URI defaultValue) {
        try {
            var jsonValue = source.get(term);
            if (JsonUtils.isString(jsonValue)) {
                return URI.create(((JsonString) jsonValue).getString());
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {

        }
        return defaultValue;
    }

}
