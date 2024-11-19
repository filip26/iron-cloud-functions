package com.apicatalog.vc.service;

public class VcApiVerticle /*extends AbstractVerticle*/ {
//
//    Instant startTime;
//
//    // Use the application default credentials
////    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
////    FirebaseOptions options = FirebaseOptions.builder()
////            .setCredentials(credentials)
//////            .setProjectId(projectId)
////            .build();
//
//    // Initialize Firebase
////    FirebaseApp app = FirebaseApp.initializeApp(options);
////
////    Firestore db = FirestoreClient.getFirestore();
//
//    @Override
//    public void start() throws Exception {
//
//        final Router router = Router.router(vertx);
//
//        router.post().handler(BodyHandler.create().setBodyLimit(250000));
//
//        // verifier's VC API
//        VerifierApi.setup(router);
//
//        // issuer's VC API
//        IssuerApi.setup(router);
//
//        // holder's VC API
//        HolderApi.setup(router);
//
//        // static resources
//        router
//                .get("/key/*")
//                .handler(StaticHandler
//                        .create("webroot/key/")
//                        .setIncludeHidden(false)
//                        .setDefaultContentEncoding("UTF-8")
//                        .setMaxAgeSeconds(365 * 24 * 3600l));
//
//        router.get().handler(StaticHandler
//                .create()
//                .setIncludeHidden(false)
//                .setDefaultContentEncoding("UTF-8")
//                .setMaxAgeSeconds(4 * 3600l) // maxAge = 4 hours
//        );
//
//        // server options
//        var serverOptions = new HttpServerOptions()
//                .setMaxWebSocketFrameSize(1000000)
//                .setUseAlpn(true);
//
//        // service
//        vertx
//                .createHttpServer(serverOptions)
//                .requestHandler(router)
//                .listen(getDefaultPort())
//                .onSuccess(ctx -> {
//                    System.out.println(VcApiVerticle.class.getName() + " started on port " + ctx.actualPort() + " with " + Charset.defaultCharset() + " charset.");
//                    startTime = Instant.now();
//                })
//                .onFailure(ctx -> System.err.println(VcApiVerticle.class.getName() + " start failed [" + ctx.getMessage() + "]."));
//    }
//
//    @Override
//    public void stop() throws Exception {
//        if (startTime != null) {
//            System.out.println(VcApiVerticle.class.getName() + " stopped after running for " + Duration.between(startTime, Instant.now()) + ".");
//        }
//    }
//
//    static final int getDefaultPort() {
//        final String envPort = System.getenv("PORT");
//
//        if (envPort != null) {
//            return Integer.valueOf(envPort);
//        }
//        return 8080;
//    }
}
