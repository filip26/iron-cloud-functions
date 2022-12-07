package com.apicatalog.vc.service.verifier;

import java.util.Arrays;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;

class VerifierErrorHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        VerificationResult verificationResult = ctx.get(Constants.CTX_RESULT);

        if (verificationResult == null) {
            verificationResult = new VerificationResult();
        }

        final Throwable e = ctx.failure();

        if (e instanceof VerificationError ve) {

            verificationResult.addError(toString(ve.getCode().name()));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DocumentError de) {

            verificationResult.addError("MALFORMED");
            verificationResult.addError(toString(de.getCode()));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DecodeException de) {

            verificationResult.addError("MALFORMED");
            verificationResult.addError("INVALID_DOCUMENT");

            ctx.response().setStatusCode(400);


        } else {
            e.printStackTrace();
            ctx.response().setStatusCode(500);
        }

        var content = verificationResult.toString();

        ctx.response()
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(content.getBytes().length))
            .end(content);
    }

    static String toString(String code) {
        return String.join("_", Arrays.stream(code.split("(?=\\p{Upper})")).map(String::toUpperCase).toList());
    }
}