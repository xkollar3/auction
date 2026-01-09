import type { ListingFilterParams, ListingPageData, ListingCardData } from '../types/listing';
import { mockListings } from './listings';

const PAGE_SIZE = 8;

// Simulate API delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export async function fetchListings(params: ListingFilterParams): Promise<ListingPageData> {
  // Simulate network delay
  await delay(300);

  let filtered = [...mockListings];

  // Full-text search on title
  if (params.query) {
    const searchTerm = params.query.toLowerCase();
    filtered = filtered.filter((listing) =>
      listing.title.toLowerCase().includes(searchTerm)
    );
  }

  // Category filter
  if (params.category) {
    filtered = filtered.filter((listing) =>
      listing.category?.toLowerCase() === params.category?.toLowerCase()
    );
  }

  // Sort
  filtered = sortListings(filtered, params.sortBy);

  // Pagination
  const totalCount = filtered.length;
  const startIndex = (params.page - 1) * PAGE_SIZE;
  const endIndex = startIndex + PAGE_SIZE;
  const paginatedListings = filtered.slice(startIndex, endIndex);
  const hasMore = endIndex < totalCount;

  return {
    listings: paginatedListings,
    hasMore,
    totalCount,
  };
}

function sortListings(listings: ListingCardData[], sortBy: string): ListingCardData[] {
  const sorted = [...listings];

  switch (sortBy) {
    case 'price_asc':
      return sorted.sort((a, b) => {
        const priceA = a.currentBid ?? a.startingPrice;
        const priceB = b.currentBid ?? b.startingPrice;
        return priceA - priceB;
      });

    case 'price_desc':
      return sorted.sort((a, b) => {
        const priceA = a.currentBid ?? a.startingPrice;
        const priceB = b.currentBid ?? b.startingPrice;
        return priceB - priceA;
      });

    case 'ending_soon':
      return sorted.sort((a, b) => {
        const endA = new Date(a.endTime).getTime();
        const endB = new Date(b.endTime).getTime();
        return endA - endB;
      });

    case 'hot':
      return sorted.sort((a, b) => {
        const recentA = a.recentBids ?? 0;
        const recentB = b.recentBids ?? 0;
        return recentB - recentA;
      });

    default:
      return sorted;
  }
}
