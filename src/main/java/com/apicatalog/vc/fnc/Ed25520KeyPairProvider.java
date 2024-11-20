package com.apicatalog.vc.fnc;

import java.net.URI;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;

/**
 * For demo purposes only. Do not use in a production.
 */
public class Ed25520KeyPairProvider {

    public static final KeyPair getKeyPair() {

        var publicKey = "z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7";
        var privateKey = System.getenv("ED_PRIVATE_KEY_TEST_1");

        var id = URI.create("did:key:" + publicKey + "#" + publicKey);

        return GenericMultikey.of(
                id,
                URI.create("did:key:" + publicKey),
                new GenericMulticodecKey(
                        KeyCodec.ED25519_PUBLIC_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(publicKey))),
                new GenericMulticodecKey(
                        KeyCodec.ED25519_PRIVATE_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(privateKey))));
    }

    public static final URI getVerificationMethod() {
        var publicKey = "z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7";
        return URI.create("did:key:" + publicKey + "#" + publicKey);
    }
}
