package com.apicatalog.vc.fnc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.cloud.functions.HttpRequest;

public class VerifyFunctionTest {

    static VerifyFunction VERIFY = new VerifyFunction();

    @Test
    void testEd25519() throws IOException {

        HttpRequest req = MockHttpRequest.of(
                """
                                                {
                          "verifiableCredential": {
                            "@context": [
                              "https://www.w3.org/ns/credentials/v2",
                              "https://w3id.org/security/suites/ed25519-2020/v1"
                            ],
                            "type": [
                              "VerifiableCredential"
                            ],
                            "credentialSubject": {
                              "id": "did:example:subject"
                            },
                            "issuer": "https://us-central1-api-catalog.cloudfunctions.net/vc-api-issue-ed25519",
                            "proof": {
                              "type": "Ed25519Signature2020",
                              "created": "2025-02-10T14:57:15Z",
                              "proofPurpose": "assertionMethod",
                              "verificationMethod": "did:key:z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7#z6MktgKTsu1QhX6QPbyqG6geXdw6FQCZBPq7uQpieWbiQiG7",
                              "proofValue": "z2mwteLVe6VikBBZfDLEsLwav6zrRwZvNJL6yrUJnnoTMpEBopVqnGNcqaKra8tPWUQWAQnneSY9pwNU1btqFKst2"
                            }
                          }
                        }
                                                """

        );
        
        var res = new MockHttpResponse();

        VERIFY.service(req, res);
        
        assertEquals(200, res.getStatusCode());

    }

}
