package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class DeriveFunction extends HttpJsonFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final DocumentReader READER = DocumentReader
//            .with(new ECDSASD2023Suite())
//            .loader(LOADER);
//
//    public DeriveFunction() {
//        super("POST", HttpURLConnection.HTTP_CREATED);
//    }
//
//    @Override
//    protected JsonObject process(final JsonObject json) throws HttpFunctionError {
//
//        JsonObject derived = null;
//
//        try {
//            var request = DeriveRequest.from(json);
//
//            VerifiableDocument document = READER.read(request.credential());
//
//            if (document.proofs().isEmpty()) {
//                throw new HttpFunctionError("NoProof");
//            }
//
//            Proof proof = document.proofs().iterator().next();
//
//            derived = proof.derive(request.selectivePointers());
//
//        } catch (DocumentError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));
//
//        } catch (CryptoSuiteError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));
//        }
//        return derived;
//    }

}
