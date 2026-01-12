#!/bin/bash
USERNAME=${1:-"testbuyer"}
PASSWORD=${2:-"password"}

echo "-----------------------------------"
echo "Target User: $USERNAME"
echo "Target Pass: $PASSWORD" 
echo "-----------------------------------"

# Define colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# 1. Login the User
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
    echo "Error details: $USER_RESPONSE"
    exit 1
else
    echo -e "${GREEN}SUCCESS${NC}"
fi

# 2. Create Stripe Customer
echo -n "2. Creating Stripe Customer... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request POST \
  --url http://localhost:8081/api/users/me/create-stripe-customer \
  --header "authorization: Bearer $USER_TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "line1": "123 Main St",
  "line2": "Apt 4B",
  "city": "Brno",
  "state": "South Moravia",
  "postalCode": "60200",
  "country": "CZ"
}')

if [[ "$HTTP_CODE" -ge 200 && "$HTTP_CODE" -lt 300 ]]; then
     echo -e "${GREEN}SUCCESS${NC}"
elif [ "$HTTP_CODE" -eq 409 ]; then
     echo -e "${GREEN}SKIPPED (Already created)${NC}"
else
     echo -e "${RED}FAILED (HTTP $HTTP_CODE)${NC}"
     exit 1
fi

# Wait for 1 second to ensure the customer is created
sleep 1

# 3. Create Payment Intent
echo -n "3. Creating Payment Intent... "

# Capture the RAW response first (Do not pipe to jq yet)
RESPONSE_BODY=$(curl -s --request POST \
  --url http://localhost:8081/api/users/me/setup-payment-intent \
  --header "authorization: Bearer $USER_TOKEN" \
  --header 'content-type: application/json' \
  --data '{
  "line1": "123 Main St",
  "line2": "Apt 4B",
  "city": "Brno",
  "state": "South Moravia",
  "postalCode": "60200",
  "country": "CZ"
}')

# Now try to extract the secret
PAYMENT_INTENT_SECRET=$(echo $RESPONSE_BODY | jq -r .clientSecret)

# Check if the extraction worked
if [ "$PAYMENT_INTENT_SECRET" == "null" ] || [ -z "$PAYMENT_INTENT_SECRET" ]; then
    echo -e "${RED}FAILED${NC}"
    # Now you can print the actual response body to see the real error
    echo "Server Response: $RESPONSE_BODY"
    exit 1
else
    echo -e "${GREEN}SUCCESS${NC}"
    echo "Payment Intent Secret: $PAYMENT_INTENT_SECRET"
fi


# 3. Use the add-payment-method.html to add a payment method
echo "3. Opening 'add-payment-method.html' in browser..."

# Open browser automatically (Cross-platform support)
if [[ "$OSTYPE" == "darwin"* ]]; then
    open "add-payment-method.html"
elif command -v xdg-open &> /dev/null; then
    xdg-open "add-payment-method.html"
else
    echo "Could not open browser automatically. Please open 'add-payment-method.html' manually."
fi

echo "---------------------------------------------------"
echo "INSTRUCTION: Copy the 'Payment Intent Secret' below:"
echo "$PAYMENT_INTENT_SECRET"
echo "---------------------------------------------------"

# Pause script and wait for user input
read -p "4. Paste the resulting 'pm_...' ID here and press Enter: " PAYMENT_METHOD_ID

# Check if input is valid
if [[ -z "$PAYMENT_METHOD_ID" ]]; then
    echo "Error: No Payment Method ID provided."
    exit 1
fi

echo "Attaching Payment Method ($PAYMENT_METHOD_ID) to user..."

# Execute the command automatically
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --request POST \
  --url http://localhost:8081/api/users/me/payment-methods \
  --header "authorization: Bearer $USER_TOKEN" \
  --header 'content-type: application/json' \
  --data "{
    \"paymentMethodId\": \"$PAYMENT_METHOD_ID\"
  }")

if [[ "$HTTP_CODE" -ge 200 && "$HTTP_CODE" -lt 300 ]]; then
    echo -e "${GREEN}SUCCESS${NC} Payment method attached."
else
    echo -e "${RED}FAILED${NC} Server returned HTTP $HTTP_CODE"
    exit 1
fi