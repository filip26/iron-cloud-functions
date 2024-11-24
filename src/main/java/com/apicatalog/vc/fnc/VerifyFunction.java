package com.apicatalog.vc.fnc;

import java.io.BufferedWriter;
import java.util.Map.Entry;

import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.ecdsa.ECDSAJcs2019Suite;
import com.apicatalog.ld.signature.ecdsa.ECDSARdfc2019Suite;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020;
import com.apicatalog.ld.signature.eddsa.EdDSAJcs2022Suite;
import com.apicatalog.ld.signature.eddsa.EdDSARdfc2022Suite;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.resolver.ControllableKeyProvider;
import com.apicatalog.vc.method.resolver.MethodPredicate;
import com.apicatalog.vc.method.resolver.MethodSelector;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.apicatalog.vc.verifier.Verifier;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class VerifyFunction implements HttpFunction {

    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());

    static final Verifier VERIFIER = Verifier.with(
            new EdDSARdfc2022Suite(),
            new EdDSAJcs2022Suite(),
            new ECDSARdfc2019Suite(),
            new ECDSAJcs2019Suite(),
            new Ed25519Signature2020())
            .methodResolver(defaultResolvers(LOADER))
            .loader(LOADER);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        String contentType = request.getContentType().orElseThrow();

//       Verifier verifier = Verifier.with(null);
//       
//       
//
//        try (JsonParser parser = Json.createParser(request.getInputStream())) {
//            parser.next();
//            JsonValue jsonValue = parser.getValue();
//            
//            verifier.verify(jsonValue.asJsonObject());     
//        } catch (VerificationError | DocumentError e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        response.setStatusCode(200);
        response.setContentType("text/plain");
        BufferedWriter writer = response.getWriter();
        writer.write("Test");
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            writer.write(entry.getKey() + " -> " + entry.getValue());
        }
    }

    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {
        return MethodSelector.create()
                // accept did:key
                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
                        ControllableKeyProvider.of(new DidKeyResolver(MulticodecDecoder.getInstance(Tag.Key))))

                .build();
    }

}
