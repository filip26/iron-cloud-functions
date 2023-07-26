package com.apicatalog.vc.service.issuer;

import java.io.StringReader;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.ecdsa.ECDSASignature2019;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSASignature2022;
import com.apicatalog.vc.Vc;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.ProofValueProcessor;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;

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
            ctx.fail(new DocumentError(ErrorType.Invalid));
            return;
        }
        
        try {
            final Proof proofOptions = getOptions(ctx);
        
            var signed = Vc.sign(
                    JsonDocument
                            .of(new StringReader(document.toString()))
                            .getJsonContent()
                            .orElseThrow(IllegalStateException::new)
                            .asJsonObject(),
                    KeyProvider.getKeyPair(proofOptions.getCryptoSuite().getId()),
                    proofOptions)
                    .getCompacted();

            // FIXME, remove, hack to pass the testing suite
     //       signed = applyHacks(signed);

            var response = ctx.response();

            response.setStatusCode(201); // created
            response.putHeader("content-type", "application/ld+json");
            response.end(signed.toString());

        } catch (JsonLdError | DocumentError | IllegalStateException | SigningError e) {
            e.printStackTrace();
            ctx.fail(e);
        }
    }

    static final Proof getOptions(RoutingContext ctx) throws DocumentError {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String domain = null;
        String challenge = null;

        // suite name
        String suiteName = "Ed25519Signature2020";

        // request options
        if (options != null) {
            suiteName = options.getString(Constants.OPTION_TYPE, suiteName);
            created = options.getInstant(Constants.OPTION_CREATED, created);
            domain = options.getString(Constants.OPTION_DOMAIN, null);
            challenge = options.getString(Constants.OPTION_CHALLENGE, null);
        }

        if ("ecdsa-2019".equals(suiteName)) {
            return new ECDSASignature2019()
                    .createDraft(
                            KeyProvider.getEcDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"),
                            created,
                            domain,
                            challenge);

        } else if ("eddsa-2022".equals(suiteName)) {
            return new EdDSASignature2022()
                    .createDraft(
                            KeyProvider.getEdDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"),
                            created,
                            domain,
                            challenge);
        }

        return Ed25519Signature2020
                .createDraft(
                        KeyProvider.getEd25519Method(),
                        URI.create("https://w3id.org/security#assertionMethod"),
                        created,
                        domain,
                        challenge);
    }

    // FIXME, remove
    static final JsonObject applyHacks(final JsonObject signed) {

        var document = Json.createObjectBuilder(signed);

        var proof = signed.getJsonObject("proof");

        if (JsonUtils.isObject(proof.get("verificationMethod"))) {
            proof = Json.createObjectBuilder(proof)
                    .add("verificationMethod",
                            proof.getJsonObject("verificationMethod")
                                    .getString("id"))
                    .build();
        }

        if (JsonUtils.isString(signed.get("credentialSubject"))) {
            document = document
                    .add("credentialSubject",
                            Json.createObjectBuilder()
                                    .add("id", signed.getString("credentialSubject")));
        }

        if (JsonUtils.isNotNull(signed.get("cred:issuanceDate"))) {
            document = document
                    .add("issuanceDate", signed.get("cred:issuanceDate"))
                    .remove("cred:issuanceDate");
        }

        return document.build();
    }
}
