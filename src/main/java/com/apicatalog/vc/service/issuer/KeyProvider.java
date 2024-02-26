package com.apicatalog.vc.service.issuer;

import java.net.URI;

import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multikey.MultiKey;

/* PLEASE DO NOT USE IN PRODUCTION 
 * only to demonstrate
 */
public class KeyProvider {

    static final URI ED_KEYPAIR_ID = URI.create("did:key:" + System.getProperty("VC_PUBLIC_KEY", System.getenv("VC_PUCLIC_KEY")));
    static final String ED_PUBLIC_KEY = System.getProperty("VC_PUBLIC_KEY", System.getenv("VC_PUBLIC_KEY"));
    static final String ED_PRIVATE_KEY = System.getProperty("VC_PRIVATE_KEY", System.getenv("VC_PRIVATE_KEY"));

    static final URI ED_VERIFICATION_KEY = URI.create(System.getProperty("VC_VERIFICATION_KEY", System.getenv("VC_VERIFICATION_KEY")));

    static final URI EC_KEYPAIR_ID = URI.create("did:key:" + System.getProperty("EC_PUBLIC_KEY", System.getenv("EC_PUCLIC_KEY")));
    static final String EC_PUBLIC_KEY = System.getProperty("EC_PUBLIC_KEY", System.getenv("EC_PUBLIC_KEY"));
    static final String EC_PRIVATE_KEY = System.getProperty("EC_PRIVATE_KEY", System.getenv("EC_PRIVATE_KEY"));

    static final URI EC_VERIFICATION_KEY = URI.create(System.getProperty("EC_VERIFICATION_KEY", System.getenv("EC_VERIFICATION_KEY")));

    public static KeyPair getKeyPair(String suite) {
        if ("eddsa-2022".equals(suite)) {
            var key = new MultiKey();
            key.setId(ED_KEYPAIR_ID);
            key.setPublicKey(KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PUBLIC_KEY)));
            key.setPrivateKey(KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PRIVATE_KEY)));
            return key;
        }

        if ("ecdsa-2019".equals(suite)) {
            var key = new MultiKey();
            key.setId(EC_KEYPAIR_ID);
            key.setPublicKey(KeyCodec.P256_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(EC_PUBLIC_KEY)));
            key.setPrivateKey(KeyCodec.P256_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(EC_PRIVATE_KEY)));
            return key;
        }

        return new Ed25519KeyPair2020(
                ED_KEYPAIR_ID,
                null,
                null,
                KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PUBLIC_KEY)),
                KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PRIVATE_KEY)));
    }

    public static VerificationMethod getEd25519Method() {
        // verification key
        return new Ed25519KeyPair2020(
                ED_VERIFICATION_KEY,
                null,
                null,
                null,
                null);
    }

    public static URI getEdDsaMethod() {
        return ED_VERIFICATION_KEY;
    }

    public static URI getEcDsaMethod() {
        return EC_VERIFICATION_KEY;
    }
}
