package com.apicatalog.vc.service.issuer;

import com.apicatalog.vc.service.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
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
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.VERIFIABLE_CREDENTIAL_KEY)
            
            // validation TODO
//            .handler(ctx -> {
//                ctx.next();
//            })
            
            //FIXME remove
            .handler(ctx -> {
                System.out.println(ctx.body().asString());
                ctx.next();
            })
            
            // issue
            .blockingHandler(new IssuingHandler())

            // handle errors
            .failureHandler(new IssuerErrorHandler());
    }
}
