/**
 * Auction item status
 */
export type AuctionStatus = 'ACTIVE' | 'CLOSED' | 'CANCELLED';

/**
 * Auction item category
 */
export type AuctionCategory = 'FASHION' | 'FURNITURE' | 'ART' | 'JEWELRY' | 'ELECTRONICS' | 'MUSIC';

/**
 * Bid response from API
 */
export interface BidResponse {
  amount: number;
  placedAt: string; // ISO datetime
}

/**
 * Auction item detail response from API
 */
export interface AuctionItemDetailResponse {
  id: string;
  sellerId: string;
  sellerFirstName: string;
  sellerLastName: string;
  title: string;
  description: string;
  startingPrice: number;
  currentPrice: number;
  bidCount: number;
  category: AuctionCategory;
  status: AuctionStatus;
  auctionEndTime: string; // ISO datetime
  recentBids: BidResponse[];
}

/**
 * Legacy Bid interface (for backwards compatibility)
 */
export interface Bid {
  id: string;
  bidderId: string;
  bidderName: string;
  amount: number;
  placedAt: string; // ISO datetime
}

/**
 * Legacy AuctionItem interface (for backwards compatibility)
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
  currentPrice: number;
  category: AuctionCategory;
  auctionEndTime: string;
  status: AuctionStatus;
  bidCount: number;
  imageUrl: string | null;
}

/**
 * Backend sort options
 */
export type AuctionSortOption = 'ENDING_SOON' | 'HOT' | 'PRICE_HIGH_TO_LOW' | 'PRICE_LOW_TO_HIGH';

/**
 * Browse auction item response (from API)
 */
export interface BrowseAuctionItemResponse {
  id: string;
  sellerId: string;
  title: string;
  description: string;
  startingPrice: number;
  currentPrice: number;
  bidCount: number;
  category: AuctionCategory;
  auctionEndTime: string;
  imageUrl: string | null;
}

/**
 * Browse auctions paginated response
 */
export interface BrowseAuctionsResponse {
  items: BrowseAuctionItemResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Browse auctions request params
 */
export interface BrowseAuctionsParams {
  category?: AuctionCategory;
  sort?: AuctionSortOption;
  search?: string;
  page?: number;
  size?: number;
}

/**
 * Auction item images response
 */
export interface AuctionItemImagesResponse {
  images: string[];
}
