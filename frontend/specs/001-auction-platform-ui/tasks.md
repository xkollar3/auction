# Tasks: Auction Platform UI

**Input**: Design from `design/` and user specifications
**Prerequisites**: plan.md, spec.md

**Scope**: Landing page implementation complete. Now implementing Listings Browsing Page with filtering, sorting, and infinite scroll.

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

## Phase 5: Listings Browsing Page - User Story 1 Part B (Priority: P1)

**Goal**: Implement the listings browsing page with search, filtering, sorting, and infinite scroll pagination

**Independent Test**: Navigate to /listings (via search bar or header link), see paginated list of auction items with working filters and infinite scroll

**Entry Points**:
- Search bar in Header → navigates to `/listings?q={searchQuery}`
- "Browse Auctions" button → navigates to `/listings`
- "View All" link → navigates to `/listings`
- Category card click → navigates to `/listings?category={categoryName}`

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

## Phase 6: Listings Page Polish

**Purpose**: Final styling, responsiveness, and UX polish

- [X] T041 [P] Add responsive styling to filter bar - Stack filters vertically on mobile, horizontal on desktop
- [ ] T042 [P] Add loading skeleton to ListingGrid while initial data loads (OPTIONAL)
- [ ] T043 [P] Add "Back to top" button that appears after scrolling down (OPTIONAL)
- [X] T044 Verify URL state sync - Filters should update URL, page reload should restore filter state
- [X] T045 Test page loads without errors in browser console
- [X] T046 Verify navigation from HomePage to ListingsPage works via all entry points (search, Browse Auctions, View All, category cards)

---

## Phase 7: Homepage Preview Fix (Bug Fix)

**Goal**: Fix HomePage to show only 8 featured listings instead of all listings from the expanded mock data

**Bug Description**: HomePage currently imports all mockListings directly, which now shows 32+ items instead of the intended 8 preview items.

**Solution**: Create a separate mock API for homepage featured listings that returns exactly 8 items (sorted by "ending soon" or "hot").

### Mock API

- [X] T047 [US1] Create `src/mocks/homeApi.ts` - Mock API function `fetchFeaturedListings(): Promise<ListingCardData[]>` that returns 8 listings sorted by endTime (ending soonest first) for homepage preview

### Page Update

- [X] T048 [US1] Update `src/pages/HomePage.tsx` - Replace direct mockListings import with fetchFeaturedListings API call, add loading state, display exactly 8 items in the Live Auctions section

**Checkpoint**: HomePage displays exactly 8 featured listings (ending soonest), using the same mock data source but through a dedicated API

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1-4 (Landing Page)**: COMPLETED
- **Phase 5 (Listings Page)**: Can start immediately - builds on existing components
- **Phase 6 (Polish)**: Depends on Phase 5 completion

### Within Phase 5

1. **Types & Mock Data (T024-T026)**: Start first, T024-T025 parallel, T026 depends on T024-T025
2. **Shared Components (T027-T032)**: All parallel, can start after types
3. **Hooks (T033-T034)**: Parallel, can start after types
4. **Mock API (T035)**: Depends on types and mock data
5. **Page Implementation (T036-T040)**: Sequential, depends on components/hooks/API

### Parallel Opportunities

Phase 5 shared components (T027-T032) can all run in parallel.
Phase 5 hooks (T033-T034) can run in parallel.
Phase 6 polish tasks (T041-T043) can run in parallel.

---

## Implementation Notes

### URL Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| q | string | Full-text search query |
| category | string | Category filter (matches category name) |
| sort | string | Sort option: price_asc, price_desc, ending_soon, hot |

### Sort Options

| Option | Display Name | Logic |
|--------|--------------|-------|
| price_asc | Price: Low to High | Sort by currentBid ascending |
| price_desc | Price: High to Low | Sort by currentBid descending |
| ending_soon | Ending Soon | Sort by endTime ascending (soonest first) |
| hot | Hot | Sort by recentBids descending (most active first) |

### Infinite Scroll Behavior

- Initial load: 8 listings
- Load more: 8 additional listings when sentinel visible
- Stop condition: hasMore=false from API
- Loading state: Show spinner at bottom while loading more
- Error state: Show retry button if load fails

### Mock Data Structure

```typescript
// Extended ListingCardData
{
  id: string;
  title: string;
  imageUrl: string;
  currentBid: number;
  startingPrice: number;
  endTime: string;
  bidCount: number;
  category: string;        // NEW: for filtering
  recentBids: number;      // NEW: bids in last 10 mins (for "hot" sorting)
}

// Filter params
{
  query?: string;
  category?: string;
  sortBy: 'price_asc' | 'price_desc' | 'ending_soon' | 'hot';
  page: number;
}

// API response
{
  listings: ListingCardData[];
  hasMore: boolean;
  totalCount: number;
}
```
