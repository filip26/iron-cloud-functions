package com.apicatalog.vc.service.verifier;

import java.util.Arrays;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
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

            verificationResult.addCheck(toVcErrorCode(ve.getCode()));

            ctx.response().setStatusCode(400);

        } else if (e instanceof DocumentError de) {

            verificationResult.addError(toVcErrorCode(de.getCode()));
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

    static String toVcErrorCode(String code) {
        if ("MissingVerificationMethod".equals(code)
                || "MissingProofVerificationId".equals(code)
                || "UnknownProofVerificationId".equals(code)) {
            return "MALFORMED_PROOF_ERROR";
        }

        if ("InvalidProofPurpose".equals(code)) {
            return "MISMATCHED_PROOF_PURPOSE_ERROR";
        }
        
        return "MALFORMED";
    }

    static String toVcErrorCode(Code code) {

        switch (code) {
        case UnsupportedCryptoSuite:
            return "INVALID_PROOF_CONFIGURATION";
        case Expired:
            return "EXPIRED";
        case InvalidSignature:
            return "INVALID_SIGNATURE";
        case NotValidYet:
            return "NOT_VALID_YET";
        }

        return "UNKNOWN";
    }
}