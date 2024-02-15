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
                Arguments.of("issue - proof options", "0001-in.jsonld", "/credentials/issue", 201),
                Arguments.of("verify - proof options", "0002-in.jsonld", "/credentials/verify", 200),
                Arguments.of("issue - proof options", "0003-in.jsonld", "/credentials/issue", 201),
                Arguments.of("verify - proof options", "0004-in.jsonld", "/credentials/verify", 400),
                Arguments.of("verify - proof options", "0005-in.jsonld", "/credentials/verify", 400)
                );
    }

    @BeforeEach
    @DisplayName("Deploy a verticle")
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        
        //set environmental variables
        System.setProperty("VC_PUBLIC_KEY", "z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y");
        System.setProperty("VC_PRIVATE_KEY", "z3u2RRiEW8idMgvP3kthwjWqPo9W8X4pvEp52toGwp8EjJvg");
        System.setProperty("VC_VERIFICATION_KEY", "did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y#z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y");

        System.setProperty("EC_PUBLIC_KEY", "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP");
        System.setProperty("EC_PRIVATE_KEY", "z42twTcNeSYcnqg1FLuSFs2bsGH3ZqbRHFmvS9XMsYhjxvHN");
        System.setProperty("EC_VERIFICATION_KEY", "did:key:zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP#zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP");

        // start
        vertx.deployVerticle(new VcApiVerticle(), testContext.succeedingThenComplete());
    }

//    @RepeatedTest(3)
    @ParameterizedTest
    @MethodSource("testData")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    @DisplayName("Execute a test")
    void executeTest(String name, String path, String endpoint, int code, Vertx vertx, VertxTestContext testContext) throws Throwable {

        try (final InputStream is = this.getClass().getResourceAsStream(path)) {

            final byte[] input = is.readAllBytes();

            WebClient client = WebClient.create(vertx);

            client.post(8080, "localhost", endpoint)
                    .putHeader("content-type", "application/json")
                    .as(BodyCodec.string())
                    .sendBuffer(Buffer.buffer(input),
                            testContext.succeeding(response -> testContext.verify(() -> {
                                assertEquals(code, response.statusCode());
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
