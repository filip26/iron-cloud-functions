package com.apicatalog.vc.service.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

class IssueEmbeddedOptionsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // verification key
        Ed25519VerificationKey2020 verificationKey = new Ed25519VerificationKey2020(
        		URI.create("did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y#z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y"), 
        		null,  
        		null, 
        		Multicodec.decode(Multicodec.Type.Key, Multibase.decode("z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y"))
        		);
        
        // default values
        ProofOptions proofOptions = 
        	Ed25519Signature2020
        		.createOptions(
        			verificationKey,
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
