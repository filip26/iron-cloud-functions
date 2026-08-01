package com.apicatalog.vc.fnc;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.cloud.functions.HttpResponse;

public class MockHttpResponse implements HttpResponse {

    int code;
    String message;
    String contentType;
    
    OutputStream os;
    BufferedWriter writer;

    public MockHttpResponse() {
        this.os = new ByteArrayOutputStream();
        this.writer = new BufferedWriter(new OutputStreamWriter(this.os));
    }
    
    @Override
    public void setStatusCode(int code) {
        this.code = code;
    }

    @Override
    public void setStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    @Override
    public void appendHeader(String header, String value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return os;
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return writer;
    }

    public Integer getStatusCode() {
        return code;
    }

}
