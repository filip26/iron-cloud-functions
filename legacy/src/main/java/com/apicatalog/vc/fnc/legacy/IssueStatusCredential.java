package com.apicatalog.vc.fnc.legacy;

public class IssueStatusCredential extends HttpJsonFunction {
//
//    static final DocumentLoader LOADER = new StaticContextLoader(SchemeRouter.defaultInstance());
//
//    static final EdDSARdfc2022Suite SUITE = new EdDSARdfc2022Suite();
//
//    static final URI VERIFICATION_METHOD = Ed25520KeyPairProvider.getVerificationMethod();
//
//    static final Issuer ISSUER = SUITE.createIssuer(Ed25520KeyPairProvider.getKeyPair()).loader(LOADER);
//
//    static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
//
//    static final Firestore DB = FirestoreOptions.getDefaultInstance().toBuilder()
//            .setDatabaseId("iron-vc-demo")
//            .build()
//            .getService();
//
//    static final String TEMPLATE = """
//            {
//                "@context": [
//                  "https://www.w3.org/ns/credentials/v2"
//                ],
//                "id": "%s",
//                "type": ["VerifiableCredential", "BitstringStatusListCredential"],
//                "issuer": "%s",
//                "credentialSubject": {
//                  "type": "BitstringStatusList",
//                  "statusPurpose": "%s",
//                  "encodedList": "%s"
//                }
//            }
//            """;
//
//    public IssueStatusCredential() {
//        super("POST", HttpURLConnection.HTTP_CREATED);
//    }
//
//    @Override
//    protected JsonObject process(JsonObject jsonData) throws HttpFunctionError {
//
//        var statusList = jsonData.string("statusList");
//
//        try {
//
//            var snapshot = DB.collection("statusList").document(statusList).get().get();
//
//            if (!snapshot.exists()) {
//                // TODO
//            }
//
//            // Retrieve the array field
//            List<Long> values = (List<Long>) snapshot.get("index");
////            
//            byte[] x = values.stream().map(IssueStatusCredential::intToByteArray)
//                    .collect(
//                            () -> new ByteArrayOutputStream(),
//                            (b, e) -> b.write(e, 0, e.length),
//                            (a, b) -> {
//                            })
//                    .toByteArray();
//
//            byte[] y = compress(x);
//
//            String purpose = snapshot.string("purpose");
//
//            var m = String.format(TEMPLATE,
//                    "https://firebasestorage.googleapis.com/v0/b/vc-status/o/"
//                            + statusList + "?alt=media",
//                    "https://iron.apicatalog.com",
//                    purpose,
//                    Multibase.BASE_64_URL.encode(y));
//
//            DataIntegrityDraft draft = ISSUER.createDraft(VERIFICATION_METHOD);
//
//            draft.purpose(URI.create("https://w3id.org/security#assertionMethod"));
//            draft.created(Instant.now().truncatedTo(ChronoUnit.SECONDS));
//
//            var json = parseJson(new StringReader(m));
//
//            var signed = ISSUER.sign(json, draft);
//
//            BlobStorage.createBlob(STORAGE, "vc-status", statusList, signed);
//
//            return signed;
//
//        } catch (DocumentError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code()));
//
//        } catch (CryptoSuiteError e) {
//            throw new HttpFunctionError(e, HttpFunctionError.toString(e.code().name()));
//
//        } catch (IOException e) {
//            throw new HttpFunctionError(e, e.getMessage());
//
//        } catch (InterruptedException | ExecutionException e) {
//            throw new HttpFunctionError(e, e.getMessage());
//        }
//    }
//
//    static final byte[] intToByteArray(int value) {
//        return new byte[] {
//                (byte) (value >>> 24),
//                (byte) (value >>> 16),
//                (byte) (value >>> 8),
//                (byte) value
//        };
//    }
//
//    static final byte[] intToByteArray(long value) {
//        return new byte[] {
//                (byte) (value >>> 24),
//                (byte) (value >>> 16),
//                (byte) (value >>> 8),
//                (byte) value
//        };
//    }
//
//    static byte[] compress(byte[] input) throws IOException {
//        if (input == null || input.length == 0) {
//            return new byte[0]; // Return empty byte array if the input is null or empty
//        }
//
//        var byteArrayOutputStream = new ByteArrayOutputStream();
//
//        try (var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
//            gzipOutputStream.write(input);
//        }
//
//        return byteArrayOutputStream.toByteArray();
//    }
//
//    static byte[] decompress(byte[] input) throws IOException {
//        if (input == null || input.length == 0) {
//            return new byte[0]; // Return empty byte array if the input is null or empty
//        }
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
//        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
//            return gzipInputStream.readAllBytes();
//        }
//    }
}
