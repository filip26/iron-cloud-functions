package com.apicatalog.vc.service;

import static io.vertx.ext.web.validation.builder.Parameters.optionalParam;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.vc.api.Vc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;


public class VerifierVerticle extends AbstractVerticle {

    private static final String OPTION_DOMAIN = "domain";
    private static final String OPTION_CHALLENGE = "challenge";
    
    private static final String CTX_RESULT = "verificationResult";
    
    Instant startTime;

    @Override
    public void start() throws Exception {

        var schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
        var schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

        final Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create().setBodyLimit(250000));

        router
            .post("/credentials/verify")
            .putMetadata("input-type", "crendetial")
            .putMetadata("strict", true)
            .handler(ctx -> ctx.reroute("/verify"));

        router
            .post("/presentations/verify")
            .putMetadata("input-type", "presentation")
            .putMetadata("strict", true)
            .handler(ctx -> ctx.reroute("/verify"));

        router
            .post("/verify")
            .consumes("application/json")
            .consumes("application/ld+json")
            .produces("application/json")

            // validation
            .handler(ValidationHandlerBuilder
                        .create(schemaParser)
                        .queryParameter(optionalParam(OPTION_DOMAIN, stringSchema()))
                        .queryParameter(optionalParam(OPTION_CHALLENGE, stringSchema()))
                        .predicate(RequestPredicate.BODY_REQUIRED)      // request body is required
                        .build()
                    )

            // options
            .handler(ctx -> {
                final RequestParameters parameters = ctx.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                
                var domain = parameters.queryParameter(OPTION_DOMAIN);
                
                if (domain != null) {
                    ctx.put(OPTION_DOMAIN, domain.getString());
                }
                
                var challenge = parameters.queryParameter(OPTION_CHALLENGE);
                
                if (challenge != null) {
                    ctx.put(OPTION_CHALLENGE, challenge.getString());
                }
            })
            
            // set verification result
            .handler(ctx -> {
                ctx.put(CTX_RESULT, new VerificationResult());
                ctx.next();
            })
           
            // verify
            .handler(ctx -> {
                
                final VerificationResult verificationResult = ctx.get(CTX_RESULT);
                
                var body = ctx.body().asJsonObject();

                var document = body.getJsonObject("verifiableCredential" 
                        //body.getJsonObject("verifiablePresentation", null)
                        );
                
                try {
                    
                    verificationResult.addCheck("proof");

                    Vc.verify(JsonDocument
                                .of(new StringReader(document.toString()))
                                .getJsonContent()
                                .orElseThrow(IllegalStateException::new)
                                .asJsonObject())
                    
                        .domain(ctx.get(OPTION_DOMAIN, null))
                        
                        // assert document validity
                        .isValid();
                                                        
                    ctx.json(verificationResult);
                
                } catch (JsonLdError | VerificationError | DataError e) {
                    ctx.fail(e);
                }
            })
            
            // handle errors
            .failureHandler(new ErrorHandler());
            
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
    
    
    static class ErrorHandler implements Handler<RoutingContext> {

        @Override
        public void handle(RoutingContext ctx) {

            final VerificationResult verificationResult = ctx.get(CTX_RESULT);
            
            final Throwable e = ctx.failure();

            if (e instanceof VerificationError ve) {
                verificationResult.addError(ve.getCode().name());

            } else if (e instanceof DataError de) {

                verificationResult.addError("MALFORMED");
                
                ctx.response().setStatusCode(400);
                
            } else {
                ctx.response().setStatusCode(500);
                
            }
            
            var content = verificationResult.toString();

            ctx.response()
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(content.getBytes().length))
                .end(content);
        }
    }
    /*
     router
  .route("/metadata/route")
  .putMetadata("metadata-key", "123")
  .handler(ctx -> {
    Route route = ctx.currentRoute();
    String value = route.getMetadata("metadata-key"); // 123
    // will end the request with the value 123
    ctx.end(value);
  });
     */
}
