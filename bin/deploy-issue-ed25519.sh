#!/usr/bin/env bash

gcloud functions deploy vc-api-issue-ed25519 --gen2 --entry-point=com.apicatalog.vc.fnc.IssueEd25519 --runtime=java21 --source=. --trigger-http --allow-unauthenticated
