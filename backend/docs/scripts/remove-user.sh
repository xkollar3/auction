#!/bin/bash
USERNAME=${1:-"testbuyer"}
PASSWORD=${2:-"password"}

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# 1. Login (This part was fine)
echo -n "1. Logging in as User... "
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
    exit 1
else
    echo -e "${GREEN}SUCCESS${NC}"
fi

# 2. Remove user
echo -n "2. Remove user "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request DELETE \
  --url http://localhost:8081/api/users/me \
  --header "authorization: Bearer $USER_TOKEN")

if [ "$HTTP_CODE" -eq 204 ]; then
    echo -e "${GREEN}SUCCESS${NC}"
elif [ "$HTTP_CODE" -eq 404 ]; then
    echo -e "${GREEN}SKIPPED (User already deleted)${NC}"
else
    echo -e "${RED}FAILED (HTTP $HTTP_CODE)${NC}"
    exit 1
fi

echo "--- Script Completed Successfully ---"
