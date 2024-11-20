package com.apicatalog.vc.fnc;

import java.util.Arrays;

public class HttpFunctionError extends Exception {
    
    private static final long serialVersionUID = -3594627626673763447L;
    
    protected String code;

    public HttpFunctionError(Throwable e, String code) {
        super(e.getMessage(), e);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }

    static String toString(String code) {
        return String.join("_", Arrays.stream(code.split("(?=\\p{Upper})")).map(String::toUpperCase).toList());
    }
}
