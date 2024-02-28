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

    static final URI ED_KEYPAIR_ID = URI.create("did:key:" + getProperty("ED_PUBLIC_KEY") + "#" +  getProperty("ED_PUBLIC_KEY"));
    static final String ED_PUBLIC_KEY =  getProperty("ED_PUBLIC_KEY");
    static final String ED_PRIVATE_KEY = getProperty("ED_PRIVATE_KEY");

    static final URI P256_KEYPAIR_ID = URI.create("did:key:" + getProperty("P256_PUBLIC_KEY") + "#" + getProperty("P256_PUBLIC_KEY"));
    static final String P256_PUBLIC_KEY = getProperty("P256_PUBLIC_KEY");
    static final String P256_PRIVATE_KEY = getProperty("P256_PRIVATE_KEY");

    static final URI P384_KEYPAIR_ID = URI.create("did:key:" + getProperty("P384_PUBLIC_KEY") + "#" + getProperty("P384_PUBLIC_KEY"));
    static final String P384_PUBLIC_KEY = getProperty("P384_PUBLIC_KEY");
    static final String P384_PRIVATE_KEY = getProperty("P384_PRIVATE_KEY");

    protected static String getProperty(String name) {
        return System.getProperty(name, System.getenv(name));
    }
    
    public static VerificationMethod getEd25519Method() {
        // verification key
        return new Ed25519KeyPair2020(
                ED_KEYPAIR_ID,
                null,
                null,
                null,
                null);
    }

    public static URI getEdDsaMethod() {
        return ED_KEYPAIR_ID;
    }

    public static URI getP256Method() {
        return P256_KEYPAIR_ID;
    }

    public static URI getP384Method() {
        return P384_KEYPAIR_ID;
    }

    public static KeyPair getEdDSAKeys() {
        var key = new MultiKey();
        key.setId(ED_KEYPAIR_ID);
        key.setPublicKey(KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PUBLIC_KEY)));
        key.setPrivateKey(KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PRIVATE_KEY)));
        return key;
    }

    public static KeyPair getP256Keys() {
        var key = new MultiKey();
        key.setId(P256_KEYPAIR_ID);
        key.setPublicKey(KeyCodec.P256_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(P256_PUBLIC_KEY)));
        key.setPrivateKey(KeyCodec.P256_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(P256_PRIVATE_KEY)));
        return key;
    }

    public static KeyPair getP384Keys() {
        var key = new MultiKey();
        key.setId(P384_KEYPAIR_ID);
        key.setPublicKey(KeyCodec.P384_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(P384_PUBLIC_KEY)));
        key.setPrivateKey(KeyCodec.P384_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(P384_PRIVATE_KEY)));
        return key;

    }

    public static KeyPair getEd25519Keys() {
        return new Ed25519KeyPair2020(
                ED_KEYPAIR_ID,
                null,
                null,
                KeyCodec.ED25519_PUBLIC_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PUBLIC_KEY)),
                KeyCodec.ED25519_PRIVATE_KEY.decode(Multibase.BASE_58_BTC.decode(ED_PRIVATE_KEY)));
    }
}
