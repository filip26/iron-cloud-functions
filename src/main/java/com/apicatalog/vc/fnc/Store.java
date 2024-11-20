package com.apicatalog.vc.fnc;

import java.util.HashMap;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import jakarta.json.JsonObject;

public class Store {

    protected static Firestore DB = null;

    static {
//        try {
//            
//            // Use the application default credentials
//            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
//            FirestoreOptions options = FirestoreOptions.getDefaultInstance().toBuilder()
//                    .setDatabaseId("iron-vc-demo")
//                    .setCredentials(credentials)
//                    .setProjectId("api-catalog")
//                    .build();
//
//            DB = options.getService();
//            
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static final boolean isInitialized() {
        return DB != null;
    }

    public static final ApiFuture<DocumentReference> write(JsonObject signed) {
        CollectionReference docRef = DB.collection("issued");

        Map<String, Object> data = new HashMap<>();
        data.put("first", "Ada");
        data.put("last", "Lovelace");
        data.put("born", 1815);
        // asynchronously write data

        ApiFuture<DocumentReference> result = docRef.add(signed);
        
        return result;
    }
    
    public static final ApiFuture<DocumentReference> writeError(JsonObject credential) {
        CollectionReference docRef = DB.collection("error");

        Map<String, Object> data = new HashMap<>();
        data.put("first", "Ada");
        data.put("last", "Lovelace");
        data.put("born", 1815);
        // asynchronously write data

        ApiFuture<DocumentReference> result = docRef.add(data);
        return result;
    }
}
