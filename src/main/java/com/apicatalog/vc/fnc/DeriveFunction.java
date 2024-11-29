package com.apicatalog.vc.fnc;

import java.net.HttpURLConnection;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.ecdsa.sd.ECDSASD2023Suite;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.DocumentReader;
import com.google.cloud.functions.HttpFunction;

import jakarta.json.JsonObject;

public class DeriveFunction extends HttpJsonFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final DocumentReader READER = DocumentReader
            .with(new ECDSASD2023Suite())
            .loader(LOADER);

    public DeriveFunction() {
        super("POST", HttpURLConnection.HTTP_CREATED);
    }

    @Override
    protected JsonObject process(final JsonObject json) throws HttpFunctionError {

        JsonObject derived = null;

        try {
            var request = DeriveRequest.of(json);

            VerifiableDocument document = READER.read(request.credential());

            if (document.proofs().isEmpty()) {
                throw new HttpFunctionError("NoProof");
            }

            Proof proof = document.proofs().iterator().next();

            derived = proof.derive(request.selectivePointers());

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (CryptoSuiteError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));
        }
        return derived;
    }

}
