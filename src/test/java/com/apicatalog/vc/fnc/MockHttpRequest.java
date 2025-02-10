package com.apicatalog.vc.fnc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.cloud.functions.HttpRequest;

public class MockHttpRequest implements HttpRequest {

    final byte[] content;
    
    public MockHttpRequest(byte[] content) {
        this.content = content;
    }

    public static MockHttpRequest of(String json) {
        return new MockHttpRequest(json.getBytes());
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getContentType() {
        // TODO Auto-generated method stub
        return Optional.of("application/json");
    }

    @Override
    public long getContentLength() {
        return content.length;
    }

    @Override
    public Optional<String> getCharacterEncoding() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getQuery() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, HttpPart> getParts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMethod() {
        return "POST";
    }
}
