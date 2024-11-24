package com.apicatalog.vc.fnc;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.ecdsa.ECDSARdfc2019Suite;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class IssueECDSARdfc2019P256 extends DataIntegrityIssueFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final SignatureSuite SUITE = new ECDSARdfc2019Suite();

    static final URI VERIFICATION_METHOD = P256KeyPairProvider.getVerificationMethod();

    static final Issuer ISSUER = SUITE.createIssuer(P256KeyPairProvider.getKeyPair()).loader(LOADER);

    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
            .setDatabaseId("iron-vc-demo")
            .build()
            .getService();

    public IssueECDSARdfc2019P256() {
        super(ISSUER, STORAGE, DB, VERIFICATION_METHOD);
    }
}
