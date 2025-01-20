package com.apicatalog.vc.fnc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class HttpClientApi {

    protected HttpClient client;

    public HttpClientApi() {
        this.client = HttpClient.newHttpClient();
    }

    public String callApi(String apiUrl, String method, String payload) throws Exception {

        // Build the request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // Set the HTTP method and body if necessary
        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(payload));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = requestBuilder.build();

        // Send the request and get the response
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        // Check response status code
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP request failed with status code: " + response.statusCode());
        }
    }
}
