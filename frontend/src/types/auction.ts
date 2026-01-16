/**
 * Auction item status
 */
export type AuctionStatus = 'OPEN' | 'CLOSED' | 'CANCELLED';

/**
 * Auction item category
 */
export type AuctionCategory = 'FASHION' | 'FURNITURE' | 'ART' | 'JEWELRY' | 'ELECTRONICS' | 'MUSIC';

/**
 * Bid information
 */
export interface Bid {
  id: string;
  bidderId: string;
  bidderName: string;
  amount: number;
  placedAt: string; // ISO datetime
}

/**
 * Full auction item details
 */
export interface AuctionItem {
  id: string;
  sellerId: string;
  sellerName: string;
  title: string;
  description: string;
  imageUrl: string;
  startingPrice: number;
  category: AuctionCategory;
  auctionEndTime: string; // ISO datetime
  status: AuctionStatus;
  highestBidderId: string | null;
  highestBidderName: string | null;
  highestBidAmount: number | null;
  bidCount: number;
  recentBids: Bid[];
}

/**
 * Place bid request
 */
export interface PlaceBidRequest {
  auctionItemId: string;
  amount: number;
}

/**
 * Place bid response
 */
export interface PlaceBidResponse {
  success: boolean;
  message?: string;
  newHighestBid?: number;
}

/**
 * Add auction item request
 */
export interface AddAuctionItemRequest {
  title: string;
  description: string;
  startingPrice: number;
  category: AuctionCategory;
  auctionEndTime: string; // ISO datetime
}

/**
 * Add auction item response
 */
export interface AddAuctionItemResponse {
  auctionItemId: string;
}

/**
 * Seller auction item response (for dashboard)
 */
export interface SellerAuctionItemResponse {
  id: string;
  title: string;
  description: string;
  startingPrice: number;
  category: AuctionCategory;
  auctionEndTime: string;
  status: AuctionStatus;
  highestBidAmount: number | null;
  bidCount: number;
}
