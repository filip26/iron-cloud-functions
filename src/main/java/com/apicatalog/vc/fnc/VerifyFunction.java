package com.apicatalog.vc.fnc;

import java.net.HttpURLConnection;
import java.util.HashMap;

import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.ecdsa.ECDSAJcs2019Suite;
import com.apicatalog.ld.signature.ecdsa.ECDSARdfc2019Suite;
import com.apicatalog.ld.signature.ecdsa.sd.ECDSASD2023Suite;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSAJcs2022Suite;
import com.apicatalog.ld.signature.eddsa.EdDSARdfc2022Suite;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.di.VcdiVocab;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.ControllableKeyProvider;
import com.apicatalog.vc.method.MethodPredicate;
import com.apicatalog.vc.method.MethodSelector;
import com.apicatalog.vc.method.VerificationKeyProvider;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.verifier.Verifier;
import com.google.cloud.functions.HttpFunction;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class VerifyFunction extends HttpJsonFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final Verifier VERIFIER = Verifier.with(
            new EdDSARdfc2022Suite(),
            new EdDSAJcs2022Suite(),
            new ECDSARdfc2019Suite(),
            new ECDSAJcs2019Suite(),
            new Ed25519Signature2020(),
            new ECDSASD2023Suite())
            .methodResolver(defaultResolvers(LOADER))
            .loader(LOADER);

    public VerifyFunction() {
        super("POST", HttpURLConnection.HTTP_OK);
    }

    @Override
    protected JsonObject process(JsonObject json) throws HttpFunctionError {

        try {
            var request = VerificationRequest.of(json);

            var params = new HashMap<String, Object>();
            params.put(VcdiVocab.PURPOSE.name(), request.purpose());
            params.put(VcdiVocab.CHALLENGE.name(), request.challenge());
            params.put(VcdiVocab.DOMAIN.name(), request.domain());
            params.put(VcdiVocab.NONCE.name(), request.nonce());

            var verifiable = VERIFIER.verify(request.verifiable(), params);

            if (verifiable == null) {
                throw new HttpFunctionError(HttpFunctionError.toString(VerificationErrorCode.InvalidSignature.name()));
            }

            return write(verifiable);

        } catch (DocumentError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));

        } catch (VerificationError e) {
            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));
        }
    }

    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {
        return MethodSelector.create()
                // accept did:key
                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
                        ControllableKeyProvider.of(new DidKeyResolver(MulticodecDecoder.getInstance(Tag.Key))))

                .build();
    }

    static final JsonObject write(VerifiableDocument verifiable) {
        var result = JSON.createObjectBuilder()
                .add("verified", JsonValue.TRUE)
                .add("type", JSON.createArrayBuilder(verifiable.type()));

        if (verifiable.id() != null) {
            result.add("id", verifiable.id().toString());
        }

        var proofs = JSON.createArrayBuilder();

        verifiable.proofs().forEach(proof -> {
            proofs.add(JSON.createObjectBuilder()
                    .add("type", JSON.createArrayBuilder(proof.type()))
                    .add("cryptosuite", proof.cryptosuite().name())
                    .add("keyLength", proof.cryptosuite().keyLength()));
        });

        result.add("proofs", proofs);

        return result.build();
    }

}
