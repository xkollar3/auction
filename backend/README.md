# Auction Marketplace Backend

## User Context

This module handles user management, authentication, and payment integration within the auction marketplace.

### Keycloak Integration
Authentication is managed via **Keycloak**. 
- The application acts as an OIDC client (`auction-client`).
- Users authenticate to obtain an access token.
- This token is required for accessing protected endpoints (passed as `Authorization: Bearer <token>`).
- User identities in the local database are linked to Keycloak users.

### Stripe Integration
The application integrates with **Stripe** to handle payments and payouts. There is a distinction between **Buyer** and **Seller** accounts.

#### Buyer Account Creation
Buyers are Standard Stripe Customers.
- **Endpoint**: `POST /api/users/me/create-stripe-customer`
- **Flow**: Creates a Customer in Stripe and links it to the internal user profile via `StripeCustomerRegisteredEvent`.
- **Payment Methods**: Buyers can attach payment methods (cards) using a Setup Intent (`POST /api/users/me/setup-payment-intent`).

#### Seller Account Creation
Sellers are modeled as **Stripe Connected Accounts** (Express).
- **Endpoint**: `POST /api/users/me/create-seller-account`
- **Flow**:
    1. A Stripe Connected Account is created.
    2. An onboarding link (`Account Link`) is generated.
    3. The user is redirected to Stripe to complete KYC and onboarding.
    4. Upon completion, the user returns to the application.
- **Event Handling**: `StripeConnectedAccountCreatedEvent` triggers the assignment of the Seller Account ID to the user.

#### Webhook Integration
The application listens for Stripe webhooks to keep local state in sync.
- **Controller**: `StripeWebhookController` (`/api/v1/webhooks/stripe`)
- **Events**:
    - `account.updated`: Monitors the status of Connected Accounts.
    - Updates local seller status (enabled/disabled) based on `charges_enabled` and `payouts_enabled` flags from Stripe.

### Testing Scripts
Shell scripts for testing these flows are located in `docs/scripts`:
- `register-user.sh`: script for creating a new user in Keycloak/Application.
- `create-buyer-with-payments.sh`: Full flow to login, create a Stripe Customer, create a Payment Intent, and attach a payment method.
- `create-seller.sh`: Logs in a user, creates a Connected Account, and generates the onboarding link.
- `remove-user.sh`: script for removing a user from Keycloak/Application.
