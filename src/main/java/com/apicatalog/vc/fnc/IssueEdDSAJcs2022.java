package com.apicatalog.vc.fnc;

import java.io.StringReader;
import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.eddsa.EdDSAJcs2022Suite;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.json.JsonStructure;

public class IssueEdDSAJcs2022 extends DataIntegrityIssueFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final EdDSAJcs2022Suite SUITE = new EdDSAJcs2022Suite();

    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();

    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);

    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
            .setDatabaseId("iron-vc-demo")
            .build()
            .getService();

    static final HttpClientApi clientApi = new HttpClientApi();

    public IssueEdDSAJcs2022() {
        super(ISSUER, STORAGE, DB, VERIFICATION_METHOD);
    }

    @Override
    protected JsonStructure getStatus() throws HttpFunctionError {
        try {
            var json = clientApi.callApi("https://us-central1-api-catalog.cloudfunctions.net/statusGetIndex", "POST", "{\"purpose\":\"revocation\"}");

            return parseJson(new StringReader(json));

        } catch (Exception e) {
            throw new HttpFunctionError(e, "InvalidDocument");
        }
    }
}
