# Implementation Plan: Auction Platform UI

**Branch**: `001-auction-platform-ui` | **Date**: 2026-01-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/001-auction-platform-ui/spec.md`

## Summary

Build a complete auction platform frontend enabling users to browse/bid on items, create storefronts, manage listings, handle purchase proposals, and track orders. Authentication via Keycloak OAuth2, payments via Stripe integration. Initial implementation uses mocked data; real-time features via WebSocket planned for later phase.

## Technical Context

**Language/Version**: TypeScript 5.x with React 18+
**Primary Dependencies**: shadcn/ui, Tailwind CSS, TanStack Query, Axios, Zustand, Zod
**Storage**: N/A (frontend only - backend handles persistence)
**Testing**: Vitest (if tests required)
**Target Platform**: Web browsers (modern evergreen browsers)
**Project Type**: Frontend SPA (Single Page Application)
**Performance Goals**: 60fps animations, <100ms UI interactions, <2s bid update visibility
**Constraints**: Mobile-responsive, WCAG 2.1 AA accessibility
**Scale/Scope**: ~15 pages, ~40 shared components, 7 Zustand stores

**Additional Technology (per user input)**:
- **Authentication**: Keycloak OAuth2 (registration via Keycloak hosted form, login returns JWT)
- **Payments**: Stripe Elements for buyer payment methods, Stripe Connect for seller onboarding
- **Data Strategy**: Phase 1 uses mocked JSON data; Phase 2+ integrates with backend APIs
- **Real-time**: WebSocket integration deferred to later phase; initial implementation uses polling or manual refresh

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Locked Technology Stack | ✅ PASS | Using approved stack only; Keycloak/Stripe are external services, not added libraries |
| II. Styling Hierarchy | ✅ PASS | shadcn → Tailwind → raw CSS (last resort) |
| III. Schema Validation with Zod | ✅ PASS | All API responses and forms validated with Zod |
| IV. Data Fetching Architecture | ✅ PASS | TanStack Query + Axios for all remote data |
| V. State Management with Zustand | ✅ PASS | Zustand for UI state; server state in TanStack Query |
| VI. Component Architecture | ✅ PASS | One component per file, PascalCase, /shared + /pages structure |
| VII. Design Fidelity | ⚠️ PENDING | Design files in /design required before implementation |
| VIII. Frontend-Only Scope | ✅ PASS | All work confined to frontend/ directory |

**Gate Result**: PASS (Principle VII requires design files but does not block planning)

## Project Structure

### Documentation (this feature)

```text
specs/001-auction-platform-ui/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (API mocks)
│   ├── auth.json        # Keycloak integration contracts
│   ├── listings.json    # Auction listings API
│   ├── bids.json        # Bidding API
│   ├── storefronts.json # Seller storefronts API
│   ├── orders.json      # Order fulfillment API
│   └── payments.json    # Stripe integration contracts
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (frontend directory)

```text
src/
├── pages/               # Page-level components
│   ├── HomePage.tsx
│   ├── ListingsPage.tsx
│   ├── ListingDetailPage.tsx
│   ├── LoginPage.tsx
│   ├── ProfilePage.tsx
│   ├── StorefrontPage.tsx
│   ├── CreateListingPage.tsx
│   ├── MyBidsPage.tsx
│   ├── MyOrdersPage.tsx
│   ├── ProposalsPage.tsx
│   └── SellerDashboardPage.tsx
├── shared/              # Reusable components
│   ├── ListingCard.tsx
│   ├── BidForm.tsx
│   ├── CountdownTimer.tsx
│   ├── NotificationBell.tsx
│   ├── PaymentMethodForm.tsx
│   └── ... (~35 more)
├── hooks/               # Custom React hooks
│   ├── useAuth.ts
│   ├── useListings.ts
│   ├── useBids.ts
│   └── useNotifications.ts
├── stores/              # Zustand stores
│   ├── authStore.ts
│   ├── notificationStore.ts
│   ├── cartStore.ts
│   └── uiStore.ts
├── api/                 # API layer
│   ├── client.ts        # Axios instance
│   ├── auth.ts          # Auth queries/mutations
│   ├── listings.ts      # Listing queries/mutations
│   ├── bids.ts          # Bid queries/mutations
│   └── orders.ts        # Order queries/mutations
├── schemas/             # Zod schemas
│   ├── user.ts
│   ├── listing.ts
│   ├── bid.ts
│   ├── order.ts
│   └── payment.ts
├── types/               # TypeScript types (inferred from schemas)
├── mocks/               # Mock data for Phase 1
│   ├── listings.json
│   ├── users.json
│   └── handlers.ts      # MSW handlers (optional)
└── lib/                 # Utilities
    ├── keycloak.ts      # Keycloak adapter
    └── stripe.ts        # Stripe Elements setup
```

## Complexity Tracking

| Deviation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| External auth (Keycloak) | User requirement for OAuth2 | Self-built auth would duplicate backend responsibility |
| External payments (Stripe) | User requirement for payment handling | No simpler alternative for PCI compliance |
| /mocks directory | Phase 1 requires working UI without backend | Inline mock data would clutter components |
| /lib directory | Keycloak/Stripe adapters need isolation | Mixing with /api would conflate external services with internal API calls |

## Phase 0: Research Summary

See [research.md](./research.md) for detailed findings.

**Key Decisions**:
1. **Keycloak Integration**: Use `@react-keycloak/web` adapter for React integration
2. **Stripe Integration**: Use `@stripe/react-stripe-js` for Elements, Stripe Connect handled via redirect flow
3. **Mock Data Strategy**: Use MSW (Mock Service Worker) for intercepting API calls during development
4. **Routing**: React Router v6 for client-side routing
5. **Form Handling**: React Hook Form with Zod resolver for validation

## Phase 1: Design Artifacts

### Data Model
See [data-model.md](./data-model.md) for complete entity definitions.

### API Contracts
API contracts will emerge from implementation. Schemas and types will be defined as needed during development.

### Quickstart Guide
See [quickstart.md](./quickstart.md) for development setup instructions.
