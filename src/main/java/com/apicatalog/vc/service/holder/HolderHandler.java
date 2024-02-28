package com.apicatalog.vc.service.holder;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.vc.holder.Holder;
import com.apicatalog.vc.service.Constants;
import com.apicatalog.vc.service.Suites;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;

class HolderHandler implements Handler<RoutingContext> {

    static final Holder HOLDER = Holder.with(Suites.ECDSA_SD_2023);
    
    @Override
    public void handle(RoutingContext ctx) {

        var route = ctx.currentRoute();

        var document = ctx.body().asJsonObject();

        final String documentKey = route.getMetadata(Constants.CTX_DOCUMENT_KEY);

        if (StringUtils.isNotBlank(documentKey)) {
            document = document.getJsonObject(documentKey);
        }

        if (document == null) {
            ctx.fail(new DocumentError(ErrorType.Invalid));
            return;
        }

        try {
            var options = HolderOptions.getOptions(ctx);

            var derived = HOLDER.derive(JsonDocument
                    .of(new StringReader(document.toString()))
                    .getJsonContent()
                    .orElseThrow(IllegalStateException::new)
                    .asJsonObject(), options.selectivePointers()).compacted();
            
            var response = ctx.response();

            derived = applyHacks(derived);

            response.setStatusCode(201); // created
            response.putHeader("content-type", "application/ld+json");
            response.end(derived.toString());


        } catch (DocumentError | IllegalStateException | JsonLdError | SigningError e) {
            ctx.fail(e);
        }
    }

    static final JsonObject applyHacks(final JsonObject signed) {

        var document = Json.createObjectBuilder(signed);

        if (JsonUtils.isString(signed.get("credentialSubject"))) {
            document = document
                    .add("credentialSubject",
                            Json.createObjectBuilder()
                                    .add("id", signed.getString("credentialSubject")));
        }

        return document.build();
    }
}
