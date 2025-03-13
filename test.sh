#!/bin/bash

# Test script for Queue Service
HOST="localhost"
PORT="8080"
API_URL="http://$HOST:$PORT/api/requests"
REQUEST_TYPES=("TYPE1" "TYPE2" "TYPE3" "TYPE4")
CONTENTS=(
  "Important data that needs processing"
  "High priority information"
  "Medium priority task details"
  "Low priority background job"
  "Critical system notification"
)

# Function to send a single request
send_request() {
  local type=$1
  local content=$2
  local id=$3

  json="{\"type\":\"$type\",\"content\":\"$content #$id\"}"

  curl -s -X POST $API_URL \
    -H "Content-Type: application/json" \
    -d "$json" > /dev/null

  echo "Sent request #$id of type $type"
}

# Loop to send multiple requests
for i in {1..100}; do
  type_index=$((2 % ${#REQUEST_TYPES[@]}))
  content_index=$((RANDOM % ${#CONTENTS[@]}))

  send_request "${REQUEST_TYPES[$type_index]}" "${CONTENTS[$content_index]}" "$i"
done