package com.apicatalog.vc.fnc;

import java.io.BufferedWriter;
import java.util.Map.Entry;

import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.ed25519.Ed25519ContextLoader;
import com.apicatalog.ld.signature.ed25519.Ed25519VerificationKey2020Provider;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.vc.method.resolver.ControllableKeyProvider;
import com.apicatalog.vc.method.resolver.MethodPredicate;
import com.apicatalog.vc.method.resolver.MethodSelector;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class VerifyFunction implements HttpFunction {

    final static DocumentLoader LOADER = new Ed25519ContextLoader();

    final static VerificationKeyProvider RESOLVERS = defaultResolvers(LOADER);

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
                .with(MethodPredicate.methodId(
                        // accept only https Ed25519VerificationKey2020
                        m -> m.getScheme().equalsIgnoreCase("https")),
                        new Ed25519VerificationKey2020Provider(loader))

                // accept did:key
                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
                        ControllableKeyProvider.of(new DidKeyResolver(MulticodecDecoder.getInstance(KeyCodec.ED25519_PUBLIC_KEY, KeyCodec.ED25519_PRIVATE_KEY))))

                .build();
    }


}
