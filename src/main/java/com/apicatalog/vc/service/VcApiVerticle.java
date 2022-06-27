package com.apicatalog.vc.service;

import static io.vertx.ext.web.validation.builder.Parameters.optionalParam;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;


public class VcApiVerticle extends AbstractVerticle {

    Instant startTime;

    @Override
    public void start() throws Exception {

        var schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
        var schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

        final Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create().setBodyLimit(250000));

        router
            .post("/credentials/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.VERIFIABLE_CREDENTIAL_KEY)
            .putMetadata(Constants.CTX_STRICT, true)

            //TODO validation
            .handler(ctx -> {
                System.out.println(ctx.body().asString());
                ctx.next();
            })

            // options
            .handler(new VerifyEmbeddedOptionsHandler())

            // verify
            .handler(new VerificationHandler())

            // handle errors
            .failureHandler(new ErrorHandler());

        router
            .post("/presentations/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.PRESENTATION_KEY)
            .putMetadata(Constants.CTX_STRICT, true)

            //TODO validation

            // options
            .handler(new VerifyEmbeddedOptionsHandler())

            // verify
            .handler(new VerificationHandler())

            // handle errors
            .failureHandler(new ErrorHandler());


        router
            .post("/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_STRICT, false)

            // validation
            .handler(ValidationHandlerBuilder
                        .create(schemaParser)                           //TODO body validation
                        .queryParameter(optionalParam(Constants.OPTION_DOMAIN, stringSchema()))
                        .queryParameter(optionalParam(Constants.OPTION_CHALLENGE, stringSchema()))

                        .predicate(RequestPredicate.BODY_REQUIRED)      // request body is required
                        .build()
                    )

            // options
            .handler(ctx -> {
                final RequestParameters parameters = ctx.get(ValidationHandler.REQUEST_CONTEXT_KEY);

                var domain = parameters.queryParameter(Constants.OPTION_DOMAIN);

                if (domain != null) {
                    ctx.put(Constants.OPTION_DOMAIN, domain.getString());
                }

                var challenge = parameters.queryParameter(Constants.OPTION_CHALLENGE);

                if (challenge != null) {
                    ctx.put(Constants.OPTION_CHALLENGE, challenge.getString());
                }

                ctx.next();
            })

            // verify
            .handler(new VerificationHandler())

            // handle errors
            .failureHandler(new ErrorHandler());

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
            .failureHandler(new ErrorHandler());
            ;
            
        
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

        // server options
        var serverOptions = new HttpServerOptions()
                                    .setMaxWebSocketFrameSize(1000000)
                                    .setUseAlpn(true);
        
        // service 
        vertx
            .createHttpServer(serverOptions)
            .requestHandler(router)
            .listen(getDefaultPort())
                .onSuccess(ctx -> {
                    System.out.println(VcApiVerticle.class.getName() +  " started on port " + ctx.actualPort() + " with " + Charset.defaultCharset()  + " charset.");
                    startTime = Instant.now();
                })
                .onFailure(ctx ->
                    System.err.println(VcApiVerticle.class.getName() +  " start failed [" + ctx.getMessage() + "].")
                );
    }

    @Override
    public void stop() throws Exception {
        if (startTime != null) {
            System.out.println(VcApiVerticle.class.getName() +  " stopped after running for " +  Duration.between(startTime, Instant.now()) + ".");
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
