package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class IssueEdDSAJcs2022 extends DataIntegrityIssueFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final EdDSAJcs2022Suite SUITE = new EdDSAJcs2022Suite();
//
//    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();
//
//    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);
//
//    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
//
//    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
//            .setDatabaseId("iron-vc-demo")
//            .build()
//            .getService();
//
//    static final HttpClientApi clientApi = new HttpClientApi();
//
//    public IssueEdDSAJcs2022() {
//        super(ISSUER, STORAGE, DB, VERIFICATION_METHOD);
//    }
//
//    @Override
//    protected JsonStructure getStatus() throws HttpFunctionError {
//        try {
//            var json = clientApi.callApi("https://us-central1-api-catalog.cloudfunctions.net/statusGetIndex", "POST", "{\"purpose\":\"revocation\"}");
//
//            return parseJson(new StringReader(json));
//
//        } catch (Exception e) {
//            throw new HttpFunctionError(e, "InvalidDocument");
//        }
//    }
}
