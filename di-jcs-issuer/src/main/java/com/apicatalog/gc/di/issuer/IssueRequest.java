package com.apicatalog.gc.di.issuer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.Consumer;

import com.apicatalog.di.suite.StandardCryptoSuite.ProofDraft;
import com.apicatalog.trust.proof.Proof;
import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.CryptoKeyVersion;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyRingName;
import com.google.cloud.kms.v1.PublicKey;

public record IssueRequest(
        SequencedCollection<String> context,
        Map<String, Object> document,
        Options options) {

    static final String CREDENTIAL = "credential";
    static final String PRESENTATION = "presentation";
    static final String OPTIONS = "options";

    public static IssueRequest from(final Map<String, Object> source) {

        SequencedCollection<String> context = List.of();
        Map<String, Object> document = source;
        Options options = null;

        for (var entry : source.entrySet()) {
            switch (entry.getKey()) {
            case "@context" -> context = MapEntryAdapter.stringCollection(entry);
            case CREDENTIAL -> document = (Map<String, Object>) entry.getValue();
            case OPTIONS -> options = Options.from((Map<String, ?>) entry.getValue());

            default ->
                throw new IllegalArgumentException("Unexpected option: " + entry.getKey() + "=" + entry.getValue());
            }
        }

        return new IssueRequest(context, document, options);
    }

    static record Options(
            String credentialId,
            Collection<String> mandatoryPointers,
            SequencedCollection<String> context,
            String purpose,
            Instant created,
            Instant expires,
            String challenge,
            SequencedCollection<String> domain,
            String nonce,
            SequencedCollection<String> previous) {

        static final String ASSERTION_PURPOSE = "assertionMethod";

        static final String OPTION_DOMAIN = "domain";
        static final String OPTION_CHALLENGE = "challenge";
        static final String OPTION_NONCE = "nonce";
        static final String OPTION_CREATED = "created";
        static final String OPTION_EXPIRES = "expires";
        static final String OPTION_MANDATORY_POINTERS = "mandatoryPointers";
        static final String OPTION_PREVIOUS = "previousProof";
        static final String OPTION_CREDENTIAL_ID = "credentialId";

        static Options from(Map<String, ?> source) {

            Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            Instant expires = null;
            String challenge = null;
            SequencedCollection<String> domain = null;
            String nonce = null;
            SequencedCollection<String> previous = null;

            String credentialId = null;
            SequencedCollection<String> context = List.of();
            Collection<String> mandatoryPointers = List.of();

            for (var entry : source.entrySet()) {

                switch (entry.getKey()) {
                case OPTION_CREDENTIAL_ID -> credentialId = MapEntryAdapter.string(entry);
                case OPTION_MANDATORY_POINTERS -> mandatoryPointers = MapEntryAdapter.stringCollection(entry);
                case OPTION_CREATED -> created = MapEntryAdapter.instant(entry);
                case OPTION_EXPIRES -> expires = MapEntryAdapter.instant(entry);
                case OPTION_CHALLENGE -> challenge = MapEntryAdapter.string(entry);
                case OPTION_DOMAIN -> domain = MapEntryAdapter.stringCollection(entry);
                case OPTION_NONCE -> nonce = MapEntryAdapter.string(entry);
                case OPTION_PREVIOUS -> previous = MapEntryAdapter.stringCollection(entry);
                case "@context" -> context = MapEntryAdapter.stringCollection(entry);

                default ->
                    throw new IllegalArgumentException("Unexpected option: " + entry.getKey() + "=" + entry.getValue());
                }
            }

            return new Options(
                    credentialId,
                    mandatoryPointers,
                    context,
                    ASSERTION_PURPOSE,
                    created,
                    expires,
                    challenge,
                    domain,
                    nonce,
                    previous);

        }

        public void init(SequencedCollection<String> documentContext, ProofDraft proofDraft) {
            proofDraft.context(context != null
                    ? context
                    : documentContext);
            proofDraft.created(created);
            proofDraft.challenge(challenge);
            proofDraft.expires(expires);
            proofDraft.nonce(nonce);
            proofDraft.previousProof(previous);
            proofDraft.purpose(Proof.Purpose.from(purpose));
            proofDraft.domain(domain);
        }

    }

    // TODO move to crypto-kms
    static void forEachPublicKey(KeyManagementServiceClient client, KeyRingName keyRingName, Consumer<PublicKey> consumer) throws Exception {

//        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {

            for (CryptoKey cryptoKey : client.listCryptoKeys(keyRingName).iterateAll()) {
                CryptoKey.CryptoKeyPurpose purpose = cryptoKey.getPurpose();

                if (purpose == CryptoKey.CryptoKeyPurpose.ASYMMETRIC_SIGN) {
                    for (CryptoKeyVersion version : client.listCryptoKeyVersions(cryptoKey.getName()).iterateAll()) {
                        if (version.getState() == CryptoKeyVersion.CryptoKeyVersionState.ENABLED) {
                            consumer.accept(client.getPublicKey(version.getName()));
//                            PublicKey publicKey = client.getPublicKey(version.getName());
//                            try {
//                                System.out.printf("Key %s, Version: %s%n", publicKey.getAlgorithm(), version.getName());
//                                IO.println(KmsPublicKeyExporter.publicKeyMultibase(publicKey));
//                            } catch (IllegalArgumentException e) {
//                                IO.println(e.getMessage());
//                            }
                        }
                    }
                }
            }
//        }
    }
}
