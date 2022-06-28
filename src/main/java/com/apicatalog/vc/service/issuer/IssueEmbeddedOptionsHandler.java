package com.apicatalog.vc.service.issuer;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.signature.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class IssueEmbeddedOptionsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        Ed25519ProofOptions2020 proofOptions = new Ed25519ProofOptions2020();
        proofOptions.setVerificationMethod(new Ed25519VerificationKey2020(URI.create("https://vc.apicatalog.com/key/test.json")));
        
        if (options != null) {

            var domain = options.getString(Constants.OPTION_DOMAIN);

            if (domain != null) {
                proofOptions.setDomain(domain);
            }

            Instant created = options.getInstant(Constants.OPTION_CREATED);

            if (created != null) {
                proofOptions.setCreated(created);
            }
        }

        ctx.put(Constants.OPTIONS, proofOptions);
        ctx.next();
    }
}
