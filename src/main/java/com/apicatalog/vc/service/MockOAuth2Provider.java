package com.apicatalog.vc.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.oauth2.OAuth2Auth;

public class MockOAuth2Provider implements OAuth2Auth {

    @Override
    public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
        System.out.println("Authenticate");
        System.out.println(credentials);
        
    }

    @Override
    public Future<Void> jWKSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuth2Auth missingKeyHandler(Handler<String> handler) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String authorizeURL(JsonObject params) {
        System.out.println("Authorize");
        System.out.println(params);

        return null;
    }

    @Override
    public Future<User> refresh(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<Void> revoke(User user, String tokenType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<JsonObject> userInfo(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String endSessionURL(User user, JsonObject params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        System.out.println("Close");        
    }    

}
