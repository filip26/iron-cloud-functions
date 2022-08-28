package com.apicatalog.vc.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class OAuth2Test {

    @Test
    @DisplayName("🚀 Deploy VC API service verticle and make a verification to Oauth2 protected endpoint")
    void testUnauthenticatedOAuth2Verify(Vertx vertx, VertxTestContext testContext) {

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
                                    assertThat(resp.statusCode()).isEqualTo(HttpResponseStatus.UNAUTHORIZED.code());
                                    requestCheckpoint.flag();
                                });
                            }));

        }));
    }
}