package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public abstract class HttpJsonFunction implements HttpFunction {

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

        try {
            var input = JsonRequest.of(request);
            var output = process(input);

            if (output == null) {
                throw new IllegalStateException();
            }
            
            response.setStatusCode(successCode);

            try (var writer = response.getWriter()) {
                response.setContentType("application/json");
                writer.write(output.toString());
            }

        } catch (HttpFunctionError e) {

            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            response.setContentType("application/json");

            // TODO Auto-generated catch block
//            e.printStackTrace();

        } catch (IOException e) {
            response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
            
        }
    }

    protected abstract JsonObject process(JsonObject jsonData) throws HttpFunctionError;

    protected static boolean isJsonRequest(HttpRequest httpRequest) {
        var contentType = httpRequest.getContentType().orElse(null);
        return contentType != null && "application/json".equals(contentType);
    }

    protected static JsonObject of(HttpRequest httpRequest) throws IOException {
        try (var parser = Json.createParser(httpRequest.getReader())) {
            parser.next();
            JsonObject data = parser.getObject();
            return data;
        }
    }
}
