#!/usr/bin/env bash

KMS_LOCATION=global KMS_KEY_RING=did-cel-keyring-1 KMS_KEY_ID=witness-indigo VERIFICATION_METHOD=did:key:z6Mkt4GJLRNdRhVPJjq8D4fuuqX44LtqgVsEV5o35xmjcjN4#z6Mkt4GJLRNdRhVPJjq8D4fuuqX44LtqgVsEV5o35xmjcjN4 mvn function:run -Drun.functionTarget=com.apicatalog.gc.di.issuer.RDFCIssuerService


#KMS_LOCATION=global KMS_KEY_RING=did-cel-keyring-1 KMS_KEY_ID=witness-indigo VERIFICATION_METHOD=did:key:z6Mkt4GJLRNdRhVPJjq8D4fuuqX44LtqgVsEV5o35xmjcjN4#z6Mkt4GJLRNdRhVPJjq8D4fuuqX44LtqgVsEV5o35xmjcjN4 mvn function:run -Drun.functionTarget=com.apicatalog.gc.di.issuer.JcsIssuerService


#KMS_LOCATION=global KMS_KEY_RING=did-cel-keyring-1 KMS_KEY_ID=witness-red-p256-hsm VERIFICATION_METHOD=did:key:zDnaeVGPdpamk6akcmP4dBAvHnKwnZwvxiGdzva4YMjqmwwj4#zDnaeVGPdpamk6akcmP4dBAvHnKwnZwvxiGdzva4YMjqmwwj4 mvn function:run -Drun.functionTarget=com.apicatalog.iron.gc.issuer.JcsIssuerService
