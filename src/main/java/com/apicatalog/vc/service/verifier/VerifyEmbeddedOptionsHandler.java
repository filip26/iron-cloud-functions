package com.apicatalog.vc.service.verifier;

import com.apicatalog.vc.service.Constants;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class VerifyEmbeddedOptionsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        if (options != null) {

            var domain = options.getString(Constants.OPTION_DOMAIN);

            if (domain != null) {
                ctx.put(Constants.OPTION_DOMAIN, domain);
            }

            var challenge = options.getString(Constants.OPTION_CHALLENGE);

            if (challenge != null) {
                ctx.put(Constants.OPTION_CHALLENGE, challenge);
            }
        }

        ctx.next();
    }

}
