#!/usr/bin/env bash

gcloud functions deploy vc-api-derive --gen2 --entry-point=com.apicatalog.vc.fnc.DeriveFunction --runtime=java21 --source=. --trigger-http --allow-unauthenticated
