package com.apicatalog.vc.fnc.legacy;

import com.google.cloud.functions.HttpFunction;

public class VerifyFunction extends HttpJsonFunction implements HttpFunction {

//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final Verifier VERIFIER = Verifier.with(
//            new EdDSARdfc2022Suite(),
//            new EdDSAJcs2022Suite(),
//            new ECDSARdfc2019Suite(),
//            new ECDSAJcs2019Suite(),
//            new Ed25519Signature2020(),
//            new ECDSASD2023Suite())
//            .methodResolver(defaultResolvers(LOADER))
//            .loader(LOADER);
//
//    public VerifyFunction() {
//        super("POST", HttpURLConnection.HTTP_OK);
//    }
//
//    @Override
//    protected JsonObject process(JsonObject json) throws HttpFunctionError {
//        try {
//            var request = VerificationRequest.from(json);
//
//            var params = new HashMap<String, Object>();
//            params.put(VcdiVocab.PURPOSE.name(), request.purpose());
//            params.put(VcdiVocab.CHALLENGE.name(), request.challenge());
//            params.put(VcdiVocab.DOMAIN.name(), request.domain());
//            params.put(VcdiVocab.NONCE.name(), request.nonce());
//
//            var verifiable = VERIFIER.verify(request.verifiable(), params);
//
//            if (verifiable == null) {
//                throw new HttpFunctionError(HttpFunctionError.toString(VerificationErrorCode.InvalidSignature.name()));
//            }
//
//            return write(verifiable);
//
//        } catch (DocumentError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));
//
//        } catch (VerificationError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));
//        }
//    }
//
//    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {
//        return MethodSelector.create()
//                // accept did:key
//                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
//                        ControllableKeyProvider.from(new DidKeyResolver(MulticodecDecoder.getInstance(Tag.Key))))
//
//                .build();
//    }
//
//    static final JsonObject write(VerifiableDocument verifiable) {
//        var result = JSON.createObjectBuilder()
//                .add("verified", JsonValue.TRUE)
//                .add("type", JSON.createArrayBuilder(verifiable.type()));
//
//        if (verifiable.id() != null) {
//            result.add("id", verifiable.id().toString());
//        }
//
//        var proofs = JSON.createArrayBuilder();
//
//        verifiable.proofs().forEach(proof -> {
//
//            var record = JSON.createObjectBuilder()
//                    .add("type", JSON.createArrayBuilder(proof.type()));
//
//            if (proof.cryptosuite() != null) {
//                record.add("cryptosuite", proof.cryptosuite().name())
//                        .add("keyLength", proof.cryptosuite().keyLength());
//            }
//
//            proofs.add(record);
//        });
//
//        result.add("proofs", proofs);
//
//        return result.build();
//    }

}
