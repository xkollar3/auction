# Auction platform - BidFlow

This repository contains the code for an auctioning platfrom MVP built for a course at FI MU - PV293.

https://is.muni.cz/auth/rozpis/tema?fakulta=1433;obdobi=9783;balik=556749;tema=556794;uplne_info=1

On this platform people are able to post listings for items they want to sell. Other users can place bids on a time bound auction and upon close the item is automatically sold to the highest bidder. Platform automatically tries to find alternative buyers among top 10 bidders if unable to sell to the first one.

Platfrom also manages orders and schedules deadlines for sellers that need to ship their sold packages. It acts as a middleman reserving money and depositing it when the sale is complete or returning the funds to customers in case of undelivered packages.

Platform attempts to be compliant with regulations for online payment by integrating stripe to manage funds.

Our business model makes money by taking off a commision (10 % for now) from each sale made.



## Architectural decision records
1. Event sourced architecture
    - Context: We are building an auctioning system with long business flows driven by user interactions with the platform
    - Decision: We will utilize an event source architecture to avoid storing state but rather source the state from events happenning on the platform

2. Axon framework usage
    - Context: ADR #1 led to use an event sourcing architecture, which means we need to use a framework to support this decision
    - Decision: We will use the Axon framework as it seems to be the most used framework for building DDD, CQRS event sourced systems in Java ecosystem
    - Rationale: We also considered using Eventuate, however eventuate is less documented and has overall less contributors, and requires far more scaffold to get started (choosing a message broker, choosing a database for event store). Where as Axon has good documentation and also provides Axon server that can immediately serve as the enterprise bus and event store

3. Modular monolith architecture
    - Context: We need to choose a way to implement the main portion of the application the backend
    - Decision: We will use the modular monolith architecture because the system was analysed with event storming providing us clear boundaries for modules
    - Rationale: We are building the system based on events, SOA or Monolith make more sense for systems designed with strong consistent data in mind, in our case we designed by finding loosely coupled modules which left us to consider Microservices and Modulith. We will go with the modulith because the project is not big enough to warrant microservices (only 4 devs) and a modulith will allow us flexibility to switch to microservices if necessary

4. Stripe for management of payment flows
    - Context: Our platform is an ecommerce platform and it requires transferring funds across parties, we don't want to implement this as its a generic domain and requires sensitive data handling
    - Decision: We will utilize Stripe because it provides us with an API allowing us to build the platform as a middle man between our customers and sellers on the platform

5. Ship 24 for tracking of shipments
    - Context: Sales made on the platform need to be shipped and we need to make sure people do not get scammed by providing fake tracking numbers for fake delivery services. Secondly we cannot implement various courier services to listen for tracking updates
    - Decision: We will utilize ship24 because it is able to track packages across different couriers and provide events about the packages which we can deduce the delivery status from. It also provides exceptions when tracking numbers are fake or non existent

7. Postgresql for read models
    - Context: Since the backend is event sourced and we need to present data to users we will need a place to store the current representation of various states to be displayed on the UI or used elsewhere in the application
    - Decision: We will utilize psql as its an well known, well tooled and proven RDBMS

7. Keycloak for user management
    - Context: We need the users to have some sort of identity and be able to log in to the platform so we can associate their data and manage their profiles
    - Decision: We don't want to implement user management so we will utilize keycloak which uses OIDC and OAuth2
    - Rationale: This way we don't manage the users information and we can utillize OAuth2 to add various SSO options later to make the sign up to the platform simpler. Since keycloak supports OIDC the frontend will use it to log in the users and obtain JWT tokens which have integrity ensured and can be used to authenticate the users to the backend

8. Minio for auction image storage
    - Context: Users want to upload some images to show off what they are auctioning
    - Decision: Use minio because it implements the S3 protocol making storage accessible via REST

9. Module internal architecture
    - Context: We need to harmonize and implement each module with same principles in mind
    - Decision: We will follow a architecture best matching axon framework with packages - events/commands/aggregates, the project is simple enough that this should be sufficient
    - Rationale: We won't use layered architecture because our business logic is contained within aggregates and it is not a good choice for CQRS event sourced application as the business logic lives in command handlers not the services. We won't use clean architecture (or otherwise called ports and adapters or hexagonal or onion) because having to declare ports and implement adapters is an abstraction we don't need and would slow us down

## User Context - drozdma6
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

### User-context Testing Scripts
Shell scripts for testing these flows are located in `docs/scripts`:
- `register-user.sh`: script for creating a new user in Keycloak/Application.
- `create-buyer-with-payments.sh`: Full flow to login, create a Stripe Customer, create a Payment Intent, and attach a payment method.
- `create-seller.sh`: Logs in a user, creates a Connected Account, and generates the onboarding link.
- `remove-user.sh`: script for removing a user from Keycloak/Application.

## Auction Items Context - eduardmlyn
This module handles auction creation, bidding, real-time updates, and image management.

### Core Functionality
- Sellers create time-bound auctions with images
- Buyers place bids with real-time validation
- System maintains top 10 bids for settlement (sourced from `HighestBidSetEvent`)
- Automated deadline handling closes auctions and triggers settlement

### Event-Sourced Architecture
The `AuctionItem` aggregate emits events to track auction lifecycle:
- **BidPlacedEvent**: Every bid attempt (for analytics/tracking only)
- **HighestBidSetEvent**: Valid bid becomes new highest (used to source top 10 bids)
- **BidRejectedEvent**: Rejected bids (auction closed, amount too low)
- **AuctionClosedEvent**: Auction deadline reached (triggers settlement)

Top 10 bids are maintained for settlement, with each bidder having only one bid in the list (latest/highest).

### REST API
- **POST /api/auctions**: Create auction (sellers only)
- **GET /api/auctions**: Browse with filtering, sorting, pagination
- **GET /api/auctions/featured**: 8 auctions ending soonest
- **GET /api/auctions/{id}**: Auction details with recent bids
- **GET /api/auctions/seller/{sellerId}**: Seller's auctions by status
- **GET /api/auctions/my-bids**: User's active bids
- **POST /api/auctions/{id}/bids**: Place a bid
- **POST /api/auctions/{id}/images**: Upload images (multipart)
- **GET /api/auctions/{id}/images**: Get image URLs

### WebSocket Integration
Real-time updates via **STOMP over SockJS**:
- Clients subscribe to `/topic/auction/{auctionItemId}`
- Bid updates pushed when `HighestBidSetEvent` occurs
- Auction closure notifications on `AuctionClosedEvent`

### Image Storage
**MinIO** (S3-compatible) integration:
- Multipart upload to `auctions/{auctionItemId}/` folder
- UUID-prefixed filenames prevent conflicts
- First image becomes preview in listings

### Deadline Management
Axon's `DeadlineManager` schedules automatic auction closure:
- Deadline set when auction created
- Triggers `CloseAuctionCommand` at expiry
- Emits `AuctionClosedEvent` with top 10 bids for settlement

### Read Models (PostgreSQL)
- **AuctionItemReadModel**: Listings with filtering/sorting
- **BidReadModel**: Bid history for analytics
- **AuctionItemImageReadModel**: Image URLs

Projection handlers update read models on events for efficient querying.

## Auction Settlements Context - adammajzlik
## Order context - xkollar3
- this context is responsible for managing the orders and financial flows
- manages payouts in case orders are delivered successfully and manages refunds in case of exception
- integrates stripe to work with users payment method and ensure payouts
- integrates ship24 to ensure validity of tracking numbers and tracking of orders
