package com.apicatalog.vc.service.issuer;

import java.net.URI;

import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.ecdsa.ECDSAKeyPair2019;
import com.apicatalog.ld.signature.ecdsa.ECDSAVerificationKey2019;
import com.apicatalog.ld.signature.ed25519.Ed25519KeyPair2020;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020;
import com.apicatalog.ld.signature.eddsa.EdDSAKeyPair2022;
import com.apicatalog.ld.signature.eddsa.EdDSAVerificationKey2022;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;

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
            return new EdDSAKeyPair2022(
                    ED_KEYPAIR_ID,
                    null,
                    null,
                    Multicodec.decode(Multicodec.Type.Key, Multibase.decode(ED_PUBLIC_KEY)),
                    Multicodec.decode(Multicodec.Type.Key, Multibase.decode(ED_PRIVATE_KEY)));            
        }

        if ("ecdsa-2019".equals(suite)) {
            return new ECDSAKeyPair2019(
                    EC_KEYPAIR_ID,
                    null,
                    null,
                    Multicodec.decode(Multicodec.Type.Key, Multibase.decode(EC_PUBLIC_KEY)),
                    Multicodec.decode(Multicodec.Type.Key, Multibase.decode(EC_PRIVATE_KEY)));            
        }

        return new Ed25519KeyPair2020(
                ED_KEYPAIR_ID,
                null,
                null,
                Multicodec.decode(Multicodec.Type.Key, Multibase.decode(ED_PUBLIC_KEY)),
                Multicodec.decode(Multicodec.Type.Key, Multibase.decode(ED_PRIVATE_KEY)));

    }
    
    public static VerificationMethod getEd25519Method() {
        // verification key
        return new Ed25519VerificationKey2020(
                ED_VERIFICATION_KEY,
                null,
                null,
                null);
    }
    
    public static VerificationMethod getEdDsaMethod() {
        // verification key
        return new EdDSAVerificationKey2022(
                ED_VERIFICATION_KEY,
                null,
                null,
                null);
    }
    
    public static VerificationMethod getEcDsaMethod() {
        // verification key
        return new ECDSAVerificationKey2019(
                EC_VERIFICATION_KEY,
                null,
                null,
                null);
    }
}
