package com.apicatalog.vc.service;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;
import com.apicatalog.ld.signature.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.api.Vc;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class IssuingHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

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
        
        final KeyPair keyPair = new Ed25519KeyPair2020(null);
        //TODO
        
        final ProofOptions proofOptions = new Ed25519ProofOptions2020();
        //TODO
        

        try {

            var signed = Vc.sign(JsonDocument
                            .of(new StringReader(document.toString()))
                            .getJsonContent()
                            .orElseThrow(IllegalStateException::new)
                            .asJsonObject()
                            ,
                            keyPair,
                            proofOptions)
                            .getExpanded();     //TODO compacted
            
//            ctx.json(verificationResult);
            ctx.end();

        } catch (JsonLdError | DataError | IllegalStateException | SigningError e) {
            ctx.fail(e);
        }
    }

}
