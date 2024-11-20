package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonParserFactory;

public abstract class HttpJsonFunction implements HttpFunction {

    protected static final JsonParserFactory JSON_PARSER_FACTORY = Json.createParserFactory(Collections.emptyMap());
    protected static final JsonWriterFactory JSON_WRITER_FACTORY = Json.createWriterFactory(Collections.emptyMap());

    private final String method;
    private final int successCode;

    public HttpJsonFunction(final String method, final int successCode) {
        this.method = method;
        this.successCode = successCode;
    }

    @Override
    public void service(final HttpRequest request, final HttpResponse response) throws IOException {

        if (!method.equalsIgnoreCase(request.getMethod())) {
            response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
            return;
        }

        if (isJsonRequest(request)) {
            response.setStatusCode(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
        }

        JsonStructure output = null;

        try {
            output = process(parseJson(request));

            if (output == null) {
                throw new IllegalStateException();
            }

            response.setStatusCode(successCode);

        } catch (HttpFunctionError e) {

            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            output = error(e);

        } catch (IllegalArgumentException e) {
            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            output = error(e.getMessage(), null);

        } catch (IOException e) {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            output = null;
        }

        if (output != null) {
            writeJson(output, response);
        }
    }

    protected abstract JsonObject process(JsonObject jsonData) throws HttpFunctionError;

    protected static final boolean isJsonRequest(HttpRequest httpRequest) {
        var contentType = httpRequest.getContentType().orElse(null);
        return contentType != null && "application/json".equals(contentType);
    }

    protected static final JsonObject parseJson(HttpRequest httpRequest) throws IOException {
        try (var parser = JSON_PARSER_FACTORY.createParser(httpRequest.getReader())) {
            parser.next();
            JsonObject data = parser.getObject();
            return data;
        }
    }

    protected static final void writeJson(JsonStructure data, HttpResponse response) throws IOException {
        response.setContentType("application/json");
        try (var writer = JSON_WRITER_FACTORY.createWriter(response.getWriter())) {
            writer.write(data);
        }
    }

    protected static final JsonObject error(HttpFunctionError error) {
        return error(error.getMessage(), error.getCode());
    }

    protected static final JsonObject error(String message, String code) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (message != null) {
            builder.add("message", message);
        }
        if (code != null) {
            builder.add("code", code);
        }
        return builder.build();
    }
    
//    // Set CORS headers
//    //   Allows GETs from any origin with the Content-Type
//    //   header and caches preflight response for 3600s
//    response.appendHeader("Access-Control-Allow-Origin", "*");
//
//    if ("OPTIONS".equals(request.getMethod())) {
//      response.appendHeader("Access-Control-Allow-Methods", "GET");
//      response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
//      response.appendHeader("Access-Control-Max-Age", "3600");
//      response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
//      return;
//    }
}
