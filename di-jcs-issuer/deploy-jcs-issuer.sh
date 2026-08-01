#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-ecdsa-jcs-2019 --gen2 --entry-point=com.apicatalog.iron.gc.issuer.JCSIssuerService --runtime=java25 --source=. --trigger-http --allow-unauthenticated
