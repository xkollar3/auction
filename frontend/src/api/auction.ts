import { api } from '../lib/api';
import type { AuctionItem, AuctionStatus, PlaceBidRequest, PlaceBidResponse, AddAuctionItemRequest, AddAuctionItemResponse, SellerAuctionItemResponse, BrowseAuctionsParams, BrowseAuctionsResponse, AuctionItemDetailResponse } from '../types/auction';

/**
 * Mock auction item data
 * TODO: Replace with real API call
 */
const mockAuctionItem: AuctionItem = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  sellerId: 'seller-123',
  sellerName: 'John Seller',
  title: 'Vintage Fender Stratocaster 1965 Original',
  description: `This is a stunning 1965 Fender Stratocaster in excellent condition.

Features:
- Original sunburst finish with natural aging
- All original electronics and pickups
- Original case included
- Serial number verified authentic
- Minor wear consistent with age

This guitar has been professionally maintained and plays beautifully. A true collector's piece with incredible tone that only comes from decades of aging.

Shipping: Insured worldwide shipping available. Local pickup in Los Angeles also available.`,
  imageUrl: 'https://picsum.photos/800/600?random=guitar',
  startingPrice: 150000,
  category: 'MUSIC',
  auctionEndTime: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString(), // 4 hours from now
  status: 'OPEN',
  highestBidderId: 'bidder-456',
  highestBidderName: 'Jane Bidder',
  highestBidAmount: 185000,
  bidCount: 17,
  recentBids: [
    {
      id: 'bid-1',
      bidderId: 'bidder-456',
      bidderName: 'Jane B.',
      amount: 185000,
      placedAt: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
    },
    {
      id: 'bid-2',
      bidderId: 'bidder-789',
      bidderName: 'Mike S.',
      amount: 180000,
      placedAt: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
    },
    {
      id: 'bid-3',
      bidderId: 'bidder-456',
      bidderName: 'Jane B.',
      amount: 175000,
      placedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    },
    {
      id: 'bid-4',
      bidderId: 'bidder-101',
      bidderName: 'Alex K.',
      amount: 170000,
      placedAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
    },
    {
      id: 'bid-5',
      bidderId: 'bidder-202',
      bidderName: 'Sam W.',
      amount: 165000,
      placedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    },
  ],
};

/**
 * Get auction item by ID
 */
export const getAuctionItem = async (id: string): Promise<AuctionItemDetailResponse> => {
  const response = await api.get<AuctionItemDetailResponse>(`/api/auctions/${id}`);
  return response.data;
};

/**
 * Place a bid on an auction item
 * TODO: Replace with real API call
 */
export const placeBid = async (request: PlaceBidRequest): Promise<PlaceBidResponse> => {
  // Simulate network delay
  await new Promise((resolve) => setTimeout(resolve, 500));

  // Mock validation
  if (request.amount <= (mockAuctionItem.highestBidAmount || mockAuctionItem.startingPrice)) {
    return {
      success: false,
      message: 'Bid amount must be higher than the current highest bid',
    };
  }

  return {
    success: true,
    newHighestBid: request.amount,
  };
};

/**
 * Add a new auction item
 */
export const addAuctionItem = async (request: AddAuctionItemRequest): Promise<AddAuctionItemResponse> => {
  const response = await api.post<AddAuctionItemResponse>('/api/auctions', request);
  return response.data;
};

/**
 * Get seller's auction items by status
 */
export const getSellerAuctions = async (status: AuctionStatus): Promise<SellerAuctionItemResponse[]> => {
  const response = await api.get<SellerAuctionItemResponse[]>('/api/auctions/seller/dashboard', {
    params: { status },
  });
  return response.data;
};

/**
 * Browse auction items with filtering, sorting, and pagination
 */
export const browseAuctions = async (params: BrowseAuctionsParams): Promise<BrowseAuctionsResponse> => {
  const response = await api.get<BrowseAuctionsResponse>('/api/auctions', {
    params: {
      category: params.category,
      sort: params.sort || 'ENDING_SOON',
      search: params.search,
      page: params.page || 0,
      size: params.size || 20,
    },
  });
  return response.data;
};
