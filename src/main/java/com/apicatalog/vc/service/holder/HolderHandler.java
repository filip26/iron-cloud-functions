package com.apicatalog.vc.service.holder;

import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;

class HolderHandler implements Handler<RoutingContext> {

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
            var response = ctx.response();

            response.setStatusCode(503); // created
            response.putHeader("content-type", "application/ld+json");
            response.end();

        } catch (IllegalStateException e) {
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
