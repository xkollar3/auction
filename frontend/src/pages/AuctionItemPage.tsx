import { useState, useCallback, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Clock, User, ArrowLeft, Gavel, ChevronLeft, ChevronRight } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { useAuth } from '../hooks/useAuth';
import { useAuctionWebSocket, type BidUpdateMessage } from '../hooks/useAuctionWebSocket';
import { getAuctionItem, placeBid, getAuctionImages } from '../api/auction';
import type { AuctionItemDetailResponse } from '../types/auction';

// Placeholder image for items without images
const PLACEHOLDER_IMAGE_URL = 'https://placehold.co/800x600/e2e8f0/64748b?text=No+Image';

/**
 * Format currency to display format
 */
const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
  }).format(amount);
};

/**
 * Format time remaining with seconds
 */
const formatTimeRemaining = (endTime: string): string => {
  const end = new Date(endTime).getTime();
  const now = Date.now();
  const diff = end - now;

  if (diff <= 0) return 'Ended';

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);

  if (days > 0) {
    return `${days}d ${hours}h ${minutes}m ${seconds}s`;
  }
  if (hours > 0) {
    return `${hours}h ${minutes}m ${seconds}s`;
  }
  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }
  return `${seconds}s`;
};

/**
 * Format relative time for bids
 */
const formatRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  return `${diffDays}d ago`;
};

/**
 * Auction Item Detail Page
 */
export const AuctionItemPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuth();

  const [bidAmount, setBidAmount] = useState('');
  const [bidError, setBidError] = useState<string | null>(null);
  const [bidSuccess, setBidSuccess] = useState(false);
  const [priceUpdated, setPriceUpdated] = useState(false);
  const [, setTick] = useState(0);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const { data: auction, isLoading, error } = useQuery({
    queryKey: ['auctionItem', id],
    queryFn: () => getAuctionItem(id!),
    enabled: !!id,
  });

  const { data: imagesData } = useQuery({
    queryKey: ['auctionImages', id],
    queryFn: () => getAuctionImages(id!),
    enabled: !!id,
  });

  // Filter out empty/invalid URLs
  const images = (imagesData?.images || []).filter(url => url && url.trim().length > 0);

  // Reset index if it's out of bounds when images change
  useEffect(() => {
    if (images.length > 0 && currentImageIndex >= images.length) {
      setCurrentImageIndex(0);
    }
  }, [images.length, currentImageIndex]);

  const handlePrevImage = () => {
    setCurrentImageIndex((prev) => (prev > 0 ? prev - 1 : images.length - 1));
  };

  const handleNextImage = () => {
    setCurrentImageIndex((prev) => (prev < images.length - 1 ? prev + 1 : 0));
  };

  // Get current image URL - use fetched images if available, otherwise fallback to placeholder
  const currentImageUrl = images[currentImageIndex] || PLACEHOLDER_IMAGE_URL;

  // Handle real-time bid updates
  const handleBidUpdate = useCallback(
    (message: BidUpdateMessage) => {
      const bidAmount = typeof message.bidAmount === 'string'
        ? parseFloat(message.bidAmount)
        : message.bidAmount;
      const placedAt = message.placedAt;

      // Get current data and create updated version
      const currentData = queryClient.getQueryData<AuctionItemDetailResponse>(['auctionItem', id]);
      if (currentData) {
        const newData: AuctionItemDetailResponse = {
          ...currentData,
          currentPrice: bidAmount,
          bidCount: currentData.bidCount + 1,
          recentBids: [
            { amount: bidAmount, placedAt },
            ...currentData.recentBids.slice(0, 4),
          ],
        };
        queryClient.setQueryData(['auctionItem', id], newData);
      }

      // Trigger animation
      setPriceUpdated(true);
      setTimeout(() => setPriceUpdated(false), 1000);
    },
    [queryClient, id]
  );

  // Handle auction closed event
  const handleAuctionClosed = useCallback(
    () => {
      queryClient.setQueryData<AuctionItemDetailResponse>(
        ['auctionItem', id],
        (oldData) => {
          if (!oldData) return oldData;
          return { ...oldData, status: 'CLOSED' };
        }
      );
    },
    [queryClient, id]
  );

  // Connect to WebSocket for live updates (only for active auctions)
  useAuctionWebSocket({
    auctionItemId: id,
    enabled: !!auction && auction.status === 'ACTIVE',
    onBidUpdate: handleBidUpdate,
    onAuctionClosed: handleAuctionClosed,
  });

  // Tick every second to update countdown timer
  useEffect(() => {
    if (!auction || auction.status !== 'ACTIVE') return;

    const interval = setInterval(() => {
      setTick((t) => t + 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [auction?.status]);

  const placeBidMutation = useMutation({
    mutationFn: placeBid,
    onSuccess: (response) => {
      if (response.success) {
        setBidSuccess(true);
        setBidError(null);
        setBidAmount('');
        queryClient.invalidateQueries({ queryKey: ['auctionItem', id] });
        setTimeout(() => setBidSuccess(false), 3000);
      } else {
        setBidError(response.message || 'Failed to place bid');
      }
    },
    onError: (error) => {
      setBidError(error instanceof Error ? error.message : 'Failed to place bid');
    },
  });

  // Check if current user is the seller
  const isSeller = user?.id === auction?.sellerId;

  // Calculate minimum bid (1 Kč increment)
  const minBid = auction
    ? auction.currentPrice + 1
    : 0;

  const handlePlaceBid = () => {
    if (!auction || !bidAmount) return;

    const amount = parseFloat(bidAmount);
    if (isNaN(amount) || amount < minBid) {
      setBidError(`Minimum bid is ${formatCurrency(minBid)}`);
      return;
    }

    setBidError(null);
    placeBidMutation.mutate({
      auctionItemId: auction.id,
      amount,
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="flex items-center justify-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
        <Footer />
      </div>
    );
  }

  if (error || !auction) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-4 py-12 text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Auction Not Found</h1>
          <p className="text-gray-600 mb-6">The auction you're looking for doesn't exist or has been removed.</p>
          <button
            onClick={() => navigate('/listings')}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Browse Auctions
          </button>
        </div>
        <Footer />
      </div>
    );
  }

  const isEnded = auction.status === 'CLOSED' || new Date(auction.auctionEndTime) <= new Date();

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          {/* Image Gallery */}
          <div className="rounded-lg shadow-md overflow-hidden bg-gray-100">
            <div className="relative bg-gray-100">
              <img
                src={currentImageUrl}
                alt={auction.title}
                className="block w-full h-96 object-cover"
              />

              {/* Navigation Arrows - only show if more than 1 image */}
              {images.length > 1 && (
                <>
                  <button
                    onClick={handlePrevImage}
                    className="absolute left-2 top-1/2 -translate-y-1/2 p-2 bg-black/50 text-white rounded-full hover:bg-black/70 transition-colors"
                  >
                    <ChevronLeft className="h-6 w-6" />
                  </button>
                  <button
                    onClick={handleNextImage}
                    className="absolute right-2 top-1/2 -translate-y-1/2 p-2 bg-black/50 text-white rounded-full hover:bg-black/70 transition-colors"
                  >
                    <ChevronRight className="h-6 w-6" />
                  </button>

                  {/* Image Counter */}
                  <div className="absolute bottom-4 left-1/2 -translate-x-1/2 px-3 py-1 bg-black/50 text-white text-sm rounded-full">
                    {currentImageIndex + 1} / {images.length}
                  </div>
                </>
              )}
            </div>

            {/* Thumbnail Strip - only show if more than 1 image */}
            {images.length > 1 && (
              <div className="flex gap-2 p-3 overflow-x-auto bg-white border-t">
                {images.map((url, index) => (
                  <button
                    key={index}
                    onClick={() => setCurrentImageIndex(index)}
                    className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors ${
                      index === currentImageIndex ? 'border-blue-600' : 'border-transparent hover:border-gray-300'
                    }`}
                  >
                    <img
                      src={url}
                      alt={`Thumbnail ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Details */}
          <div className="space-y-6">
            {/* Title and Status */}
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className={`px-2 py-1 text-xs font-medium rounded ${
                  isEnded
                    ? 'bg-gray-100 text-gray-600'
                    : 'bg-green-100 text-green-700'
                }`}>
                  {isEnded ? 'Ended' : 'Live'}
                </span>
                <span className="px-2 py-1 text-xs font-medium rounded bg-blue-100 text-blue-700">
                  {auction.category}
                </span>
              </div>
              <h1 className="text-2xl lg:text-3xl font-bold text-gray-900">{auction.title}</h1>
            </div>

            {/* Price and Time */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <p className="text-sm text-gray-500">Current Price</p>
                  <p className={`text-3xl font-bold transition-all duration-300 ${
                    priceUpdated
                      ? 'text-green-600 scale-105'
                      : 'text-gray-900 scale-100'
                  }`}>
                    {formatCurrency(auction.currentPrice)}
                  </p>
                  <p className="text-sm text-gray-500">{auction.bidCount} bids</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500">Starting Price</p>
                  <p className="text-lg font-medium text-gray-600 mb-2">
                    {formatCurrency(auction.startingPrice)}
                  </p>
                  <p className="text-sm text-gray-500">Time Remaining</p>
                  <p className={`text-xl font-semibold ${isEnded ? 'text-gray-500' : 'text-orange-600'}`}>
                    <Clock className="inline h-5 w-5 mr-1" />
                    {formatTimeRemaining(auction.auctionEndTime)}
                  </p>
                </div>
              </div>

              {/* Bid Form */}
              {!isEnded && (
                <div className="border-t pt-4">
                  {!isAuthenticated ? (
                    <div className="text-center py-4">
                      <p className="text-gray-600 mb-3">Sign in to place a bid</p>
                      <button
                        onClick={() => navigate('/login')}
                        className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                      >
                        Sign In
                      </button>
                    </div>
                  ) : isSeller ? (
                    <div className="text-center py-4 bg-gray-50 rounded-lg">
                      <p className="text-gray-600">You cannot bid on your own auction</p>
                    </div>
                  ) : (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Your Bid (minimum {formatCurrency(minBid)})
                      </label>
                      <div className="flex gap-3">
                        <div className="relative flex-1">
                          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">Kč</span>
                          <input
                            type="number"
                            value={bidAmount}
                            onChange={(e) => setBidAmount(e.target.value)}
                            placeholder={minBid.toString()}
                            step="1"
                            min={minBid}
                            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                          />
                        </div>
                        <button
                          onClick={handlePlaceBid}
                          disabled={placeBidMutation.isPending}
                          className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:bg-blue-400 flex items-center gap-2"
                        >
                          <Gavel className="h-5 w-5" />
                          {placeBidMutation.isPending ? 'Placing...' : 'Place Bid'}
                        </button>
                      </div>
                      {bidError && (
                        <p className="mt-2 text-sm text-red-600">{bidError}</p>
                      )}
                      {bidSuccess && (
                        <p className="mt-2 text-sm text-green-600">Bid placed successfully!</p>
                      )}
                    </div>
                  )}
                </div>
              )}

              {isEnded && auction.bidCount > 0 && (
                <div className="border-t pt-4">
                  <p className="text-center text-gray-600">
                    Auction ended at <span className="font-semibold">{formatCurrency(auction.currentPrice)}</span>
                  </p>
                </div>
              )}
            </div>

            {/* Seller Info */}
            <Link
              to={`/seller/${auction.sellerId}`}
              className="block bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center gap-3">
                <div className="bg-blue-100 rounded-full p-2">
                  <User className="h-5 w-5 text-blue-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm text-gray-500">Seller</p>
                  <p className="font-medium text-gray-900">{auction.sellerFirstName} {auction.sellerLastName}</p>
                </div>
                <span className="text-blue-600 text-sm font-medium">View Shop →</span>
              </div>
            </Link>
          </div>
        </div>

        {/* Description and Bid History */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mt-8">
          {/* Description */}
          <div className="lg:col-span-2 bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Description</h2>
            <div className="prose prose-gray max-w-none">
              {auction.description.split('\n').map((paragraph, index) => (
                <p key={index} className="text-gray-700 mb-3">
                  {paragraph}
                </p>
              ))}
            </div>
          </div>

          {/* Bid History */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Recent Bids</h2>
            {auction.recentBids.length === 0 ? (
              <p className="text-gray-500 text-center py-4">No bids yet</p>
            ) : (
              <div className="space-y-3">
                {auction.recentBids.slice(0, 5).map((bid, index, arr) => (
                  <div
                    key={`${bid.placedAt}-${bid.amount}`}
                    className={`flex justify-between items-center py-2 transition-all duration-300 ${
                      index !== arr.length - 1 ? 'border-b border-gray-100' : ''
                    } ${index === 0 && priceUpdated ? 'bg-green-50 -mx-2 px-2 rounded' : ''}`}
                  >
                    <div>
                      <p className="text-xs text-gray-500">{formatRelativeTime(bid.placedAt)}</p>
                    </div>
                    <p className={`font-semibold transition-all duration-300 ${
                      index === 0
                        ? priceUpdated ? 'text-green-600 scale-110' : 'text-green-600'
                        : 'text-gray-700'
                    }`}>
                      {formatCurrency(bid.amount)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};
