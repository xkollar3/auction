# Quickstart: Auction Platform UI

**Feature**: 001-auction-platform-ui
**Date**: 2026-01-05

## Prerequisites

- Node.js 18+
- npm or pnpm

## Environment Setup

Create `.env.local` in the frontend root:

```env
# Keycloak (ask user if not responding)
VITE_KEYCLOAK_URL=http://localhost:8089
VITE_KEYCLOAK_REALM=auction-marketplace
VITE_KEYCLOAK_CLIENT_ID=auction-ui

# Stripe (test keys - ask user for actual key)
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...

# Mock mode for Phase 1
VITE_MOCK_API=true
```

## Installation

```bash
cd frontend
npm install
```

## Running the Application

```bash
npm run dev
```

Opens at http://localhost:5173

## Project Structure

```
src/
├── pages/           # Route pages
├── shared/          # Reusable components
├── hooks/           # Custom hooks
├── stores/          # Zustand stores
├── api/             # TanStack Query + Axios
├── schemas/         # Zod validation
├── types/           # TypeScript types
├── mocks/           # Mock data (Phase 1)
└── lib/             # Keycloak/Stripe adapters
```

## Notes

- If Keycloak is not responding, ask user for correct connection details
- If Stripe key is missing, ask user for test publishable key
- Phase 1 uses mock data; backend integration comes later
