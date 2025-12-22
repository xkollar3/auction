# Stripe Marketplace Payment Flow - Test Guide

This guide walks through the complete payment flow for a marketplace using Stripe Connect. Funds are collected from a customer, held on the platform, and then transferred to a seller (Connected Account) upon order completion.

## Prerequisites

- Stripe account in **Test Mode**
- API Secret Key (`sk_test_...`)
- Stripe Dashboard access

Replace `sk_test_YOURKEY` with your actual test secret key in all commands.

---

## Step 1: Create a Customer (Buyer) (user context - CreateStripeCustomerCommand)

Customers represent buyers on your platform. They store payment methods and can be charged multiple times.

```bash
curl https://api.stripe.com/v1/customers \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d email="buyer@example.com" \
  -d name="Test Buyer"
```

**Response (save the `id`):**
```json
{
  "id": "cus_xxxxxxxxxxxxx",
  "object": "customer",
  "email": "buyer@example.com",
  "name": "Test Buyer"
}
```

---

## Step 2: Attach a Test Card to the Customer (user context - AddPaymentMethodCommand)

Use the `tok_bypassPending` token — this special test token makes funds **instantly available** for transfers (bypasses the normal 2-7 day pending period).

```bash
curl https://api.stripe.com/v1/customers/cus_xxxxxxxxxxxxx/sources \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d source=tok_bypassPending
```

**Response (save the `id`):**
```json
{
  "id": "card_xxxxxxxxxxxxx",
  "object": "card",
  "brand": "Visa",
  "last4": "0077",
  "customer": "cus_xxxxxxxxxxxxx"
}
```

### Other Useful Test Tokens

| Token | Description |
|-------|-------------|
| `tok_bypassPending` | Funds instantly available (recommended for testing transfers) |
| `tok_visa` | Standard Visa card |
| `tok_mastercard` | Standard Mastercard |
| `tok_chargeDeclined` | Always declines |

---

## Step 3: Create a Connected Account (Seller) (user context - CreateStripeConnectedAccountCommand)

Connected Accounts represent sellers who receive payouts from your platform.

### 3.1 Create the Account (ONLY THIS PART IS DONE VIA OUR BACKEND, SKIP OTHER THAN 3.1 IN INTEGRATION)

```bash
curl https://api.stripe.com/v1/accounts \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d type=express \
  -d country=US \
  -d email="seller@example.com" \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true
```

**Response (save the `id`):**
```json
{
  "id": "acct_xxxxxxxxxxxxx",
  "object": "account",
  "type": "express",
  "email": "seller@example.com"
}
```

### 3.2 Generate Onboarding Link (OUT OF SCOPE, THIS PART IS NOT MANDATORY)

The seller must complete Stripe's onboarding to verify their identity and add a bank account.

```bash
curl https://api.stripe.com/v1/account_links \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d account=acct_xxxxxxxxxxxxx \
  -d refresh_url="https://yoursite.com/reauth" \
  -d return_url="https://yoursite.com/onboarding-complete" \
  -d type=account_onboarding
```

**Response:**
```json
{
  "url": "https://connect.stripe.com/setup/s/...",
  "expires_at": 1234567890
}
```

Open the URL and complete onboarding using test data:

| Field | Test Value |
|-------|------------|
| Phone | Any valid format |
| SSN (last 4) | `0000` |
| Address | Any valid address |
| DOB | Any date (18+ years old) |
| Bank Routing (US) | `110000000` |
| Bank Account (US) | `000123456789` |

### 3.3 Verify Account is Ready

After onboarding, verify the account can receive transfers:

```bash
curl https://api.stripe.com/v1/accounts/acct_xxxxxxxxxxxxx \
  -H "Authorization: Bearer sk_test_YOURKEY"
```

**Check these fields:**
```json
{
  "charges_enabled": true,
  "payouts_enabled": true,
  "details_submitted": true,
  "requirements": {
    "currently_due": [],
    "disabled_reason": null
  }
}
```

All three must be `true` and `currently_due` should be empty before you can transfer funds.

---

## Step 4: Charge the Customer (Pivotal from buyer to shipping, ReserveFundsCommand)

Charge the customer — funds will land in your platform's available balance.

```bash
curl https://api.stripe.com/v1/charges \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d amount=5000 \
  -d currency=eur \
  -d customer=cus_xxxxxxxxxxxxx \
  -d source=tok_bypassPending \
  -d description="Order #12345"
```

**Note:** `amount=5000` = €50.00 (Stripe uses smallest currency unit)

**Response:**
```json
{
  "id": "ch_xxxxxxxxxxxxx",
  "object": "charge",
  "amount": 5000,
  "currency": "eur",
  "status": "succeeded",
  "balance_transaction": "txn_xxxxxxxxxxxxx"
}
```

### Check Actual Fees (Optional)

To see exactly how much you received after Stripe fees:

```bash
curl https://api.stripe.com/v1/balance_transactions/txn_xxxxxxxxxxxxx \
  -H "Authorization: Bearer sk_test_YOURKEY"
```

**Response:**
```json
{
  "amount": 5000,
  "fee": 175,
  "net": 4825,
  "currency": "eur"
}
```

---

## Step 5: Verify Platform Balance (SKIP, NOT NEEDED)

Before transferring, confirm funds are in your available balance:

```bash
curl https://api.stripe.com/v1/balance \
  -H "Authorization: Bearer sk_test_YOURKEY"
```

**Response:**
```json
{
  "available": [
    {
      "amount": 4825,
      "currency": "eur"
    }
  ],
  "pending": [
    {
      "amount": 0,
      "currency": "eur"
    }
  ]
}
```

**Important:** Transfers can only use `available` funds, not `pending`. The `tok_bypassPending` token ensures funds go straight to available.

---

## Step 6: Transfer to the Connected Account (Seller) (Shipping context - TransferPaymentCommand)

Transfer funds to the seller. You can transfer the full amount or keep a commission.

### Full Transfer

```bash
curl https://api.stripe.com/v1/transfers \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d amount=4825 \
  -d currency=eur \
  -d destination=acct_xxxxxxxxxxxxx
```

### Transfer with Platform Commission

Keep €5 as platform fee, transfer €43.25 to seller:

```bash
curl https://api.stripe.com/v1/transfers \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d amount=4325 \
  -d currency=eur \
  -d destination=acct_xxxxxxxxxxxxx \
  -d description="Payout for Order #12345"
```

**Response:**
```json
{
  "id": "tr_xxxxxxxxxxxxx",
  "object": "transfer",
  "amount": 4325,
  "currency": "eur",
  "destination": "acct_xxxxxxxxxxxxx"
}
```

---

## Step 7: Verify Seller Received Funds (Optional)

Check the connected account's balance:

```bash
curl https://api.stripe.com/v1/balance \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -H "Stripe-Account: acct_xxxxxxxxxxxxx"
```

---

## Alternative: Refund Instead of Transfer

If the order is cancelled, refund the customer instead of transferring to seller:

### Full Refund

```bash
curl https://api.stripe.com/v1/refunds \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d charge=ch_xxxxxxxxxxxxx
```

### Partial Refund

```bash
curl https://api.stripe.com/v1/refunds \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d charge=ch_xxxxxxxxxxxxx \
  -d amount=2500
```

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   CUSTOMER                    PLATFORM                      SELLER      │
│   cus_xxx                    (Your Stripe)                 acct_xxx     │
│                                                                         │
│   ┌─────────┐                                                           │
│   │  Card   │                                                           │
│   │ tok_xxx │                                                           │
│   └────┬────┘                                                           │
│        │                                                                │
│        │  ① Charge €50                                                  │
│        ▼                                                                │
│   ─────────────────►  Available Balance: €48.25                         │
│                       (after Stripe fees)                               │
│                              │                                          │
│                              │                                          │
│           ┌──────────────────┴──────────────────┐                       │
│           │                                     │                       │
│      ORDER CANCELLED                      ORDER COMPLETED               │
│           │                                     │                       │
│           ▼                                     ▼                       │
│      ② Refund                             ② Transfer                    │
│           │                                     │                       │
│           ▼                                     ▼                       │
│   Customer receives                      Seller receives                │
│   €50 back                               €43.25 (minus commission)      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `balance_insufficient` | Funds in pending, not available | Use `tok_bypassPending` or wait for funds to settle |
| `balance_insufficient` | Currency mismatch | Transfer in same currency as balance (check with `/v1/balance`) |
| `account_invalid` | Connected account not verified | Complete onboarding, check `payouts_enabled` |
| `amount_too_large` | Transfer exceeds available balance | Check balance first, transfer less |

---

## Production Considerations

1. **Pending Period:** Without `tok_bypassPending`, funds take 2-7 days to become available. Plan your transfer timing accordingly.

2. **Currency:** Charge and transfer in the same currency, or manage multi-currency balances.

3. **Fees:** Stripe fees reduce your net amount. Consider who absorbs the fee (platform or customer).

4. **Webhooks:** In production, listen for:
   - `charge.succeeded` — payment completed
   - `transfer.created` — transfer initiated
   - `account.updated` — seller account status changed

5. **Idempotency:** Use idempotency keys for retries:
   ```bash
   -H "Idempotency-Key: unique_request_id"
   ```
