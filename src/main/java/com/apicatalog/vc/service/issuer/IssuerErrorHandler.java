package com.apicatalog.vc.service.issuer;

import java.util.Arrays;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;

import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

class IssuerErrorHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var errorResponse = new JsonObject();

        final Throwable e = ctx.failure();

        if (e instanceof SigningError se) {

            errorResponse.put("id",  toString(se.getCode()));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DocumentError de) {

            errorResponse.put("id", "MALFORMED");
            errorResponse.put("code", de.getType());

//            verificationResult.addError("MALFORMED");
//            verificationResult.addError(toString(de));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DecodeException de) {

            errorResponse.put("id", "MALFORMED");

//            verificationResult.addError("MALFORMED");
//            verificationResult.addError("INVALID_DOCUMENT");

            ctx.response().setStatusCode(400);


        } else {
            ctx.response().setStatusCode(500);

        }
        errorResponse.put("message",  e.getMessage());

        var content = errorResponse.toString();

        ctx.response()
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(content.getBytes().length))
            .end(content);
    }

    static String toString(DocumentError de) {
        return de.getType().name().toUpperCase() + "_" + de.getSubject().toUpperCase();
    }

    static String toString(SigningError.Code code) {
        return String.join("_", Arrays.stream(code.name().split("(?=\\p{Upper})")).map(String::toUpperCase).toList());
    }

}