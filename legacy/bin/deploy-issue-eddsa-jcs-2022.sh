#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-eddsa-jcs-2022 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueEdDSAJcs2022 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
