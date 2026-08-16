#!/usr/bin/env bash

read -r -d '' USER_DATA <<'DATA'
JcsIssuerService|com.apicatalog.gc.di.issuer.JcsIssuerService|./di-jcs-issuer/.| 
RDFCIssuerService|com.apicatalog.gc.di.issuer.RDFCIssuerService|./di-rdfc-issuer/.| 
DATA

FUNCTION_ID=$1
CONFIG_FILE="functions.json"

if [ -z "$FUNCTION_ID" ]; then
  echo "Error: No function id provided."
  echo "Usage: run.sh <function-id>"
  exit 1
fi

# This maps JSON keys to the Uppercase environment variables required by the function
ENV_VARS=$(jq -r --arg ID $FUNCTION_ID '
  .[] | select(.id == $ID) | 
  .env | to_entries | 
  map("\(.key + "=" + (.value|tostring))" ) | 
  join(" ") 
' $CONFIG_FILE)

while IFS='=' read -r k v; do
  export "$k=$v"
done < <(
  jq -r --arg ID "$FUNCTION_ID" '
    .[] | select(.id == $ID) | 
    with_entries(select(.key|(contains("env") | not))) | to_entries[] |
    "FNC_\(.key|ascii_upcase)=\(.value|tostring)"
  ' $CONFIG_FILE
)

if [ -z "$FNC_TYPE" ] || [ -z "$FNC_REGION" ] || [ -z "$ENV_VARS" ]; then
 echo "Error: Configuration for $FUNCTION_ID not found."
 exit 1
fi

  while IFS='|' read -r name clazz source rest; do
    [[ "$name" == "$FNC_TYPE" ]] || continue

    shift 0

    IFS='|' read -ra args <<< "$rest"

    export "DIRECTORY=$source"
    export "CLAZZ=$clazz"
    
  done <<< "$USER_DATA"

cd $DIRECTORY
export $ENV_VARS; mvn function:run -Drun.functionTarget=$CLAZZ
