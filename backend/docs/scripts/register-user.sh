#!/bin/bash
USERNAME=${1:-"testbuyer"}
PASSWORD=${2:-"password"}
EMAIL="${USERNAME}@gmail.com"

echo "-----------------------------------"
echo "Target User: $USERNAME"
echo "Target Pass: $PASSWORD" 
echo "-----------------------------------"

# Define colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'


# Check for jq
if ! command -v jq &> /dev/null; then
    echo -e "${RED}Error: jq is not installed. Please install it to run this script.${NC}"
    exit 1
fi

echo "--- Starting Registration Process ---"

# 1. Get Keycloak Admin Token
echo -n "1. Fetching Admin Token... "
ADMIN_RESPONSE=$(curl -s --request POST \
  --url http://localhost:8089/realms/master/protocol/openid-connect/token \
  --header 'content-type: application/x-www-form-urlencoded' \
  --data client_id=admin-cli \
  --data username=admin \
  --data password=admin \
  --data grant_type=password)

ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | jq -r .access_token)

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}FAILED${NC}"
    echo "Error details: $ADMIN_RESPONSE"
    exit 1
else
    echo -e "${GREEN}SUCCESS${NC}"
fi

# 2. Create Client in Keycloak (necessary for non-browser login)
echo -n "2. Creating Client... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request POST \
  --url http://localhost:8089/admin/realms/auction-marketplace/clients \
  --header "authorization: Bearer $ADMIN_TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "clientId": "auction-client",
  "name": "auction-client",
  "enabled": true,
  "publicClient": true,
  "directAccessGrantsEnabled": true,
  "standardFlowEnabled": true,
  "redirectUris": [
    "*"
  ],
  "webOrigins": [
    "*"
  ]
}'
)

if [ "$HTTP_CODE" -eq 201 ]; then
    echo -e "${GREEN}SUCCESS (Created)${NC}"
elif [ "$HTTP_CODE" -eq 409 ]; then
    echo -e "${GREEN}SKIPPED (Client already exists)${NC}"
else
    echo -e "${RED}FAILED (HTTP $HTTP_CODE)${NC}"
    exit 1
fi


# 3. Register New User in Keycloak
echo -n "3. Creating User ($USERNAME)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request POST \
  --url http://localhost:8089/admin/realms/auction-marketplace/users \
  --header "authorization: Bearer $ADMIN_TOKEN" \
  --header 'content-type: application/json' \
  --data "{
  \"username\": \"$USERNAME\",
  \"enabled\": true,
  \"email\": \"$EMAIL\",
  \"firstName\": \"Test\",
  \"lastName\": \"Buyer\",
  \"attributes\": {
    \"phoneNumber\": [
      \"+421456789012\"
    ]
  },
  \"credentials\": [
    {
      \"type\": \"password\",
      \"value\": \"$PASSWORD\",
      \"temporary\": false
    }
  ]
}")

if [ "$HTTP_CODE" -eq 201 ]; then
    echo -e "${GREEN}SUCCESS (Created)${NC}"
elif [ "$HTTP_CODE" -eq 409 ]; then
    echo -e "${GREEN}SKIPPED (User already exists)${NC}"
else
    echo -e "${RED}FAILED (HTTP $HTTP_CODE)${NC}"
    exit 1
fi

# 4. Login the User
echo -n "4. Logging in as User... "
USER_RESPONSE=$(curl -s --request POST \
  --url http://localhost:8089/realms/auction-marketplace/protocol/openid-connect/token \
  --header 'content-type: application/x-www-form-urlencoded' \
  --data client_id=auction-client \
  --data grant_type=password \
  --data username=$USERNAME \
  --data password=$PASSWORD \
  --data scope=openid)

USER_TOKEN=$(echo $USER_RESPONSE | jq -r .access_token)

if [ "$USER_TOKEN" == "null" ] || [ -z "$USER_TOKEN" ]; then
    echo -e "${RED}FAILED${NC}"
    echo "Error details: $USER_RESPONSE"
    exit 1
else
    echo -e "${GREEN}SUCCESS${NC}"
fi


# 5. Register User in Auction Backend
echo -n "5. Registering in Auction Backend... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request POST \
  --url http://localhost:8081/api/users/register \
  --header "authorization: Bearer $USER_TOKEN")

if [[ "$HTTP_CODE" -ge 200 && "$HTTP_CODE" -lt 300 ]]; then
     echo -e "${GREEN}SUCCESS${NC}"
elif [ "$HTTP_CODE" -eq 409 ]; then
     echo -e "${GREEN}SKIPPED (Already registered)${NC}"
else
     echo -e "${RED}FAILED (HTTP $HTTP_CODE)${NC}"
     exit 1
fi

echo "--- Script Completed Successfully ---"