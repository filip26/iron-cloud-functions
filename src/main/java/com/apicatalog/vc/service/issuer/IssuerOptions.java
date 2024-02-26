package com.apicatalog.vc.service.issuer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public record IssuerOptions(
        String cryptosuite,
        Instant created,
        String domain,
        String challenge,
        Collection<String> mandatoryPointers
        ) {
    
    static final IssuerOptions getOptions(RoutingContext ctx) throws DocumentError {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String domain = null;
        String challenge = null;
        Collection<String> mandatoryPointers = null; 

        // suite name
        String suiteName = "Ed25519Signature2020";

        // request options
        if (options != null) {
            suiteName = options.getString(Constants.OPTION_TYPE, suiteName);
            created = options.getInstant(Constants.OPTION_CREATED, created);
            domain = options.getString(Constants.OPTION_DOMAIN, null);
            challenge = options.getString(Constants.OPTION_CHALLENGE, null);
            mandatoryPointers = getPointers(options.getJsonArray(Constants.OPTION_MANDATORY_POINTERS));
            
            var unknown = options.stream()
                    .filter(e -> !Constants.OPTIONS_KEYS.contains(e.getKey()))
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));

            if (unknown != null && !unknown.isBlank()) {
                System.out.println("UNKNOWN OPTIONS [" + unknown + "]");
            }
        }

        return new IssuerOptions(suiteName, created, domain, challenge, mandatoryPointers);
    }

    protected static Collection<String> getPointers(JsonArray input) {
        if (input == null) {
            return null;
        }
        
        var pointers = new ArrayList<String>(input.size());
        
        for (int i=0; i < input.size(); i++) {
            pointers.add(input.getString(i));
        }
        return pointers;
    }
    
}
