package com.apicatalog.vc.service;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import com.apicatalog.vc.service.issuer.IssuerApi;
import com.apicatalog.vc.service.verifier.VerifierApi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;


public class VcApiVerticle extends AbstractVerticle {

    Instant startTime;

    @Override
    public void start(final Promise<Void> startPromise) throws Exception {

        
        OAuth2Options credentials = new OAuth2Options()
                .setFlow(OAuth2FlowType.CLIENT)
                .setClientId("<client-id>")
                .setClientSecret("<client-secret>")
                .setSite("https://api.oauth.com");
        
        OAuth2Auth oauth2 = new MockOAuth2Provider();
                //OAuth2Auth.create(vertx, credentials);
                
        OAuth2AuthHandler oauth2Handler = (OAuth2AuthHandler) OAuth2AuthHandler.create(vertx, oauth2);
//        oauth2.
        
        var schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
        var schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

        
        final Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create().setBodyLimit(250000));
        
//        router.route("/*")
//        .handler(ctx -> {
//            System.out.println("Token " + ctx.request().absoluteURI() + ", " + ctx.request().method());
//            
//           ctx.end(); 
//        });

        router.post("/oauth/token")
        .handler(ctx -> {
            System.out.println("Token POST " + ctx.request().headers());
            System.out.println(ctx.body().asString());
            System.out.println(ctx.queryParams());
            
            var response = new JsonObject()
            .put("access_token", "2YotnFZFEjr1zCsicMWpAA")
            .put("token_type", "example")
            .put("expires_in", 3600)
            .put("example_parameter", "example_value")
            ;
            
            ctx.json(response);
            
//           ctx.end(); 
        });
        

        // verifier's VC API
//        router.post("/credentials/verify").handler(oauth2Handler);

        VerifierApi.setup(vertx, router, schemaParser);
        
        // issuer's VC API
        IssuerApi.setup(router, schemaParser);
        
        // static resources
        router
            .get("/key/*")
            .handler(StaticHandler
                        .create("webroot/key/")
                        .setIncludeHidden(false)
                        .setDefaultContentEncoding("UTF-8")
                        .setMaxAgeSeconds(365*24*3600l)      
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
                startPromise.complete();
                System.out.println(VcApiVerticle.class.getName() +  " started on port " + ctx.actualPort() + " with " + Charset.defaultCharset()  + " charset.");
                startTime = Instant.now();                
            })
            
            .onFailure(ctx -> {
                System.err.println(VcApiVerticle.class.getName() +  " start failed [" + ctx.getMessage() + "].");
                startPromise.fail(ctx);                
            });
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
