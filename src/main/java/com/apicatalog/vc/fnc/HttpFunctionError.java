package com.apicatalog.vc.fnc;

import java.util.Arrays;

public class HttpFunctionError extends Exception {
    
    private static final long serialVersionUID = -3594627626673763447L;
    
    protected String code;

    public HttpFunctionError(Throwable e, String code) {
        super(e);
        this.code = code;
    }
    
//    public void handle(RoutingContext ctx) {
//
//        var errorResponse = new JsonObject();
//
//        final Throwable e = ctx.failure();
//
//        if (e instanceof SigningError se) {
//
//            errorResponse.put("id",  toString(se.getCode().name()));
//
//            ctx.response().setStatusCode(400);
//
//        } else if (e instanceof DocumentError de) {
//
//            errorResponse.put("id", "MALFORMED");
//            errorResponse.put("code",  toString(de.getCode()));
//
//            ctx.response().setStatusCode(400);
//
//        } else if (e instanceof DecodeException de) {
//
//            errorResponse.put("id", "MALFORMED");
//
//            ctx.response().setStatusCode(400);
//
//
//        } else {
//            e.printStackTrace();
//            ctx.response().setStatusCode(500);
//        }
//        errorResponse.put("message",  e.getMessage());
//
//        var content = errorResponse.toString();
//
//        ctx.response()
//            .putHeader("content-type", "application/json")
//            .putHeader("content-length", Integer.toString(content.getBytes().length))
//            .end(content);
//    }

    static String toString(String code) {
        return String.join("_", Arrays.stream(code.split("(?=\\p{Upper})")).map(String::toUpperCase).toList());
    }
}
