#!/usr/bin/env bash

for script in bin/deploy-*-*.sh; do
  $script &;
done
