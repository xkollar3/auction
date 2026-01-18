# Data Model: Auction Platform UI

**Feature**: 001-auction-platform-ui
**Date**: 2026-01-05

## Overview

This document defines the essential frontend data structures needed for initial implementation. Focus on input schemas for user actions. Backend response shapes will be refined during integration.

## Input Schemas (User Actions)

### Create Listing

Schema for seller creating a new auction listing.

```typescript
// src/schemas/createListing.ts
import { z } from 'zod';

export const createListingSchema = z.object({
  title: z.string().min(1).max(200),
  description: z.string().max(5000),
  category: z.string().min(1),
  images: z.array(z.string().url()).min(1).max(10),
  startingPrice: z.number().positive(),
  durationHours: z.number().int().min(1).max(168), // 1 hour to 7 days
});

export type CreateListingInput = z.infer<typeof createListingSchema>;
```

---

### Place Bid

Schema for buyer placing a bid on an active auction.

```typescript
// src/schemas/placeBid.ts
import { z } from 'zod';

export const placeBidSchema = z.object({
  listingId: z.string().uuid(),
  amount: z.number().positive(),
});

export type PlaceBidInput = z.infer<typeof placeBidSchema>;
```

---

### Create Storefront

Schema for user creating a seller storefront.

```typescript
// src/schemas/createStorefront.ts
import { z } from 'zod';

export const createStorefrontSchema = z.object({
  name: z.string().min(1).max(100),
  description: z.string().max(1000).optional(),
});

export type CreateStorefrontInput = z.infer<typeof createStorefrontSchema>;
```

---

### Respond to Proposal

Schema for responding to a purchase proposal.

```typescript
// src/schemas/respondProposal.ts
import { z } from 'zod';

export const respondProposalSchema = z.object({
  proposalId: z.string().uuid(),
  accept: z.boolean(),
});

export type RespondProposalInput = z.infer<typeof respondProposalSchema>;
```

---

### Add Tracking

Schema for seller adding tracking information to an order.

```typescript
// src/schemas/addTracking.ts
import { z } from 'zod';

export const addTrackingSchema = z.object({
  orderId: z.string().uuid(),
  trackingNumber: z.string().min(1),
  carrier: z.string().min(1), // e.g., "ups", "fedex", "usps"
});

export type AddTrackingInput = z.infer<typeof addTrackingSchema>;
```

---

## Display Types (Minimal)

These are the minimal shapes needed for UI rendering. Actual API responses may include more fields.

### Listing Card (list view)

```typescript
// Minimal data for ListingCard component
interface ListingCardData {
  id: string;
  title: string;
  imageUrl: string;
  currentBid: number | null;
  startingPrice: number;
  endTime: string; // ISO datetime
  bidCount: number;
}
```

### Listing Detail (detail view)

```typescript
// Additional data for ListingDetailPage
interface ListingDetailData extends ListingCardData {
  description: string;
  images: string[];
  seller: {
    id: string;
    displayName: string;
    storefrontName: string;
  };
  category: string;
  startTime: string;
  status: 'active' | 'ended' | 'sold' | 'unsold';
}
```

### Bid (for bid history display)

```typescript
interface BidData {
  id: string;
  amount: number;
  bidderDisplayName: string;
  timestamp: string;
  isWinning: boolean;
}
```

---

## Notes

- Full Zod schemas will be created during implementation
- Backend API contract will define complete response shapes
- Types will be inferred from Zod schemas using `z.infer<>`
- Additional validation rules may be added based on backend constraints
