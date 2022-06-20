package com.apicatalog.vc.service;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.vc.api.Vc;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class VerificationHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        
        var route = ctx.currentRoute();
        
        var document = ctx.body().asJsonObject();
        
        final String documentKey = route.getMetadata(Constants.CTX_DOCUMENT_KEY);
        
        if (StringUtils.isNotBlank(documentKey)) {
            document = document.getJsonObject(documentKey);            
        }

        // set verification result
        var verificationResult = new VerificationResult();
        verificationResult.addCheck("proof");
        
        ctx.put(Constants.CTX_RESULT, verificationResult);
       

        try {

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
