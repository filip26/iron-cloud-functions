package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import jakarta.json.JsonObject;

public class BlobStorage {

    public static final String BUCKET_NAME = "iron-vc-demo";

    public static void createBlob(
            Storage storage,
            String blobName,
            JsonObject data) throws IOException {

        BlobId blobId = BlobId.of(BUCKET_NAME, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();

        byte[] content = data.toString().getBytes(StandardCharsets.UTF_8);

        // Optional: set a generation-match precondition to enable automatic retries,
        // avoid potential
        // race
        // conditions and data corruptions. The request returns a 412 error if the
        // preconditions are not met.
        Storage.BlobTargetOption precondition;
//        if (storage.get(bucketName, objectName) == null) {
        // For a target object that does not yet exist, set the DoesNotExist
        // precondition.
        // This will cause the request to fail if the object is created before the
        // request runs.
        precondition = Storage.BlobTargetOption.doesNotExist();
//        } else {
//          // If the destination already exists in your bucket, instead set a generation-match
//          // precondition. This will cause the request to fail if the existing object's generation
//          // changes before the request runs.
//          precondition =
//              Storage.BlobTargetOption.generationMatch(
//                  storage.get(bucketName, objectName).getGeneration());
//        }

        storage.create(blobInfo, content, precondition);

//          System.out.println(
//              "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }
}
