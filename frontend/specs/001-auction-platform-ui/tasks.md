# Tasks: Landing Page

**Input**: Design from `design/landing-page/` and user specifications
**Prerequisites**: plan.md, spec.md

**Scope**: This task list covers ONLY the landing page (HomePage) implementation with static mock data. Other pages and features will be addressed in separate task lists.

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

---

## Phase 1: Setup

**Purpose**: Project scaffolding and dependencies

- [ ] T001 Install required dependencies: react-router-dom, lucide-react (for icons) via `npm install`
- [ ] T002 [P] Create directory structure: `src/pages/`, `src/shared/`, `src/mocks/`, `src/types/`
- [ ] T003 [P] Configure React Router in `src/App.tsx` with route for `/` pointing to HomePage

**Checkpoint**: Project runs with empty HomePage at localhost

---

## Phase 2: Foundational (Shared Components)

**Purpose**: Create reusable components needed by the landing page

- [ ] T004 [P] Create `src/shared/Header.tsx` - Navigation header with logo, nav links (Categories, How it Works, Sell), search bar (empty callback), notification bell, Login/Sign Up buttons (navigate to placeholder routes)
- [ ] T005 [P] Create `src/shared/Footer.tsx` - Footer with logo, description, Quick Links, Support, Legal columns, social icons, copyright
- [ ] T006 [P] Create `src/shared/ListingCard.tsx` - Auction item card with image, title, current bid, countdown timer display, "Place Bid" button (empty callback)
- [ ] T007 [P] Create `src/shared/CategoryCard.tsx` - Category card with icon, name, item count
- [ ] T008 [P] Create `src/shared/HowItWorksStep.tsx` - Step card with number badge, icon, title, description

**Checkpoint**: All shared components render independently

---

## Phase 3: Landing Page - User Story 1 (Priority: P1)

**Goal**: Implement the complete landing page matching design/landing-page/*.png

**Independent Test**: Open localhost:5173, see fully styled landing page with mock auction items

### Mock Data

- [ ] T009 [P] [US1] Create `src/types/listing.ts` - TypeScript interface for ListingCardData (id, title, imageUrl, currentBid, startingPrice, endTime, bidCount)
- [ ] T010 [P] [US1] Create `src/types/category.ts` - TypeScript interface for CategoryData (id, name, icon, itemCount)
- [ ] T011 [US1] Create `src/mocks/listings.ts` - Array of 8 mock listings with placeholder image URLs (use picsum.photos), varied prices and end times

### Page Implementation

- [ ] T012 [US1] Create `src/pages/HomePage.tsx` - Main landing page component composing all sections
- [ ] T013 [US1] Implement Hero section in HomePage - Blue gradient background, headline "Discover Unique Items, Bid with Confidence", subtext, stats row (12,543 auctions, 85,000+ bidders, 247 ending today), two CTA buttons (Create Free Account → /register, Browse Auctions → /listings)
- [ ] T014 [US1] Implement Live Auctions section in HomePage - Section title "Live Auctions" with "View All" link, 4x2 grid of ListingCard components using mock data, "Place Bid" buttons with empty onClick handlers
- [ ] T015 [US1] Implement CTA Banner section in HomePage - Blue background, "Want to place a bid?" heading, "Sign Up Now - It's Free" button (→ /register)
- [ ] T016 [US1] Implement Browse by Category section in HomePage - 2x4 grid of CategoryCard components for: Watches, Cameras, Fashion, Furniture, Art, Jewelry, Electronics, Music (use lucide-react icons)
- [ ] T017 [US1] Implement How It Works section in HomePage - 4 HowItWorksStep components: Create Account, Browse Auctions, Place Your Bids, Win & Enjoy, with "Get Started Now" CTA button
- [ ] T018 [US1] Integrate Header and Footer in HomePage - Header at top, Footer at bottom, main content between

**Checkpoint**: Landing page fully styled and viewable at localhost:5173

---

## Phase 4: Polish

**Purpose**: Final styling and responsiveness

- [ ] T019 [P] Add responsive styling to Header - Mobile hamburger menu consideration (can be empty callback for now)
- [ ] T020 [P] Add responsive styling to ListingCard grid - 1 column mobile, 2 tablet, 4 desktop
- [ ] T021 [P] Add responsive styling to CategoryCard grid - 2 columns mobile, 4 desktop
- [ ] T022 Verify all button redirects work (Login → /login, Sign Up → /register, Browse Auctions → /listings, Category cards → /listings?category=X)
- [ ] T023 Test page loads without errors in browser console

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - start immediately
- **Phase 2 (Foundational)**: Depends on T001-T003 completion
- **Phase 3 (Landing Page)**: Depends on Phase 2 components
- **Phase 4 (Polish)**: Depends on Phase 3 completion

### Parallel Opportunities

Phase 2 tasks (T004-T008) can all run in parallel.
Phase 3 mock data tasks (T009-T011) can run in parallel.

---

## Implementation Notes

### Button Behaviors

| Button | Action |
|--------|--------|
| Logo click | Navigate to `/` |
| Categories nav | Empty callback (future: dropdown) |
| How it Works nav | Scroll to section or navigate |
| Sell nav | Navigate to `/seller/dashboard` |
| Search bar | Empty callback (logs input) |
| Notification bell | Empty callback |
| Login | Navigate to `/login` |
| Sign Up | Navigate to `/register` |
| Create Free Account | Navigate to `/register` |
| Browse Auctions | Navigate to `/listings` |
| Place Bid | Empty callback (logs listing id) |
| View All (Live Auctions) | Navigate to `/listings` |
| Category card click | Navigate to `/listings?category={name}` |
| Get Started Now | Navigate to `/register` |
| Footer links | Empty callbacks or placeholder routes |

### Mock Listing Data Structure

```typescript
{
  id: string;
  title: string;
  imageUrl: string; // Use picsum.photos/400/300?random=N
  currentBid: number;
  startingPrice: number;
  endTime: string; // ISO datetime, set to future dates
  bidCount: number;
}
```

### Category Icons (lucide-react)

- Watches: `Watch`
- Cameras: `Camera`
- Fashion: `Shirt`
- Furniture: `Armchair`
- Art: `Palette`
- Jewelry: `Gem`
- Electronics: `Monitor`
- Music: `Music`
