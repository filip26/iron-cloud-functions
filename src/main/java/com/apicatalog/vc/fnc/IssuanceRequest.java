package com.apicatalog.vc.fnc;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public record IssuanceRequest(
        JsonObject credential,
        // options
        URI purpose,
        Instant created,
        Instant expires,
        String challenge,
        String domain,
        String nonce,
        Collection<String> mandatoryPointers,
        URI credentialId) {

    static final URI ASSERTION_PURPOSE = URI.create("https://w3id.org/security#assertionMethod");

    static final String CREDENTIAL = "credential";
    static final String PRESENTATION = "presentation";

    static final String OPTIONS = "options";

    static final String OPTION_DOMAIN = "domain";
    static final String OPTION_CHALLENGE = "challenge";
    static final String OPTION_NONCE = "nonce";
    static final String OPTION_CREATED = "created";
    static final String OPTION_EXPIRES = "expires";
    static final String OPTION_MANDATORY_POINTERS = "mandatoryPointers";
    static final String OPTION_CREDENTIAL_ID = "credentialId";

    public static IssuanceRequest of(final JsonObject json) {

        JsonObject document = json;

        // unwrap
        if (json.containsKey(CREDENTIAL)) {
            document = json.getJsonObject(CREDENTIAL);

        } else if (json.containsKey(PRESENTATION)) {
            document = json.getJsonObject(PRESENTATION);
        }

        Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expires = null;
        String challenge = null;
        String domain = null;
        String nonce = null;

        Collection<String> mandatoryPointers = Collections.emptyList();
        String credentialId = null;

        var jsonOptions = json.get(OPTIONS);
        if (JsonUtils.isObject(jsonOptions)) {

            JsonObject options = jsonOptions.asJsonObject();

            // credential id
            var id = options.get(OPTION_CREDENTIAL_ID);
            if (JsonUtils.isString(id)) {
                credentialId = ((JsonString) id).getString();
            }

            // mandatory pointers
            var pointers = options.get(OPTION_MANDATORY_POINTERS);
            if (JsonUtils.isArray(pointers)) {
                mandatoryPointers = new ArrayList<>(pointers.asJsonArray().size());
                for (var pointer : pointers.asJsonArray()) {
                    if (JsonUtils.isNotString(pointer)) {
                        throw new IllegalArgumentException("An invalid mandatory pointer [" + pointer + "], exptected string value.");
                    }
                    mandatoryPointers.add(((JsonString) pointer).getString());
                }
            }

            created = getInstant(options, OPTION_CREATED, created);
            expires = getInstant(options, OPTION_EXPIRES, expires);

            challenge = getString(options, OPTION_CHALLENGE, challenge);
            domain = getString(options, OPTION_DOMAIN, domain);
            nonce = getString(options, OPTION_NONCE, nonce);
        }

        return new IssuanceRequest(
                document,
                ASSERTION_PURPOSE,
                created,
                expires,
                challenge,
                domain,
                nonce,
                mandatoryPointers,
                credentialId != null && !credentialId.isEmpty()
                        ? URI.create(credentialId)
                        : null);
    }

    static Instant getInstant(JsonObject source, String term, Instant defaultValue) {
        try {
            var jsonValue = source.get(term);
            if (JsonUtils.isString(jsonValue)) {
                return Instant.parse(((JsonString) jsonValue).getString());
            }
        } catch (DateTimeParseException e) {

        }
        return defaultValue;
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

}
