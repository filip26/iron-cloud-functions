package com.apicatalog.vc.service;

import java.util.Arrays;
import java.util.Collection;

public class Constants {

    public static final String OPTIONS = "options";
    public static final String OPTION_TYPE = "type";
    public static final String OPTION_DOMAIN = "domain";
    public static final String OPTION_CHALLENGE = "challenge";
    public static final String OPTION_CREATED = "created";
    public static final String OPTION_PURPOSE = "expectedProofPurpose";
    public static final String OPTION_MANDATORY_POINTERS = "mandatoryPointers";
    public static final String OPTION_SELECTIVE_POINTERS = "selectivePointers";

    public static final Collection<String> OPTIONS_KEYS = Arrays.asList(OPTION_TYPE, OPTION_DOMAIN, OPTION_CHALLENGE, OPTION_CREATED, OPTION_PURPOSE, OPTION_MANDATORY_POINTERS, OPTION_SELECTIVE_POINTERS);

    public static final String CTX_RESULT = "verificationResult";
    public static final String CTX_DOCUMENT_KEY = "documentKey";
    public static final String CTX_STRICT = "strictMode";

    public static final String CREDENTIAL_KEY = "credential";

    public static final String VERIFIABLE_CREDENTIAL_KEY = "verifiableCredential";
    public static final String VERIFIABLE_PRESENTATION_KEY = "verifiablePresentation";

    protected Constants() {
    }
}
