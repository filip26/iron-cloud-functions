#!/usr/bin/env bash

gcloud functions deploy statusIssueCredential --gen2 --entry-point=com.apicatalog.vc.fnc.IssueStatusCredential --runtime=java21 --source=. --trigger-http --allow-unauthenticated
