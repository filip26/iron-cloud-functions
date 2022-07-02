package com.apicatalog.vc.service.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class IssueEmbeddedOptionsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        ProofOptions proofOptions = 
        	Ed25519Signature2020
        		.createOptions(
        			new VerificationKey(URI.create("did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y#z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y")),
        			URI.create("https://w3id.org/security#assertionMethod")
        			)
        		.created(Instant.now().truncatedTo(ChronoUnit.SECONDS));

        // recieved options
        if (options != null) {

            var domain = options.getString(Constants.OPTION_DOMAIN);

            if (domain != null) {
                proofOptions.domain(domain);
            }

            proofOptions.created(options.getInstant(Constants.OPTION_CREATED, proofOptions.created()));
        }

        ctx.put(Constants.OPTIONS, proofOptions);
        ctx.next();
    }
}
