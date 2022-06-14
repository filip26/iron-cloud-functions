package com.apicatalog.vc.http.api;


import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;

public class VerifierVerticle extends AbstractVerticle {

    Instant startTime;

    @Override
    public void start() throws Exception {

        final SchemaRouter schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
        final SchemaParser schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

        final Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create().setBodyLimit(250000));

        // static resources
        router
            .get("/static/*")
            .handler(StaticHandler
                        .create("webroot/static/")
                        .setIncludeHidden(false)
                        .setDefaultContentEncoding("UTF-8")
                        .setMaxAgeSeconds(100*24*3600l)
                    );

        router.get().handler(StaticHandler
                                    .create()
                                    .setIncludeHidden(false)
                                    .setDefaultContentEncoding("UTF-8")
                                    .setMaxAgeSeconds(4*3600l)     // maxAge = 4 hours
                            );

        // server
        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(getDefaultPort())
                .onSuccess(ctx -> {
                    System.out.println(VerifierVerticle.class.getName() +  " started on port " + ctx.actualPort() + " with " + Charset.defaultCharset()  + " charset.");
                    startTime = Instant.now();
                })
                .onFailure(ctx ->
                    System.err.println(VerifierVerticle.class.getName() +  " start failed [" + ctx.getMessage() + "].")
                );
    }

    @Override
    public void stop() throws Exception {
        if (startTime != null) {
            System.out.println(VerifierVerticle.class.getName() +  " stopped after running for " +  Duration.between(startTime, Instant.now()) + ".");
        }
    }

    static final int getDefaultPort() {
        final String envPort = System.getenv("PORT");

        if (envPort != null) {
            return Integer.valueOf(envPort);
        }
        return 8080;
    }
}
