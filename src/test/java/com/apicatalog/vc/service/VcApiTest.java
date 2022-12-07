package com.apicatalog.vc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class VcApiTest {

    static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("isser - proof options", "it0001-in.jsonld")
                );
    }

    @BeforeEach
    @DisplayName("Deploy a verticle")
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new VcApiVerticle(), testContext.succeedingThenComplete());
    }

//    @RepeatedTest(3)
    @ParameterizedTest
    @MethodSource("testData")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    @DisplayName("Execute a test")
    void executeTest(String name, String path, Vertx vertx, VertxTestContext testContext) throws Throwable {

        try (final InputStream is = this.getClass().getResourceAsStream(path)) {

            final byte[] input = is.readAllBytes();

            WebClient client = WebClient.create(vertx);

            client.post(8080, "localhost", "/credentials/issue")
                    .putHeader("content-type", "application/json")
                    .as(BodyCodec.string())
                    .sendBuffer(Buffer.buffer(input),
                            testContext.succeeding(response -> testContext.verify(() -> {
                                assertEquals(201, response.statusCode());
                                testContext.completeNow();
                            })));
        }

    }

    @AfterEach
    @DisplayName("Check that the verticle is still there")
    void shutdown(Vertx vertx) {
        assertNotNull(vertx.deploymentIDs());
        assertEquals(1, vertx.deploymentIDs().size());
    }
}
