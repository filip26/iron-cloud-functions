package com.apicatalog.vc.fnc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.storage.control.v2.GetStorageLayoutRequest;
import com.google.storage.control.v2.StorageControlClient;
import com.google.storage.control.v2.StorageLayout;
import com.google.storage.control.v2.StorageLayoutName;

import jakarta.json.JsonObject;

public class XStorage {

    public static void uploadObject(
            Storage storage,
            String objectName,
            JsonObject data) throws IOException {

        String projectId = "api-catalog";
        String bucketName = "iron-vc-demo";

        // The ID of your GCP project
        // String projectId = "your-project-id";

        // The ID of your GCS bucket
        // String bucketName = "your-unique-bucket-name";

        // The ID of your GCS object
        // String objectName = "your-object-name";

        // The path to your file to upload
        // String filePath = "path/to/your/file"

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/ld+json").build();

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

    public static void init() throws Exception {
        String bucketName = "iron-vc-demo";

        // Instantiates a client in a try-with-resource to automatically cleanup
        // underlying resources
        try (StorageControlClient storageControlClient = StorageControlClient.create()) {
            GetStorageLayoutRequest request = GetStorageLayoutRequest.newBuilder()
                    // Set project to "_" to signify global bucket
                    .setName(StorageLayoutName.format("_", bucketName))
                    .build();
            StorageLayout response = storageControlClient.getStorageLayout(request);
            System.out.printf("Performed getStorageLayout request for %s", response.getName());
        }
    }

}
