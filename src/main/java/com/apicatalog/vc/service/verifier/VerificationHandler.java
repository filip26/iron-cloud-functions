package com.apicatalog.vc.service.verifier;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ecdsa.ECDSASignature2019;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSASignature2022;
import com.apicatalog.vc.Vc;
import com.apicatalog.vc.integrity.DataIntegrityVocab;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
            var value  = document.getValue(documentKey);
            if (value instanceof JsonArray array) {
                // ignore key length type for verification 
                // keyType = array.getString(0);
                document = array.getJsonObject(1);
                
            } else if (value instanceof JsonObject object) {
                document = object;
            }
        }

        if (document == null) {
            ctx.fail(new DocumentError(ErrorType.Invalid));
            return;
        }

        try {
            verificationResult.addCheck("PROOF");

            Vc.verify(JsonDocument
                    .of(new StringReader(document.toString()))
                    .getJsonContent()
                    .orElseThrow(IllegalStateException::new)
                    .asJsonObject(),
                    new EdDSASignature2022(),
                    new ECDSASignature2019(),
                    new Ed25519Signature2020())

                    .param(DataIntegrityVocab.DOMAIN.name(), ctx.get(Constants.OPTION_DOMAIN, null))
                    .param(DataIntegrityVocab.CHALLENGE.name(), ctx.get(Constants.OPTION_CHALLENGE, null))
                    .param(DataIntegrityVocab.PURPOSE.name(), ctx.get(Constants.OPTION_PURPOSE, null))

                    // assert document validity
                    .isValid();

            ctx.json(verificationResult);

        } catch (JsonLdError | VerificationError | DocumentError e) {
            ctx.fail(e);
        }
    }

}
