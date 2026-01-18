  #!/bin/bash

  if [ $# -lt 2 ]; then
    echo "Usage: $0 <order_id> <tracker_id>"
    exit 1
  fi

  ORDER_ID="$1"
  TRACKER_ID="$2"
  TRACKING_NUMBER="SHIP24_SAMPLE_PENDING_000"
  WEBHOOK_SECRET="whs_ddh1D05OUgf284UEuer4fFTpIWZAkc"

  curl -X POST http://localhost:8081/api/v1/webhooks/orders/ship24 \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${WEBHOOK_SECRET}" \
    -d '{
      "trackings": [
        {
          "tracker": {
            "trackerId": "'"${TRACKER_ID}"'",
            "trackingNumber": "'"${TRACKING_NUMBER}"'",
            "shipmentReference": "'"${ORDER_ID}"'"
          },
          "events": [
            {
              "eventId": "event-exception-'"$(date +%s)"'",
              "status": "Package lost in transit",
              "occurrenceDatetime": "'"$(date -u +%Y-%m-%dT%H:%M:%SZ)"'",
              "statusCode": "EX",
              "statusCategory": "exception",
              "statusMilestone": "exception"
            }
          ]
        }
      ]
    }'
