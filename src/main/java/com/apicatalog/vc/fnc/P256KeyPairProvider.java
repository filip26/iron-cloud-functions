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
public class P256KeyPairProvider {

    public static final KeyPair getKeyPair() {

        var publicKey = "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP";
        var privateKey = System.getenv("P256_PRIVATE_KEY_TEST_1");

        Objects.requireNonNull(privateKey);

        var id = URI.create("did:key:" + publicKey + "#" + publicKey);

        return GenericMultikey.of(
                id,
                URI.create("did:key:" + publicKey),
                new GenericMulticodecKey(
                        KeyCodec.P256_PUBLIC_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.P256_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(publicKey))),
                new GenericMulticodecKey(
                        KeyCodec.P256_PRIVATE_KEY,
                        Multibase.BASE_58_BTC,
                        KeyCodec.P256_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(privateKey))));
    }

    public static final URI getVerificationMethod() {
        var publicKey = "zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP";
        return URI.create("did:key:" + publicKey + "#" + publicKey);
    }
}
