package com.apicatalog.vc.service.issuer;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import com.apicatalog.vc.service.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.json.schema.SchemaParser;


public class IssuerApi extends AbstractVerticle {

    public static void setup(Router router, SchemaParser schemaParser) throws Exception {

        // issues a credential and returns the signed credentials in the response body
        router
            .post("/credentials/issue")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/ld+json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.CREDENTIAL_KEY)
            
            // validation TODO
            .handler(ctx -> {
                ctx.next();
            })
            
            // options
            .handler(new IssueEmbeddedOptionsHandler())

            // issue
            .handler(new IssuingHandler())

            // handle errors
            .failureHandler(new IssuerErrorHandler());
    }
}
