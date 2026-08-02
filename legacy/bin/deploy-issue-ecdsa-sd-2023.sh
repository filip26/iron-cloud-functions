#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-ecdsa-sd-2023-p256 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueECDSASD2023P256 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
gcloud functions deploy vc-api-issue-ecdsa-sd-2023-p384 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueECDSASD2023P384 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
