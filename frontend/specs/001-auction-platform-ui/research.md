# Research: Auction Platform UI

**Feature**: 001-auction-platform-ui
**Date**: 2026-01-05

## Research Topics

### 1. Keycloak React Integration

**Decision**: Use `@react-keycloak/web` adapter

**Rationale**:
- Official community adapter with active maintenance
- Provides React context and hooks for auth state
- Handles token refresh automatically
- Compatible with Keycloak 18+ and React 18+

**Alternatives Considered**:
- **keycloak-js directly**: More complex, requires manual React integration
- **oidc-client-ts**: Generic OIDC, loses Keycloak-specific features
- **Custom implementation**: Unnecessary complexity, reinventing the wheel

**Integration Pattern**:
```typescript
// src/lib/keycloak.ts
import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});

// Registration redirects to Keycloak's registration page
// Login uses standard OAuth2 authorization code flow
// Token stored in memory (not localStorage) for security
```

**Key Flows**:
- **Registration**: Redirect to Keycloak registration form → callback with token
- **Login**: Redirect to Keycloak login → callback with token
- **Logout**: Clear token + redirect to Keycloak logout endpoint
- **Token Refresh**: Automatic via adapter, transparent to application

---

### 2. Stripe Integration Strategy

**Decision**: Use `@stripe/react-stripe-js` with Stripe Elements

**Rationale**:
- Official React library from Stripe
- PCI-compliant card collection (card data never touches our servers)
- Consistent styling with shadcn via CSS variables
- Supports both payment method collection and Stripe Connect

**Alternatives Considered**:
- **Stripe Checkout (hosted)**: Less customization, breaks UX flow
- **Direct API calls**: PCI compliance nightmare, not recommended
- **Payment Request API only**: Limited browser support

**Buyer Payment Methods**:
```typescript
// Collect payment method via Stripe Elements
// Store payment method ID on backend via API call
// Display saved payment methods from backend
```

**Seller Onboarding (Stripe Connect)**:
```typescript
// 1. Backend creates Connect account link
// 2. Frontend redirects seller to Stripe-hosted onboarding
// 3. Seller completes onboarding on Stripe
// 4. Stripe redirects back to frontend with success/failure
// 5. Backend verifies account status
```

**Key Components**:
- `PaymentMethodForm`: Stripe Elements for card input
- `SavedPaymentMethods`: List of stored payment methods
- `SellerOnboarding`: Redirect-based Connect flow

---

### 3. Mock Data Strategy

**Decision**: Use MSW (Mock Service Worker) for API mocking

**Rationale**:
- Intercepts network requests at service worker level
- Works with TanStack Query without modification
- Same code works in browser and tests
- Gradual migration to real API by removing handlers

**Alternatives Considered**:
- **Static JSON imports**: Doesn't test API layer, no request/response cycle
- **json-server**: Requires running separate process, more setup
- **Mirage.js**: Similar to MSW but less active development

**Implementation**:
```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/listings', () => {
    return HttpResponse.json(mockListings);
  }),
  http.post('/api/bids', async ({ request }) => {
    const bid = await request.json();
    // Validate and return mock response
    return HttpResponse.json({ success: true, bid });
  }),
];

// src/mocks/browser.ts
import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';

export const worker = setupWorker(...handlers);
```

**Mock Data Files**:
- `mocks/data/listings.json`: 20+ sample auction listings
- `mocks/data/users.json`: 5 sample users (buyers/sellers)
- `mocks/data/bids.json`: Bid history for listings
- `mocks/data/orders.json`: Sample orders in various states

---

### 4. Routing Architecture

**Decision**: React Router v6 with lazy loading

**Rationale**:
- Industry standard for React SPAs
- Data loading patterns align with TanStack Query
- Lazy loading for code splitting
- Protected route patterns well-documented

**Route Structure**:
```typescript
// Public routes
/                     → HomePage
/listings             → ListingsPage
/listings/:id         → ListingDetailPage
/storefront/:id       → StorefrontPage (public view)
/login                → Redirect to Keycloak

// Protected routes (require auth)
/profile              → ProfilePage
/profile/payments     → PaymentMethodsPage
/my-bids              → MyBidsPage
/my-orders            → MyOrdersPage
/proposals            → ProposalsPage

// Seller routes (require seller role)
/seller/dashboard     → SellerDashboardPage
/seller/listings/new  → CreateListingPage
/seller/listings/:id  → EditListingPage
/seller/orders        → SellerOrdersPage
```

**Protected Route Pattern**:
```typescript
// Uses useAuth hook to check authentication
// Redirects to Keycloak login if not authenticated
// Checks user roles for seller-specific routes
```

---

### 5. Form Handling

**Decision**: React Hook Form with Zod resolver

**Rationale**:
- Performant (uncontrolled inputs by default)
- Excellent TypeScript support
- Zod resolver provides schema-based validation
- Aligns with constitution requirement for Zod

**Alternatives Considered**:
- **Formik**: More verbose, less performant
- **Native forms**: Missing validation integration
- **TanStack Form**: Newer, less ecosystem support

**Pattern**:
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { bidSchema, type Bid } from '@/schemas/bid';

const { register, handleSubmit, formState } = useForm<Bid>({
  resolver: zodResolver(bidSchema),
});
```

---

### 6. Real-time Strategy (Deferred)

**Decision**: Defer WebSocket implementation; use optimistic updates + manual refresh initially

**Rationale**:
- WebSocket requires backend infrastructure
- Optimistic updates provide good UX for bidding
- Manual refresh/polling acceptable for MVP
- Can add WebSocket layer later without major refactor

**Initial Approach**:
- Optimistic bid updates via TanStack Query mutation
- Invalidate queries on user action (view refresh)
- Add "Refresh" button for manual updates
- Show "last updated" timestamp

**Future WebSocket Integration Point**:
```typescript
// src/lib/websocket.ts (future)
// Will subscribe to auction channels
// Will trigger query invalidation on server events
// Will update notification store for real-time alerts
```

---

### 7. Image Handling

**Decision**: Direct upload to backend endpoint; display via URL

**Rationale**:
- Backend handles storage (S3/similar)
- Frontend only needs upload UI and display
- Keeps frontend stateless regarding assets

**Implementation**:
- Drag-and-drop upload component using `react-dropzone`
- Preview before upload
- Progress indicator during upload
- Backend returns URL after upload
- Display images via `<img>` with lazy loading

---

## Dependencies to Add

Based on research, these packages are needed (all permitted per constitution):

```json
{
  "dependencies": {
    "@react-keycloak/web": "^3.4.0",
    "keycloak-js": "^24.0.0",
    "@stripe/react-stripe-js": "^2.4.0",
    "@stripe/stripe-js": "^2.4.0",
    "react-router-dom": "^6.20.0",
    "react-hook-form": "^7.48.0",
    "@hookform/resolvers": "^3.3.0",
    "react-dropzone": "^14.2.0",
    "date-fns": "^3.0.0"
  },
  "devDependencies": {
    "msw": "^2.0.0"
  }
}
```

**Constitution Compliance**:
- All packages are utilities or dev tools (permitted)
- No overlap with locked stack categories
- No alternative UI/state/validation libraries
