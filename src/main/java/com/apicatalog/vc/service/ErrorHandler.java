package com.apicatalog.vc.service;

import java.util.Arrays;

import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.VerificationError;

import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;

class ErrorHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        VerificationResult verificationResult = ctx.get(Constants.CTX_RESULT);

        if (verificationResult == null) {
            verificationResult = new VerificationResult();
        }

        final Throwable e = ctx.failure();

        if (e instanceof VerificationError ve) {

            verificationResult.addError(toString(ve.getCode()));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DataError de) {

            verificationResult.addError("MALFORMED");
            verificationResult.addError(toString(de));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DecodeException de) {

            verificationResult.addError("MALFORMED");
            verificationResult.addError("INVALID_DOCUMENT");

            ctx.response().setStatusCode(400);


        } else {
            ctx.response().setStatusCode(500);

        }

        var content = verificationResult.toString();

        ctx.response()
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(content.getBytes().length))
            .end(content);
    }

    static String toString(DataError de) {
        return de.getType().name().toUpperCase() + "_" + de.getSubject().toUpperCase();
    }

    static String toString(VerificationError.Code code) {
        return String.join("_", Arrays.stream(code.name().split("(?=\\p{Upper})")).map(String::toUpperCase).toList());
    }

}