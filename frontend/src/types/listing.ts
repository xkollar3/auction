export interface ListingCardData {
  id: string;
  title: string;
  imageUrl: string;
  currentBid: number | null;
  startingPrice: number;
  endTime: string; // ISO datetime
  bidCount: number;
  category?: string; // For filtering
  recentBids?: number; // Bids in last 10 mins (for "hot" sorting)
}

export type SortOption = 'price_asc' | 'price_desc' | 'ending_soon' | 'hot';

export interface ListingFilterParams {
  query?: string;
  category?: string;
  sortBy: SortOption;
  page: number;
}

export interface ListingPageData {
  listings: ListingCardData[];
  hasMore: boolean;
  totalCount: number;
}
