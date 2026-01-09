import type { ListingCardData } from '../types/listing';
import { mockListings } from './listings';

const FEATURED_COUNT = 8;

// Simulate API delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Fetches featured listings for the homepage preview.
 * Returns exactly 8 listings sorted by endTime (ending soonest first).
 */
export async function fetchFeaturedListings(): Promise<ListingCardData[]> {
  // Simulate network delay
  await delay(200);

  // Sort by endTime ascending (ending soonest first) and take first 8
  const sorted = [...mockListings].sort((a, b) => {
    const endA = new Date(a.endTime).getTime();
    const endB = new Date(b.endTime).getTime();
    return endA - endB;
  });

  return sorted.slice(0, FEATURED_COUNT);
}
