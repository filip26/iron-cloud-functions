package com.apicatalog.vc.fnc;

import java.io.BufferedWriter;
import java.util.Map.Entry;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public class VerifyFunction implements HttpFunction {

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

}
