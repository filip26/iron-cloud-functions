package com.apicatalog.vc.service;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.vc.api.Vc;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class VerificationHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        // set verification result
        var verificationResult = new VerificationResult();
        ctx.put(Constants.CTX_RESULT, verificationResult);

        var route = ctx.currentRoute();

        var document = ctx.body().asJsonObject();

        final String documentKey = route.getMetadata(Constants.CTX_DOCUMENT_KEY);

        if (StringUtils.isNotBlank(documentKey)) {
            document = document.getJsonObject(documentKey);
        }

        if (document == null) {
            ctx.fail(new DataError(ErrorType.Invalid, "document"));
            return;
        }

        try {

            verificationResult.addCheck("PROOF");

            Vc.verify(JsonDocument
                        .of(new StringReader(document.toString()))
                        .getJsonContent()
                        .orElseThrow(IllegalStateException::new)
                        .asJsonObject())

                .domain(ctx.get(Constants.OPTION_DOMAIN, null))

                // assert document validity
                .isValid();

            ctx.json(verificationResult);

        } catch (JsonLdError | VerificationError | DataError e) {
            ctx.fail(e);
        }
    }

}
