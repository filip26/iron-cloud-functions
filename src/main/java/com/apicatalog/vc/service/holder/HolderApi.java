package com.apicatalog.vc.service.holder;

import com.apicatalog.vc.service.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.json.schema.SchemaParser;


public class HolderApi extends AbstractVerticle {

    public static void setup(Router router, SchemaParser schemaParser) throws Exception {
        router
            .post("/holder/derive")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/ld+json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.VERIFIABLE_CREDENTIAL_KEY)
                        
            //FIXME remove
            .handler(ctx -> {
                System.out.println(ctx.body().asString());
                ctx.next();
            })
            
            // issue
            .blockingHandler(new HolderHandler())

            // handle errors
            .failureHandler(new HolderErrorHandler());
    }
}
