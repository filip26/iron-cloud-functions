package com.apicatalog.vc.service;

import com.apicatalog.ld.signature.ecdsa.ECDSASignature2019;
import com.apicatalog.ld.signature.ecdsa.sd.ECDSASD2023Suite;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSASignature2022;
import com.apicatalog.vc.suite.SignatureSuite;

public class Suites {

    public static Ed25519Signature2020 ED25519_2020_SUITE = new Ed25519Signature2020();
    
    public static EdDSASignature2022 EDDSA_RDFC_2022_SUITE = new EdDSASignature2022();
    
    public static ECDSASignature2019 ECDSA_RDFC_2019_SUITE = new ECDSASignature2019();
    
    public static ECDSASD2023Suite ECDSA_SD_2023 = new ECDSASD2023Suite();
    
    public static SignatureSuite[] ALL = {
            ED25519_2020_SUITE,
            EDDSA_RDFC_2022_SUITE,
            ECDSA_RDFC_2019_SUITE,
            ECDSA_SD_2023,
    };
    
}
