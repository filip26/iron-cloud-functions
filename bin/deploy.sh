#!/usr/bin/env bash

gcloud app deploy target/*-jar-with-dependencies.jar  --no-cache --appyaml=src/main/appengine/app.yaml
