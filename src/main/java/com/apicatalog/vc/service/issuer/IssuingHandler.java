package com.apicatalog.vc.service.issuer;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;
import com.apicatalog.ld.signature.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020;
import com.apicatalog.vc.api.Vc;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.json.Json;

class IssuingHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var route = ctx.currentRoute();

        var document = ctx.body().asJsonObject();

        final String documentKey = route.getMetadata(Constants.CTX_DOCUMENT_KEY);

        if (StringUtils.isNotBlank(documentKey)) {
            document = document.getJsonObject(documentKey);
        }

        if (document == null) {
            ctx.fail(new DataError(ErrorType.Invalid, "document"));
            return;
        }
        
        try {
            var keyPair = new Ed25519KeyPair2020(URI.create("did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y"));
            keyPair.setPrivateKey(Ed25519KeyPair2020.decodeKey("zRuuyWBEr6MivrDHUX4Yd7jiGzMbGJH38EHFqQxztA4r1QY"));
            
            var proofOptions = new Ed25519ProofOptions2020();
            proofOptions.setVerificationMethod(new Ed25519VerificationKey2020(URI.create("https://vc.apicatalog.com/key/test.json")));

            var signed = Vc.sign(JsonDocument
                            .of(new StringReader(document.toString()))
                            .getJsonContent()
                            .orElseThrow(IllegalStateException::new)
                            .asJsonObject()
                            ,
                            keyPair,
                            proofOptions)
                                .getCompacted(
                                    Json.createArrayBuilder()
                                        .add("https://www.w3.org/2018/credentials/v1")
                                        .add("https://w3id.org/security/suites/ed25519-2020/v1")
                                        .build()
                                        );
            var response = ctx.response();

            response.putHeader("content-type", "application/ld+json");
            response.end(signed.toString());

        } catch (JsonLdError | DataError | IllegalStateException | SigningError e) {
            ctx.fail(e);
        }
    }

}
