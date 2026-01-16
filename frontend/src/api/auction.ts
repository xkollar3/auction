import { api } from '../lib/api';
import type { AuctionStatus, PlaceBidRequest, PlaceBidResponse, AddAuctionItemRequest, AddAuctionItemResponse, SellerAuctionItemResponse, BrowseAuctionsParams, BrowseAuctionsResponse, BrowseAuctionItemResponse, AuctionItemDetailResponse, AuctionItemImagesResponse } from '../types/auction';

/**
 * Get auction item by ID
 */
export const getAuctionItem = async (id: string): Promise<AuctionItemDetailResponse> => {
  const response = await api.get<AuctionItemDetailResponse>(`/api/auctions/${id}`);
  return response.data;
};

/**
 * Place a bid on an auction item
 */
export const placeBid = async (request: PlaceBidRequest): Promise<PlaceBidResponse> => {
  try {
    const response = await api.post<{ accepted: boolean; message?: string }>(
      `/api/auctions/${request.auctionItemId}/bids`,
      { bidAmount: request.amount }
    );
    return {
      success: response.data.accepted,
      message: response.data.message,
      newHighestBid: request.amount,
    };
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'response' in error) {
      const axiosError = error as { response?: { data?: { message?: string } } };
      return {
        success: false,
        message: axiosError.response?.data?.message || 'Failed to place bid',
      };
    }
    return {
      success: false,
      message: 'Failed to place bid',
    };
  }
};

/**
 * Add a new auction item
 */
export const addAuctionItem = async (request: AddAuctionItemRequest): Promise<AddAuctionItemResponse> => {
  const response = await api.post<AddAuctionItemResponse>('/api/auctions', request);
  return response.data;
};

/**
 * Get a seller's auction items by status
 */
export const getSellerShopAuctions = async (sellerId: string, status: AuctionStatus): Promise<SellerAuctionItemResponse[]> => {
  const response = await api.get<SellerAuctionItemResponse[]>(`/api/auctions/seller/${sellerId}`, {
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

/**
 * Get auction items the current user has bid on
 */
export const getMyBids = async (status: AuctionStatus): Promise<BrowseAuctionItemResponse[]> => {
  const response = await api.get<BrowseAuctionItemResponse[]>('/api/auctions/my-bids', {
    params: { status },
  });
  return response.data;
};

/**
 * Get auction items won by the current user
 */
export const getMyPurchases = async (): Promise<BrowseAuctionItemResponse[]> => {
  const response = await api.get<BrowseAuctionItemResponse[]>('/api/auctions/my-purchases');
  return response.data;
};

/**
 * Get current user's sold auction items
 */
export const getMySales = async (): Promise<SellerAuctionItemResponse[]> => {
  const response = await api.get<SellerAuctionItemResponse[]>('/api/auctions/my-sales');
  return response.data;
};

/**
 * Upload images for an auction item
 */
export const uploadAuctionImages = async (auctionItemId: string, files: File[]): Promise<void> => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });

  await api.post(`/api/auctions/${auctionItemId}/images`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

/**
 * Get images for an auction item
 */
export const getAuctionImages = async (auctionItemId: string): Promise<AuctionItemImagesResponse> => {
  const response = await api.get<AuctionItemImagesResponse>(`/api/auctions/${auctionItemId}/images`);
  return response.data;
};
