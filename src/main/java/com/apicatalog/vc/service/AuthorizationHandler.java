package com.apicatalog.vc.service;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.web.RoutingContext;

public class AuthorizationHandler implements Handler<RoutingContext> {

    private Authorization authorization;
    
    public AuthorizationHandler(Authorization authorization) {
        this.authorization = authorization;
    }
    
    @Override
    public void handle(RoutingContext event) {
     
        final var user = event.user();
        
        if (user == null) {
            event.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            return;
        }
        
        final var auths = user.authorizations();
        
        //if (auths == null || auths.)
        
        event.user().isAuthorized(authorization, auth -> {
            
        });
        
        
    }    
}
