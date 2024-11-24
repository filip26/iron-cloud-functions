#!/usr/bin/env bash

for script in bin/deploy-issue-*.sh; do
  $script;
done
