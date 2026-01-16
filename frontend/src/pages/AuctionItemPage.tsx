import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Clock, User, Tag, ArrowLeft, Gavel } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { useAuth } from '../hooks/useAuth';
import { getAuctionItem, placeBid } from '../api/auction';
import type { AuctionItem } from '../types/auction';

/**
 * Format currency in cents to display format
 */
const formatCurrency = (cents: number): string => {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
  }).format(cents / 100);
};

/**
 * Format time remaining
 */
const formatTimeRemaining = (endTime: string): string => {
  const end = new Date(endTime).getTime();
  const now = Date.now();
  const diff = end - now;

  if (diff <= 0) return 'Ended';

  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  if (hours > 24) {
    const days = Math.floor(hours / 24);
    return `${days}d ${hours % 24}h`;
  }

  return `${hours}h ${minutes}m`;
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

  const { data: auction, isLoading, error } = useQuery({
    queryKey: ['auctionItem', id],
    queryFn: () => getAuctionItem(id!),
    enabled: !!id,
  });

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
  const canBid = isAuthenticated && !isSeller && auction?.status === 'OPEN';

  // Calculate minimum bid
  const minBid = auction
    ? (auction.highestBidAmount || auction.startingPrice) + 100 // Minimum increment of 1 Kč (100 haléřů)
    : 0;

  const handlePlaceBid = () => {
    if (!auction || !bidAmount) return;

    const amount = Math.round(parseFloat(bidAmount) * 100); // Convert to cents
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

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Image */}
          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            <img
              src={auction.imageUrl}
              alt={auction.title}
              className="w-full h-96 object-cover"
            />
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
                  <p className="text-sm text-gray-500">Current Bid</p>
                  <p className="text-3xl font-bold text-gray-900">
                    {formatCurrency(auction.highestBidAmount || auction.startingPrice)}
                  </p>
                  <p className="text-sm text-gray-500">{auction.bidCount} bids</p>
                </div>
                <div className="text-right">
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
                            placeholder={(minBid / 100).toFixed(2)}
                            step="0.01"
                            min={(minBid / 100).toFixed(2)}
                            className="w-full pl-8 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
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

              {isEnded && auction.highestBidderName && (
                <div className="border-t pt-4">
                  <p className="text-center text-gray-600">
                    Won by <span className="font-semibold">{auction.highestBidderName}</span> for{' '}
                    <span className="font-semibold">{formatCurrency(auction.highestBidAmount!)}</span>
                  </p>
                </div>
              )}
            </div>

            {/* Seller Info */}
            <div className="bg-white rounded-lg shadow-md p-4">
              <div className="flex items-center gap-3">
                <div className="bg-gray-200 rounded-full p-2">
                  <User className="h-5 w-5 text-gray-600" />
                </div>
                <div>
                  <p className="text-sm text-gray-500">Seller</p>
                  <p className="font-medium text-gray-900">{auction.sellerName}</p>
                </div>
              </div>
            </div>
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
                {auction.recentBids.map((bid, index) => (
                  <div
                    key={bid.id}
                    className={`flex justify-between items-center py-2 ${
                      index !== auction.recentBids.length - 1 ? 'border-b border-gray-100' : ''
                    }`}
                  >
                    <div>
                      <p className="font-medium text-gray-900">{bid.bidderName}</p>
                      <p className="text-xs text-gray-500">{formatRelativeTime(bid.placedAt)}</p>
                    </div>
                    <p className={`font-semibold ${index === 0 ? 'text-green-600' : 'text-gray-700'}`}>
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
