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

# 2. Create Connected Account (FIXED)
echo -n "2. Creating Connected Account... "

RESPONSE_BODY=$(curl -s --request POST \
  --url http://localhost:8081/api/users/me/create-seller-account \
  --header "authorization: Bearer $USER_TOKEN")
ONBOARDING_URL=$(echo $RESPONSE_BODY | jq -r .onboardingUrl)

# Check if URL was found (jq returns "null" string if key is missing)
if [ "$ONBOARDING_URL" != "null" ] && [ -n "$ONBOARDING_URL" ]; then
    echo -e "${GREEN}SUCCESS${NC}"
else
    echo -e "${RED}FAILED${NC}"
    echo "Server Response: $RESPONSE_BODY"
    exit 1
fi

# 3. Open Onboarding URL
echo "3. Opening Onboarding URL..."
echo "Target: $ONBOARDING_URL"

# Cross-platform open command
if [[ "$OSTYPE" == "darwin"* ]]; then
    open "$ONBOARDING_URL"
elif command -v xdg-open &> /dev/null; then
    xdg-open "$ONBOARDING_URL"
else
    # Fallback for Windows Git Bash users
    start "$ONBOARDING_URL"
fi