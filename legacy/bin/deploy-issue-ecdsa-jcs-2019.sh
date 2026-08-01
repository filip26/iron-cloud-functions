#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-ecdsa-jcs-2019-p256 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueECDSAJcs2019P256 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
gcloud functions deploy vc-api-issue-ecdsa-jcs-2019-p384 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueECDSAJcs2019P384 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
