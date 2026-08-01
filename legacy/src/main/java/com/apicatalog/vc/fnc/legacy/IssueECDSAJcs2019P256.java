package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class IssueECDSAJcs2019P256 extends DataIntegrityIssueFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final SignatureSuite SUITE = new ECDSAJcs2019Suite();
//
//    static final URI VERIFICATION_METHOD = P256KeyPairProvider.getVerificationMethod();
//
//    static final Issuer ISSUER = SUITE.createIssuer(P256KeyPairProvider.getKeyPair()).loader(LOADER);
//
//    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
//
//    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
//            .setDatabaseId("iron-vc-demo")
//            .build()
//            .getService();
//
//    public IssueECDSAJcs2019P256() {
//        super(ISSUER, STORAGE, DB, VERIFICATION_METHOD);
//    }
}
