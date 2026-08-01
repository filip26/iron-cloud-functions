#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-ecdsa-jcs-2019 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueECDSAJcs2019P256 --runtime=java25 --source=. --trigger-http --allow-unauthenticated
