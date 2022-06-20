package com.apicatalog.vc.service;

import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.VerificationError;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class ErrorHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        final VerificationResult verificationResult = ctx.get(Constants.CTX_RESULT);
        
        final Throwable e = ctx.failure();

        if (e instanceof VerificationError ve) {
            verificationResult.addError(ve.getCode().name());

        } else if (e instanceof DataError de) {

            verificationResult.addError("MALFORMED");
            verificationResult.addError(toString(de));
            
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
    
}