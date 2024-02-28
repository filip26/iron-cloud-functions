package com.apicatalog.vc.service.verifier;

import com.apicatalog.vc.service.Constants;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

public class VerifierApi  {

    public static void setup(Router router) throws Exception {

        router
            .post("/credentials/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.VERIFIABLE_CREDENTIAL_KEY)
            .putMetadata(Constants.CTX_STRICT, true)

            //TODO validation
            
            //FIXME remove
            .handler(ctx -> {
                System.out.println(ctx.body().asString());
                ctx.next();
            })

            // options
            .handler(new VerifyEmbeddedOptionsHandler())

            // verify
            .blockingHandler(new VerificationHandler())
            
            // handle errors
            .failureHandler(new VerifierErrorHandler());

        router
            .post("/presentations/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.VERIFIABLE_PRESENTATION_KEY)
            .putMetadata(Constants.CTX_STRICT, true)

            //TODO validation

            // options
            .handler(new VerifyEmbeddedOptionsHandler())

            // verify
            .blockingHandler(new VerificationHandler())

            // handle errors
            .failureHandler(new VerifierErrorHandler());


        router
            .post("/verify")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/json")
            .putMetadata(Constants.CTX_STRICT, false)

            // options
            .handler(ctx -> {
                final RequestParameters parameters = ctx.get(ValidationHandler.REQUEST_CONTEXT_KEY);

                var domain = parameters.queryParameter(Constants.OPTION_DOMAIN);

                if (domain != null) {
                    ctx.put(Constants.OPTION_DOMAIN, domain.getString());
                }

                var challenge = parameters.queryParameter(Constants.OPTION_CHALLENGE);

                if (challenge != null) {
                    ctx.put(Constants.OPTION_CHALLENGE, challenge.getString());
                }

                ctx.next();
            })

            // verify
            .blockingHandler(new VerificationHandler())

            // handle errors
            .failureHandler(new VerifierErrorHandler());
    }
}
