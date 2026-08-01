#!/usr/bin/env bash

gcloud functions deploy vc-api-verify --gen2 --entry-point=com.apicatalog.vc.fnc.VerifyFunction --runtime=java21 --source=. --trigger-http --allow-unauthenticated
