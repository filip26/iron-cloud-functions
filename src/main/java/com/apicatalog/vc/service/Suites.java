package com.apicatalog.vc.service;

import com.apicatalog.ld.signature.ecdsa.ECDSASignature2019;
import com.apicatalog.ld.signature.ecdsa.sd.ECDSASelective2023;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSASignature2022;
import com.apicatalog.vc.suite.SignatureSuite;

public class Suites {

    public static Ed25519Signature2020 ED25519_2020 = new Ed25519Signature2020();
    
    public static EdDSASignature2022 EDDSA_RDFC_2022 = new EdDSASignature2022();
    
    public static ECDSASignature2019 ECDSA_RDFC_2019 = new ECDSASignature2019();
    
    public static ECDSASelective2023 ECDSA_SD_2023 = new ECDSASelective2023();

    public static SignatureSuite[] ALL = {
            ED25519_2020,
            EDDSA_RDFC_2022,
            ECDSA_RDFC_2019,
            ECDSA_SD_2023,
    };
    
}
