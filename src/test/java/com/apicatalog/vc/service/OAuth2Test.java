package com.apicatalog.vc.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.web.client.OAuth2WebClient;
import io.vertx.ext.web.client.OAuth2WebClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class OAuth2Test {

    static Vertx vertx;
    
    @Test
    @DisplayName("🚀 Deploy VC API service verticle and make a verification to Oauth2 protected endpoint")
    void testUnauthenticatedOAuth2Verify(VertxTestContext testContext) {

        var webClient = WebClient.create(vertx);

        var deploymentCheckpoint = testContext.checkpoint();
        var requestCheckpoint = testContext.checkpoint();

        vertx.deployVerticle(new VcApiVerticle(), testContext.succeeding(id -> {
            deploymentCheckpoint.flag();

            webClient
                .post(8080, "localhost", "/credentials/verify")
                .sendJsonObject(new JsonObject()
                            , testContext.succeeding(resp -> {
                                testContext.verify(() -> {
                                    System.out.println(resp.bodyAsString());
                                    assertThat(resp.statusCode()).isEqualTo(HttpResponseStatus.UNAUTHORIZED.code());
                                    requestCheckpoint.flag();
                                });
                            }));

        }));
    }
    
    @Test
    void testOAuth2Verify(VertxTestContext testContext) {

        OAuth2Options credentials = new OAuth2Options()
                .setFlow(OAuth2FlowType.CLIENT)
                .setClientId("123")
                .setClientSecret("456777")
                .setSite("http://localhost:8080")
                ;
        
        
        OAuth2Auth oauth2 = OAuth2Auth.create(vertx, credentials);        
        
        
        JsonObject tokenConfig = new JsonObject()
                ;

        oauth2.authenticate(tokenConfig)
        .onSuccess(user -> {
          // Success
            System.out.println(">>> " + user.subject());
            System.out.println(">>> " + user.attributes());
            System.out.println(">>> " + user.authorizations());
            System.out.println(">>> " + user.principal());
            System.out.println(">>> " + user.expired());
            testContext.completeNow();
        })
        .onFailure(err -> {
          System.err.println("Access Token Error: " + err.getMessage());
          testContext.failNow(err);
          
        });
        
        
      

//        
//        var webClient = WebClient.create(vertx);
//        
//        OAuth2WebClient client = OAuth2WebClient.create(
//                webClient,
//                oauth2,
//                new OAuth2WebClientOptions()
//                  // the client will attempt a single token request, if the request
//                  // if the status code of the response is 401
//                  // there will be only 1 attempt, so the second consecutive 401
//                  // will be passed down to your handler/promise
//                 /* .setRenewTokenOnForbidden(true)*/);
//        
//        System.out.println("3");
//        var deploymentCheckpoint = testContext.checkpoint();
//        var requestCheckpoint = testContext.checkpoint();
//
//        vertx.deployVerticle(new VcApiVerticle(), testContext.succeeding(id -> {
//            deploymentCheckpoint.flag();
//            System.out.println("4");
//            webClient
//                .post(8080, "localhost", "/credentials/verify")
//                .sendJsonObject(new JsonObject()
//                            , testContext.succeeding(resp -> {
//                                System.out.println("5");
//
//                                
//                                
//                                testContext.verify(() -> {
//                                    System.out.println(resp.bodyAsString());
//                                    assertThat(resp.statusCode()).isEqualTo(HttpResponseStatus.OK.code());
//                                    requestCheckpoint.flag();
//                                });
//                            }));
//
//        }));
    }
    
    @BeforeAll
    static void prepare() {
        vertx = Vertx.vertx(new VertxOptions()
                .setMaxEventLoopExecuteTime(1000)
                .setPreferNativeTransport(true)
                .setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
        
        vertx.deployVerticle(new VcApiVerticle());
    }
    
    @AfterAll
    static void cleanup() {
        vertx.close();
    }
    
    
}