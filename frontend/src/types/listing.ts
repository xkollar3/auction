export interface ListingCardData {
  id: string;
  title: string;
  imageUrl: string;
  currentBid: number | null;
  startingPrice: number;
  endTime: string; // ISO datetime
  bidCount: number;
}
