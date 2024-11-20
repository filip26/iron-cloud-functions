package com.apicatalog.vc.fnc;

import java.io.IOException;

import com.apicatalog.jsonld.http.media.MediaType;
import com.google.cloud.functions.HttpRequest;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

public record JsonRequest(
        String contentType,
        JsonObject data) {

    public static boolean isJsonRequest(HttpRequest httpRequest) {

        var contentType = httpRequest.getContentType().orElse(null);

        return contentType != null && "application/json".equals(contentType);
    }

    public static JsonObject of(HttpRequest httpRequest) throws IOException {

        try (JsonParser parser = Json.createParser(httpRequest.getReader())) {
            parser.next();
            JsonObject data = parser.getObject();
            return data;
        }
    }

}
