package com.apicatalog.vc.service.issuer;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;
import com.apicatalog.ld.signature.proof.ProofOptions;
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
            
            final ProofOptions proofOptions = ctx.get(Constants.OPTIONS);

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
            
            //FIXME, remove hack to pass the testing suite
            //see https://github.com/w3c-ccg/vc-api-issuer-test-suite/issues/18
            var proof = signed.getJsonObject("sec:proof");
            
            //FIXME, flatten proofValue, see above
            if (JsonUtils.isObject(proof.get("verificationMethod"))) {
                proof = Json.createObjectBuilder(proof)
                            .add("verificationMethod", 
                                        proof.getJsonObject("verificationMethod")
                                            .getString("id")
                                            ).build();
            }

            if (JsonUtils.isString(signed.get("credentialSubject"))) {
                signed = Json.createObjectBuilder(signed).add("credentialSubject", 
                        Json.createObjectBuilder()
                        .add("id", signed.getString("credentialSubject"))
                        
                        ).build();
            }
            
            if (JsonUtils.isNotNull(signed.get("cred:issuanceDate"))) {
                signed = Json.createObjectBuilder(signed)
                            .add("issuanceDate", signed.get("cred:issuanceDate"))
                            .remove("cred:issuanceDate")
                            .build();
            }

            //FIXME, remove
            if (proof != null) {
                signed = Json.createObjectBuilder(signed).remove("sec:proof").add("proof", proof).build();
            }
            
            //FIXME - hacks end here
            
            var response = ctx.response();

            response.setStatusCode(201);        // created
            response.putHeader("content-type", "application/ld+json");
            response.end(signed.toString());

        } catch (JsonLdError | DataError | IllegalStateException | SigningError e) {
            ctx.fail(e);
        }
    }

}
