package com.apicatalog.vc.fnc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.json.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;

public record DeriveRequest(
        JsonObject credential,
        // options
        Collection<String> selectivePointers) {

    static final String CREDENTIAL = "verifiableCredential";

    static final String OPTIONS = "options";
    static final String OPTION_SELECTIVE_POINTERS = "selectivePointers";

    public static DeriveRequest of(final JsonObject json) {

        JsonObject credential = json;

        if (json.containsKey(CREDENTIAL)) {
            credential = json.getJsonObject(CREDENTIAL);
        }

        Collection<String> selectivePointers = Collections.emptyList();

        var jsonOptions = json.get(OPTIONS);

        if (JsonUtils.isObject(jsonOptions)) {

            JsonObject options = jsonOptions.asJsonObject();

            // selective pointers
            var pointers = options.get(OPTION_SELECTIVE_POINTERS);
            if (JsonUtils.isArray(pointers)) {
                selectivePointers = new ArrayList<>(pointers.asJsonArray().size());
                for (var pointer : pointers.asJsonArray()) {
                    if (JsonUtils.isNotString(pointer)) {
                        throw new IllegalArgumentException("An invalid mandatory pointer [" + pointer + "], exptected string value.");
                    }
                    selectivePointers.add(((JsonString) pointer).getString());
                }
            }
        }
        return new DeriveRequest(
                credential,
                selectivePointers);
    }
}
