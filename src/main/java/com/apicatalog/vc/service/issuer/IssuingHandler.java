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
import com.apicatalog.vc.integrity.DataIntegrityProofDraft;
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

            final ProofDraft proofDraft = getDraft(options);

            var signed = getIssuer(options)
                    .sign(
                            JsonDocument
                                    .of(new StringReader(document.toString()))
                                    .getJsonContent()
                                    .orElseThrow(IllegalStateException::new)
                                    .asJsonObject(),
                            proofDraft)
                    .compacted();

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

        if (IssuerOptions.ECDSA_RDFC_2019.equals(options.cryptosuite())) {
            
            DataIntegrityProofDraft draft = null;
            
            if (IssuerOptions.P384.equalsIgnoreCase(options.curve())) {
                draft = Suites.ECDSA_RDFC_2019
                        .createP384Draft(
                                KeyProvider.getP384Method(),
                                URI.create("https://w3id.org/security#assertionMethod"));            
            } else {
                draft = Suites.ECDSA_RDFC_2019
                    .createP256Draft(
                            KeyProvider.getP256Method(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            }
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            return draft;

        } else if (IssuerOptions.EDDSA_RDFC_2022.equals(options.cryptosuite())) {
            var draft = Suites.EDDSA_RDFC_2022
                    .createDraft(
                            KeyProvider.getEdDsaMethod(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            return draft;

        } else if (IssuerOptions.ECDSA_SD_2023.equals(options.cryptosuite())) {
            var draft = Suites.ECDSA_SD_2023
                    .createP256Draft(
                            KeyProvider.getP256Method(),
                            URI.create("https://w3id.org/security#assertionMethod"));
            draft.created(options.created());
            draft.domain(options.domain());
            draft.challenge(options.challenge());
            draft.selectors(options.mandatoryPointers());
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

        if (IssuerOptions.ECDSA_RDFC_2019.equalsIgnoreCase(options.cryptosuite())) {
            if (IssuerOptions.P384.equalsIgnoreCase(options.curve())) {
                return Suites.ECDSA_RDFC_2019.createIssuer(KeyProvider.getP384Keys());
            }
            return Suites.ECDSA_RDFC_2019.createIssuer(KeyProvider.getP256Keys());

        } else if (IssuerOptions.EDDSA_RDFC_2022.equalsIgnoreCase(options.cryptosuite())) {
            return Suites.EDDSA_RDFC_2022.createIssuer(KeyProvider.getEdDSAKeys());

        } else if (IssuerOptions.ECDSA_SD_2023.equalsIgnoreCase(options.cryptosuite())) {
            return Suites.ECDSA_SD_2023.createIssuer(KeyProvider.getP256Keys());
        }

        return Suites.ED25519_2020.createIssuer(KeyProvider.getP256Keys());

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
