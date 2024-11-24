package com.apicatalog.vc.fnc;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;

/**
 * For demo purposes only. Do not use in a production.
 */
public class P384KeyPairProvider {

    public static final KeyPair getKeyPair() {

        var publicKey = "z82LkuBieyGShVBhvtE2zoiD6Kma4tJGFtkAhxR5pfkp5QPw4LutoYWhvQCnGjdVn14kujQ";
        var privateKey = System.getenv("P384_PRIVATE_KEY_TEST_1");

        Objects.requireNonNull(privateKey);

        var id = URI.create("did:key:" + publicKey + "#" + publicKey);

        return GenericMultikey.of(
                id,
                URI.create("did:key:" + publicKey),
                new GenericMulticodecKey(
                        KeyCodec.P384_PUBLIC_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.P384_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(publicKey))),
                new GenericMulticodecKey(
                        KeyCodec.P384_PRIVATE_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.P384_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(privateKey))));
    }

    public static final URI getVerificationMethod() {
        var publicKey = "z82LkuBieyGShVBhvtE2zoiD6Kma4tJGFtkAhxR5pfkp5QPw4LutoYWhvQCnGjdVn14kujQ";
        return URI.create("did:key:" + publicKey + "#" + publicKey);
    }
}
