package com.apicatalog.vc.service.issuer;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.issuer.ProofDraft;
import com.apicatalog.vc.service.Constants;
import com.apicatalog.vc.service.Suites;

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
            var options = IssuerOptions.getOptions(ctx);

            final ProofDraft proofOptions = getDraft(options);

            var signed = getIssuer(options)
                    .sign(
                            JsonDocument
                                    .of(new StringReader(document.toString()))
                                    .getJsonContent()
                                    .orElseThrow(IllegalStateException::new)
                                    .asJsonObject(),
                            proofOptions).compacted();

            signed = applyHacks(signed);

            var response = ctx.response();

            response.setStatusCode(201); // created
            response.putHeader("content-type", "application/ld+json");
            response.end(signed.toString());

        } catch (JsonLdError | DocumentError | IllegalStateException | SigningError e) {
            ctx.fail(e);
        } catch (KeyGenError e) {
            ctx.fail(e);
        }
    }

    static final ProofDraft getDraft(IssuerOptions options) throws DocumentError, KeyGenError {

        if ("ecdsa-2019".equals(options.cryptosuite())) {
            var draft = Suites.ECDSA_RDFC_2019
                    .createP256Draft(
                            KeyProvider.getEcDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            return draft;

        } else if ("eddsa-2022".equals(options.cryptosuite())) {
            var draft = Suites.EDDSA_RDFC_2022
                    .createDraft(
                            KeyProvider.getEdDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            return draft;

        } else if ("ecdsa-sd-2023".equals(options.cryptosuite())) {
            var draft = Suites.ECDSA_SD_2023
                    .createP256Draft(
                            KeyProvider.getEcDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            draft.useGeneratedHmacKey(32);
            draft.useGeneratedProofKeys();
            return draft;
        }

        var draft = Ed25519Signature2020
                .createDraft(
                        KeyProvider.getEd25519Method(),
                        URI.create("https://w3id.org/security#assertionMethod"));

        draft.created(options.created());
        draft.domain(options.domain());
        draft.challenge(options.challenge());
        return draft;
    }

    static final Issuer getIssuer(IssuerOptions options) throws DocumentError {

        if ("ecdsa-2019".equals(options.cryptosuite())) {
            return Suites.ECDSA_RDFC_2019.createIssuer(KeyProvider.getECDSA256Keys());

        } else if ("eddsa-2022".equals(options.cryptosuite())) {
            return Suites.EDDSA_RDFC_2022.createIssuer(KeyProvider.getEdDSAKeys());

        } else if ("ecdsa-sd-2023".equals(options.cryptosuite())) {
            return Suites.ECDSA_SD_2023.createIssuer(KeyProvider.getECDSA256Keys());
        }

        return Suites.ED25519_2020.createIssuer(KeyProvider.getECDSA256Keys());

    }

    static final JsonObject applyHacks(final JsonObject signed) {

        var document = Json.createObjectBuilder(signed);

        if (JsonUtils.isString(signed.get("credentialSubject"))) {
            document = document
                    .add("credentialSubject",
                            Json.createObjectBuilder()
                                    .add("id", signed.getString("credentialSubject")));
        }

        return document.build();
    }
}
