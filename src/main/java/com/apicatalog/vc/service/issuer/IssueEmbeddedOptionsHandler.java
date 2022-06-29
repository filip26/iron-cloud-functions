package com.apicatalog.vc.service.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.ld.signature.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.ld.signature.proof.VerificationMethodReference;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class IssueEmbeddedOptionsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        Ed25519ProofOptions2020 proofOptions = new Ed25519ProofOptions2020();
        
        // default values
//        proofOptions.setVerificationMethod(new VerificationMethodReference(URI.create("https://vc.apicatalog.com/key/test.json")));      
        proofOptions.setVerificationMethod(new VerificationMethodReference(URI.create("did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y")));
        proofOptions.setPurpose(URI.create("https://w3id.org/security#assertionMethod"));
        proofOptions.setCreated(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        
        // recieved options
        if (options != null) {

            var domain = options.getString(Constants.OPTION_DOMAIN);

            if (domain != null) {
                proofOptions.setDomain(domain);
            }

            proofOptions.setCreated(options.getInstant(Constants.OPTION_CREATED, proofOptions.getCreated()));
        }

        ctx.put(Constants.OPTIONS, proofOptions);
        ctx.next();
    }
}
