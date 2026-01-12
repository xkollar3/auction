# Tasks: Auction Platform UI

**Input**: Design from `design/` and user specifications
**Prerequisites**: plan.md, spec.md

**Scope**: Landing page and listings browsing complete. Now implementing User Management with Keycloak authentication and Stripe payment integration.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Path Conventions

All paths relative to `frontend/`:

- **Pages**: `src/pages/`
- **Shared components**: `src/shared/`
- **Mock data**: `src/mocks/`
- **Types**: `src/types/`
- **Hooks**: `src/hooks/`
- **Lib**: `src/lib/` - External service adapters (Keycloak, Stripe)
- **API**: `src/api/` - API client functions
- **Stores**: `src/stores/` - Zustand state stores
- **Schemas**: `src/schemas/` - Zod validation schemas

---

## Phase 1: Setup (COMPLETED)

**Purpose**: Project scaffolding and dependencies

- [X] T001 Install required dependencies: react-router-dom, lucide-react (for icons) via `npm install`
- [X] T002 [P] Create directory structure: `src/pages/`, `src/shared/`, `src/mocks/`, `src/types/`
- [X] T003 [P] Configure React Router in `src/App.tsx` with route for `/` pointing to HomePage

**Checkpoint**: Project runs with empty HomePage at localhost

---

## Phase 2: Foundational (Shared Components) (COMPLETED)

**Purpose**: Create reusable components needed by the landing page

- [X] T004 [P] Create `src/shared/Header.tsx` - Navigation header with logo, nav links (Categories, How it Works, Sell), search bar (empty callback), notification bell, Login/Sign Up buttons (navigate to placeholder routes)
- [X] T005 [P] Create `src/shared/Footer.tsx` - Footer with logo, description, Quick Links, Support, Legal columns, social icons, copyright
- [X] T006 [P] Create `src/shared/ListingCard.tsx` - Auction item card with image, title, current bid, countdown timer display, "Place Bid" button (empty callback)
- [X] T007 [P] Create `src/shared/CategoryCard.tsx` - Category card with icon, name, item count
- [X] T008 [P] Create `src/shared/HowItWorksStep.tsx` - Step card with number badge, icon, title, description

**Checkpoint**: All shared components render independently

---

## Phase 3: Landing Page - User Story 1 Part A (Priority: P1) (COMPLETED)

**Goal**: Implement the complete landing page matching design/landing-page/*.png

**Independent Test**: Open localhost:5173, see fully styled landing page with mock auction items

### Mock Data

- [X] T009 [P] [US1] Create `src/types/listing.ts` - TypeScript interface for ListingCardData (id, title, imageUrl, currentBid, startingPrice, endTime, bidCount)
- [X] T010 [P] [US1] Create `src/types/category.ts` - TypeScript interface for CategoryData (id, name, icon, itemCount)
- [X] T011 [US1] Create `src/mocks/listings.ts` - Array of 8 mock listings with placeholder image URLs (use picsum.photos), varied prices and end times

### Page Implementation

- [X] T012 [US1] Create `src/pages/HomePage.tsx` - Main landing page component composing all sections
- [X] T013 [US1] Implement Hero section in HomePage - Blue gradient background, headline "Discover Unique Items, Bid with Confidence", subtext, stats row (12,543 auctions, 85,000+ bidders, 247 ending today), two CTA buttons (Create Free Account → /register, Browse Auctions → /listings)
- [X] T014 [US1] Implement Live Auctions section in HomePage - Section title "Live Auctions" with "View All" link, 4x2 grid of ListingCard components using mock data, "Place Bid" buttons with empty onClick handlers
- [X] T015 [US1] Implement CTA Banner section in HomePage - Blue background, "Want to place a bid?" heading, "Sign Up Now - It's Free" button (→ /register)
- [X] T016 [US1] Implement Browse by Category section in HomePage - 2x4 grid of CategoryCard components for: Watches, Cameras, Fashion, Furniture, Art, Jewelry, Electronics, Music (use lucide-react icons)
- [X] T017 [US1] Implement How It Works section in HomePage - 4 HowItWorksStep components: Create Account, Browse Auctions, Place Your Bids, Win & Enjoy, with "Get Started Now" CTA button
- [X] T018 [US1] Integrate Header and Footer in HomePage - Header at top, Footer at bottom, main content between

**Checkpoint**: Landing page fully styled and viewable at localhost:5173

---

## Phase 4: Landing Page Polish (COMPLETED)

**Purpose**: Final styling and responsiveness

- [X] T019 [P] Add responsive styling to Header - Mobile hamburger menu consideration (can be empty callback for now)
- [X] T020 [P] Add responsive styling to ListingCard grid - 1 column mobile, 2 tablet, 4 desktop
- [X] T021 [P] Add responsive styling to CategoryCard grid - 2 columns mobile, 4 desktop
- [X] T022 Verify all button redirects work (Login → /login, Sign Up → /register, Browse Auctions → /listings, Category cards → /listings?category=X)
- [X] T023 Test page loads without errors in browser console

---

## Phase 5: Listings Browsing Page (COMPLETED)

**Goal**: Implement the listings browsing page with search, filtering, sorting, and infinite scroll pagination

**Independent Test**: Navigate to /listings (via search bar or header link), see paginated list of auction items with working filters and infinite scroll

### Types & Mock Data

- [X] T024 [P] [US1] Extend `src/types/listing.ts` - Add ListingFilterParams interface (query?: string, category?: string, sortBy: 'price_asc' | 'price_desc' | 'ending_soon' | 'hot', page: number)
- [X] T025 [P] [US1] Extend `src/types/listing.ts` - Add ListingPageData interface (listings: ListingCardData[], hasMore: boolean, totalCount: number)
- [X] T026 [US1] Extend `src/mocks/listings.ts` - Add 24+ mock listings with varied categories, prices, endTimes, and bidCounts (for pagination testing). Add recentBids field to ListingCardData for "hot" sorting

### Shared Components

- [X] T027 [P] [US1] Create `src/shared/SearchBar.tsx` - Standalone search input component with search icon, placeholder "Search auctions...", onSearch callback that navigates to /listings?q={query}
- [X] T028 [P] [US1] Create `src/shared/CategoryFilter.tsx` - Dropdown or button group to select category filter (All, Watches, Cameras, Fashion, Furniture, Art, Jewelry, Electronics, Music), onChange callback
- [X] T029 [P] [US1] Create `src/shared/SortSelect.tsx` - Dropdown to select sort option: "Price: Low to High", "Price: High to Low", "Ending Soon", "Hot" (most bids in last 10 mins), onChange callback
- [X] T030 [P] [US1] Create `src/shared/ListingGrid.tsx` - Responsive grid wrapper for ListingCard components (1 col mobile, 2 tablet, 4 desktop), accepts listings array
- [X] T031 [P] [US1] Create `src/shared/LoadingSpinner.tsx` - Simple loading spinner component for infinite scroll loading state
- [X] T032 [P] [US1] Create `src/shared/EmptyState.tsx` - Empty state component for "No listings found" with optional message and icon

### Hooks

- [X] T033 [US1] Create `src/hooks/useListingsFilter.ts` - Custom hook to manage filter state (query, category, sortBy), parse URL params on mount, update URL on change, return { filters, setQuery, setCategory, setSortBy }
- [X] T034 [US1] Create `src/hooks/useInfiniteScroll.ts` - Custom hook for infinite scroll: accepts callback, returns { observerRef, isLoading }. Uses IntersectionObserver to detect when sentinel element is visible

### Mock API Layer

- [X] T035 [US1] Create `src/mocks/listingsApi.ts` - Mock API function `fetchListings(params: ListingFilterParams): Promise<ListingPageData>` that filters/sorts mock data and returns paginated results (8 items per page). Implements: full-text search on title (just string includes), category filter, sort by price/ending_soon/hot

### Page Implementation

- [X] T036 [US1] Create `src/pages/ListingsPage.tsx` - Main listings browsing page with Header, search/filter controls, listing grid, and Footer
- [X] T037 [US1] Implement filter bar in ListingsPage - Horizontal bar with SearchBar (pre-filled from URL ?q param), CategoryFilter (pre-selected from URL ?category param), SortSelect
- [X] T038 [US1] Implement listings grid section in ListingsPage - Use ListingGrid component, display listings from mock API, show LoadingSpinner while loading, show EmptyState if no results
- [X] T039 [US1] Implement infinite scroll in ListingsPage - Use useInfiniteScroll hook, add sentinel div at bottom of grid, load next page when sentinel visible, append new listings to existing, stop when hasMore=false
- [X] T040 [US1] Integrate Header search with ListingsPage - Update Header.tsx to navigate to /listings?q={query} on search submit, pre-fill search input if on ListingsPage with existing query

**Checkpoint**: Listings page fully functional with search, category filter, sorting, and infinite scroll pagination

---

## Phase 6: Listings Page Polish (COMPLETED)

**Purpose**: Final styling, responsiveness, and UX polish

- [X] T041 [P] Add responsive styling to filter bar - Stack filters vertically on mobile, horizontal on desktop
- [ ] T042 [P] Add loading skeleton to ListingGrid while initial data loads (OPTIONAL)
- [ ] T043 [P] Add "Back to top" button that appears after scrolling down (OPTIONAL)
- [X] T044 Verify URL state sync - Filters should update URL, page reload should restore filter state
- [X] T045 Test page loads without errors in browser console
- [X] T046 Verify navigation from HomePage to ListingsPage works via all entry points (search, Browse Auctions, View All, category cards)

---

## Phase 7: Homepage Preview Fix (COMPLETED)

**Goal**: Fix HomePage to show only 8 featured listings instead of all listings from the expanded mock data

- [X] T047 [US1] Create `src/mocks/homeApi.ts` - Mock API function `fetchFeaturedListings(): Promise<ListingCardData[]>` that returns 8 listings sorted by endTime (ending soonest first) for homepage preview
- [X] T048 [US1] Update `src/pages/HomePage.tsx` - Replace direct mockListings import with fetchFeaturedListings API call, add loading state, display exactly 8 items in the Live Auctions section

**Checkpoint**: HomePage displays exactly 8 featured listings (ending soonest), using the same mock data source but through a dedicated API

---

# NEW PHASE: User Management & Payment Integration

**Context**: New project phase - Keycloak authentication and Stripe payment methods integration
**Keycloak Config**: localhost:8089, realm "auction marketplace", client TBD
**Stripe Integration**: Mock backend calls for seller/buyer account creation and payment methods

---

## Phase 8: User Management Setup (Shared Infrastructure)

**Purpose**: Install dependencies and configure external services

- [X] T049 Install Keycloak dependencies (@react-keycloak/web, keycloak-js) via npm install
- [X] T050 Install Stripe dependencies (@stripe/react-stripe-js, @stripe/stripe-js) via npm install
- [X] T051 [P] Create environment variable configuration for Keycloak (VITE_KEYCLOAK_URL=http://localhost:8089, VITE_KEYCLOAK_REALM=auction-marketplace, VITE_KEYCLOAK_CLIENT_ID=auction-ui-client)
- [X] T052 [P] Create environment variable configuration for Stripe (VITE_STRIPE_PUBLISHABLE_KEY)

---

## Phase 9: User Management Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user management story can be implemented

**⚠️ CRITICAL**: No user management work can begin until this phase is complete

- [X] T053 [P] Create Keycloak client configuration in src/lib/keycloak.ts
- [X] T054 [P] Create Stripe client configuration in src/lib/stripe.ts
- [X] T055 [P] Setup ReactKeycloakProvider wrapper in src/App.tsx
- [X] T056 [P] Setup StripeElementsProvider wrapper in src/App.tsx
- [X] T057 Create authentication store in src/stores/authStore.ts (user state, token, isAuthenticated, setUser, clearUser)
- [X] T058 [P] Create useAuth hook in src/hooks/useAuth.ts (exposes auth store + Keycloak instance methods)
- [X] T059 [P] Create user schema in src/schemas/user.ts (Zod schema for user profile)
- [X] T060 [P] Create user types in src/types/user.ts (User, UserProfile interfaces)
- [X] T061 Create protected route wrapper component in src/shared/ProtectedRoute.tsx (redirects to Keycloak if not authenticated)
- [X] T062 Configure routes for user management pages in src/App.tsx (/profile, /profile/payments, /profile/payments/setup)

**Checkpoint**: ✅ Foundation ready - user management implementation can now begin

---

## Phase 10: User Story - Authentication with Keycloak (Priority: P1) 🎯 MVP

**Goal**: Enable users to register and log in via Keycloak, with JWT-based authentication

**Independent Test**: User can click sign up, complete registration in Keycloak, be redirected back with valid JWT, and see their logged-in state. Existing users can log in and be redirected back authenticated.

### Implementation

- [X] T063 [P] [US-AUTH] Create SignUpButton component in src/shared/SignUpButton.tsx (redirects to Keycloak registration via keycloak.register())
- [X] T064 [P] [US-AUTH] Create LoginButton component in src/shared/LoginButton.tsx (redirects to Keycloak login via keycloak.login())
- [X] T065 [P] [US-AUTH] Create LogoutButton component in src/shared/LogoutButton.tsx (calls keycloak.logout())
- [X] T066 [US-AUTH] Implement Keycloak redirect handlers for registration callback in src/lib/keycloak.ts
- [X] T067 [US-AUTH] Implement Keycloak redirect handlers for login callback in src/lib/keycloak.ts
- [X] T068 [US-AUTH] Add JWT token storage and retrieval logic in src/stores/authStore.ts
- [X] T069 [US-AUTH] Update Header component to show SignUp/Login buttons when not authenticated in src/shared/Header.tsx
- [X] T070 [US-AUTH] Update Header component to show user display name and Logout when authenticated in src/shared/Header.tsx

**Checkpoint**: ✅ Users can register/login via Keycloak and JWT authentication works

---

## Phase 11: User Story - User Profile Display (Priority: P2)

**Goal**: Display read-only user profile details after login, sourced from Keycloak token

**Independent Test**: After logging in, navigate to /profile and verify user details (name, email, phone) are displayed correctly and are read-only.

### Implementation

- [X] T071 [P] [US-PROFILE] Create profile schema in src/schemas/profile.ts (Zod schema for profile display)
- [X] T072 [P] [US-PROFILE] Create profile types in src/types/profile.ts (ProfileData interface)
- [X] T073 [P] [US-PROFILE] Create UserProfile component in src/shared/UserProfile.tsx (displays user details read-only in card format)
- [X] T074 [US-PROFILE] Create ProfilePage component in src/pages/ProfilePage.tsx
- [X] T075 [US-PROFILE] Extract user details from Keycloak JWT token (name, email, phone) in src/hooks/useAuth.ts
- [X] T076 [US-PROFILE] Add protected route for /profile in src/App.tsx
- [X] T077 [US-PROFILE] Add navigation link to profile from Header user menu in src/shared/Header.tsx

**Checkpoint**: ✅ Users can view their profile details after login

---

## Phase 12: User Story - Account Deletion (Priority: P3) ⚠️ OPTIONAL

**Goal**: Allow users to delete their entire account from Keycloak using their logged-in token

**Independent Test**: Navigate to profile settings, click delete account, confirm deletion, and verify account is removed from Keycloak.

**NOTE**: This story is OPTIONAL - only implement if Keycloak provides an API endpoint for account deletion using the user's token.

### Implementation

- [ ] T078 [US-DELETE] Research Keycloak account deletion API endpoint and token requirements (check Keycloak Account REST API)
- [ ] T079 [P] [US-DELETE] Create account deletion schema in src/schemas/account.ts
- [ ] T080 [P] [US-DELETE] Create account deletion API function in src/api/auth.ts (calls Keycloak account API)
- [ ] T081 [US-DELETE] Create DeleteAccountButton component in src/shared/DeleteAccountButton.tsx (with confirmation dialog using shadcn AlertDialog)
- [ ] T082 [US-DELETE] Add delete account mutation using TanStack Query in src/hooks/useAuth.ts
- [ ] T083 [US-DELETE] Integrate DeleteAccountButton into ProfilePage in src/pages/ProfilePage.tsx
- [ ] T084 [US-DELETE] Handle successful deletion by logging out and redirecting to home page

**Checkpoint**: Users can delete their account if Keycloak API supports it

---

## Phase 13: User Story - Seller Account Creation (Priority: P4)

**Goal**: Allow users to create a Stripe seller account (Connect) to receive payments

**Independent Test**: User clicks "Become a Seller" button, request is sent to mocked backend API with JWT, backend returns sellerId and onboarding/dashboard URLs, user sees success message with links.

### Implementation

- [X] T085 [P] [US-SELLER] Create seller account schema in src/schemas/seller.ts (Zod schema for seller account creation)
- [X] T086 [P] [US-SELLER] Create seller account types in src/types/seller.ts (SellerAccount, SellerAccountResponse interfaces)
- [X] T087 [P] [US-SELLER] Create Stripe types in src/types/stripe.ts (CreateSellerAccountRequest, CreateSellerAccountResponse)
- [X] T088 [US-SELLER] Create mocked POST /user/stripe/seller-account endpoint in src/api/stripe.ts (returns sellerId, onboardingUrl, dashboardUrl)
- [X] T089 [US-SELLER] Create API function for creating seller account in src/api/stripe.ts (mock function with hardcoded values)
- [X] T090 [P] [US-SELLER] Create BecomeSellerButton component in src/shared/BecomeSellerButton.tsx
- [X] T091 [US-SELLER] Create seller account mutation using TanStack Query in src/hooks/useSeller.ts
- [X] T092 [US-SELLER] Handle response and display onboarding/dashboard URLs in BecomeSellerButton
- [X] T093 [US-SELLER] Add BecomeSellerButton to ProfilePage in src/pages/ProfilePage.tsx
- [X] T094 [P] [US-SELLER] Create SellerDashboardLink component (integrated into BecomeSellerButton)

**Checkpoint**: ✅ Users can create a seller account and see Stripe onboarding/dashboard URLs

---

## Phase 14: User Story - Buyer Account & Address (Priority: P5)

**Goal**: Allow users to create a Stripe customer account by providing their address

**Independent Test**: User clicks "Add Payment Method", fills out address form, submits to mocked backend, receives customerId in response and can proceed to payment method setup.

### Implementation

- [X] T095 [P] [US-BUYER] Create CreateStripeCustomerRequest schema in src/schemas/customer.ts (Zod schema with line1, line2, city, state, postalCode, country)
- [X] T096 [P] [US-BUYER] Create customer types in src/types/customer.ts (CreateStripeCustomerRequest, CreateStripeCustomerResponse interfaces)
- [X] T097 [US-BUYER] Create mocked POST /user/stripe/customer endpoint in src/mocks/handlers.ts (accepts address, returns customerId)
- [X] T098 [US-BUYER] Create API function for creating customer in src/api/stripe.ts
- [X] T099 [P] [US-BUYER] Create AddressForm component in src/shared/AddressForm.tsx with React Hook Form + Zod validation
- [X] T100 [US-BUYER] Create customer creation mutation using TanStack Query in src/hooks/useCustomer.ts
- [X] T101 [US-BUYER] Store customerId in authStore after successful creation in src/stores/authStore.ts
- [X] T102 [US-BUYER] Create PaymentSetupPage component in src/pages/PaymentSetupPage.tsx
- [X] T103 [US-BUYER] Add protected route for /profile/payments/setup in src/App.tsx
- [X] T104 [US-BUYER] Add navigation link to payment setup from ProfilePage

**Checkpoint**: ✅ Users can create a Stripe customer account by providing address

---

## Phase 15: User Story - Payment Method Setup (Priority: P6)

**Goal**: Allow users to add a payment method using Stripe Elements and SetupIntent

**Independent Test**: After creating customer account, user can request a SetupIntent, enter card details via Stripe Elements, confirm card, and save payment method ID to backend.

### Implementation

- [X] T105 [P] [US-PAYMENT] Create SetupIntent types in src/types/stripe.ts (SetupIntentRequest, SetupIntentResponse with clientSecret)
- [X] T106 [P] [US-PAYMENT] Create payment method schema in src/schemas/payment.ts
- [X] T107 [P] [US-PAYMENT] Create payment method types in src/types/payment.ts (PaymentMethod interface)
- [X] T108 [US-PAYMENT] Create mocked POST /user/stripe/setup-intent endpoint in src/mocks/handlers.ts (accepts customerId, returns clientSecret and setupIntentId)
- [X] T109 [US-PAYMENT] Create API function for creating setup intent in src/api/stripe.ts
- [X] T110 [P] [US-PAYMENT] Create CardForm component using Stripe CardElement in src/shared/CardForm.tsx
- [X] T111 [US-PAYMENT] Create setup intent mutation using TanStack Query in src/hooks/usePaymentMethods.ts
- [X] T112 [US-PAYMENT] Implement confirmCardSetup flow using Stripe.js in src/lib/stripe.ts
- [X] T113 [US-PAYMENT] Extract payment method ID from SetupIntent response after confirmation
- [X] T114 [US-PAYMENT] Create mocked POST /user/stripe/payment-method endpoint in src/mocks/handlers.ts (accepts paymentMethodId)
- [X] T115 [US-PAYMENT] Create API function for attaching payment method in src/api/stripe.ts
- [X] T116 [US-PAYMENT] Create payment method attachment mutation in src/hooks/usePaymentMethods.ts
- [X] T117 [US-PAYMENT] Integrate CardForm into PaymentSetupPage in src/pages/PaymentSetupPage.tsx
- [X] T118 [US-PAYMENT] Handle successful payment method save and display confirmation message

**Checkpoint**: ✅ Users can add a payment method using Stripe Elements

---

## Phase 16: User Story - Saved Payment Methods Display (Priority: P7)

**Goal**: Display user's saved payment methods on their profile

**Independent Test**: After adding payment methods, navigate to /profile/payments and see list of saved payment methods with card details (last 4 digits, brand, expiry).

### Implementation

- [X] T119 [P] [US-DISPLAY] Create payment methods list types in src/types/payment.ts (PaymentMethodCard with last4, brand, expiry, isDefault)
- [X] T120 [US-DISPLAY] Create mocked GET /user/stripe/payment-methods endpoint in src/mocks/handlers.ts (returns array of payment methods)
- [X] T121 [US-DISPLAY] Create API function for fetching payment methods in src/api/stripe.ts
- [X] T122 [P] [US-DISPLAY] Create PaymentMethodCard component in src/shared/PaymentMethodCard.tsx (displays card brand icon, last 4 digits, expiry date)
- [X] T123 [P] [US-DISPLAY] Create PaymentMethodsList component in src/shared/PaymentMethodsList.tsx
- [X] T124 [US-DISPLAY] Create payment methods query using TanStack Query in src/hooks/usePaymentMethods.ts
- [X] T125 [US-DISPLAY] Create PaymentMethodsPage component in src/pages/PaymentMethodsPage.tsx
- [X] T126 [US-DISPLAY] Add protected route for /profile/payments in src/App.tsx
- [X] T127 [US-DISPLAY] Add navigation link to payment methods from ProfilePage in src/pages/ProfilePage.tsx

**Checkpoint**: ✅ Users can view their saved payment methods

---

## Phase 17: User Management Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user management stories

- [ ] T128 [P] Add loading states for all API calls (Keycloak redirects, Stripe API calls)
- [ ] T129 [P] Add error handling and error messages for failed API calls using toast notifications
- [ ] T130 [P] Add success notifications for account creation, payment method addition
- [ ] T131 [P] Update Header navigation to include profile and payment management links in user menu
- [ ] T132 [P] Add responsive design verification for all new pages (profile, payment setup, payment methods)
- [ ] T133 [P] Add accessibility audit for forms and buttons (keyboard navigation, ARIA labels)
- [ ] T134 Create mock data for Keycloak user profiles in src/mocks/data/users.json (5 sample users)
- [ ] T135 Create mock data for Stripe customer/payment methods in src/mocks/data/payments.json
- [ ] T136 [P] Add environment variables documentation in README.md for Keycloak and Stripe configuration
- [ ] T137 [P] Add user management flow documentation in README.md
- [ ] T138 [P] Add paymentMethodId field to User interface in src/types/user.ts (for default payment method)
- [ ] T139 [P] Add paymentMethodId field to userSchema in src/schemas/user.ts
- [ ] T140 Add GET /user/profile endpoint call to fetch paymentMethodId from backend in src/api/user.ts
- [ ] T141 Update PaymentMethodsPage to display user's default payment method ID from backend in src/pages/PaymentMethodsPage.tsx
- [ ] T142 Add visual indicator for default payment method in PaymentMethodCard component in src/shared/PaymentMethodCard.tsx
- [ ] T143 Localize all payment amounts to CZK currency with Czech formatting (create src/lib/currency.ts utility, update all price displays in ListingCard, PaymentSetupPage, and other components to use CZK format)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1-7 (Landing Page & Listings)**: COMPLETED
- **Phase 8 (User Management Setup)**: Can start immediately
- **Phase 9 (User Management Foundational)**: Depends on Phase 8 completion - BLOCKS all user management stories
- **Phase 10-16 (User Management Stories)**: All depend on Phase 9 completion
  - User stories can proceed sequentially in priority order
  - Some user stories have dependencies:
    - US-PROFILE (Phase 11) depends on US-AUTH (Phase 10)
    - US-DELETE (Phase 12) depends on US-AUTH (Phase 10) - OPTIONAL
    - US-SELLER (Phase 13) depends on US-AUTH (Phase 10) - Independent of buyer flow
    - US-BUYER (Phase 14) depends on US-AUTH (Phase 10) - Independent of seller flow
    - US-PAYMENT (Phase 15) depends on US-BUYER (Phase 14 - needs customerId)
    - US-DISPLAY (Phase 16) depends on US-PAYMENT (Phase 15)
- **Phase 17 (Polish)**: Depends on all desired user management stories being complete

### Parallel Opportunities

- **Phase 8 tasks (T049-T052)**: Can all run in parallel
- **Phase 9 Foundational**: Tasks marked [P] can run in parallel (T053-T054, T055-T056, T058-T060)
- **Within each user story**: Tasks marked [P] can run in parallel
  - Phase 10: T063-T065 can run in parallel (button components)
  - Phase 11: T071-T073 can run in parallel
  - Phase 12: T079-T080 can run in parallel
  - Phase 13: T085-T087, T090, T094 can run in parallel
  - Phase 14: T095-T096, T099 can run in parallel
  - Phase 15: T105-T107, T110 can run in parallel
  - Phase 16: T119, T122-T123 can run in parallel
- **Independent flows**: Seller flow (Phase 13) and Buyer flow (Phase 14-16) are independent

---

## Implementation Notes

### Keycloak Configuration

| Variable | Value | Description |
|----------|-------|-------------|
| VITE_KEYCLOAK_URL | http://localhost:8089 | Keycloak server URL |
| VITE_KEYCLOAK_REALM | auction-marketplace | Realm name |
| VITE_KEYCLOAK_CLIENT_ID | auction-ui-client | Client ID (to be created) |

### Stripe Integration

| Variable | Value | Description |
|----------|-------|-------------|
| VITE_STRIPE_PUBLISHABLE_KEY | pk_test_... | Stripe publishable key |

### API Endpoints (Mocked)

| Endpoint | Method | Description |
|----------|--------|-------------|
| /user/stripe/seller-account | POST | Create Stripe seller account (Connect) |
| /user/stripe/customer | POST | Create Stripe customer with address |
| /user/stripe/setup-intent | POST | Create SetupIntent for payment method |
| /user/stripe/payment-method | POST | Attach payment method to customer |
| /user/stripe/payment-methods | GET | Get customer's saved payment methods |

### User Flow Diagrams

**Authentication Flow**:
1. User clicks Sign Up → Redirect to Keycloak registration
2. User fills form + phone → Keycloak redirects with JWT
3. Frontend stores JWT in authStore → User logged in

**Payment Method Flow**:
1. User provides address → Create Stripe customer → Get customerId
2. Request SetupIntent → Get clientSecret
3. User enters card via Stripe Elements → Confirm SetupIntent → Get paymentMethodId
4. Attach payment method to customer → Save paymentMethodId

---

## Total Task Count Summary

- **Completed**: 127 tasks (T001-T127 across Phases 1-16)
- **Remaining User Management Tasks**: 16 tasks (T128-T143 in Phase 17)
- **Total Tasks**: 143
- **Parallel Opportunities**: 35+ tasks marked [P]

### Tasks Per User Story

- **US-AUTH (Authentication)**: 8 tasks (COMPLETED)
- **US-PROFILE (Profile Display)**: 7 tasks (COMPLETED)
- **US-DELETE (Account Deletion)**: 7 tasks (OPTIONAL - SKIPPED)
- **US-SELLER (Seller Account)**: 10 tasks (COMPLETED)
- **US-BUYER (Buyer Account)**: 10 tasks (COMPLETED)
- **US-PAYMENT (Payment Setup)**: 14 tasks (COMPLETED)
- **US-DISPLAY (Payment Display)**: 9 tasks (COMPLETED)
- **Setup & Foundational**: 14 tasks (COMPLETED)
- **Polish**: 16 tasks (10 original + 5 payment method tasks + 1 CZK localization task)

### Suggested MVP Scope

**Minimal MVP** (Authentication + Profile):
- Phase 8: User Management Setup (4 tasks)
- Phase 9: User Management Foundational (10 tasks)
- Phase 10: Authentication (8 tasks)
- Phase 11: Profile Display (7 tasks)
- **Total: 29 tasks**

**Extended MVP** (+ Buyer Flow):
- Add Phase 14: Buyer Account (10 tasks)
- Add Phase 15: Payment Setup (14 tasks)
- Add Phase 16: Payment Display (9 tasks)
- **Total: 62 tasks**

---

## Format Validation

✅ All tasks follow the required format: `- [ ] [TaskID] [P?] [Story?] Description with file path`
✅ All tasks have sequential IDs (T001-T137)
✅ All user story tasks have story labels ([US-AUTH], [US-PROFILE], etc.)
✅ All parallelizable tasks marked with [P]
✅ All tasks include exact file paths
✅ Setup and Foundational phases have no story labels (correct)
✅ Polish phase has no story labels (correct)
