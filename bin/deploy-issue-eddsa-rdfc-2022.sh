#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-eddsa-rdfc-2022 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueEdDsaRdfc2022 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
