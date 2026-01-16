import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Clock, User, Store } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getSellerShopAuctions } from '../api/auction';
import type { AuctionStatus } from '../types/auction';

const formatTimeRemaining = (endTime: string): string => {
  const end = new Date(endTime);
  const now = new Date();
  const diff = end.getTime() - now.getTime();

  if (diff <= 0) return 'Ended';

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  if (days > 0) return `${days}d ${hours}h`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
};

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
  }).format(price);
};

// Placeholder image for items without images
const PLACEHOLDER_IMAGE_URL = 'https://placehold.co/400x300/e2e8f0/64748b?text=No+Image';

export const SellerShopPage = () => {
  const { sellerId } = useParams<{ sellerId: string }>();
  const [statusFilter, setStatusFilter] = useState<AuctionStatus>('ACTIVE');

  const { data: auctions, isLoading, error } = useQuery({
    queryKey: ['sellerShop', sellerId, statusFilter],
    queryFn: () => getSellerShopAuctions(sellerId!, statusFilter),
    enabled: !!sellerId,
  });

  // Get seller name from first auction (they all have the same seller)
  const sellerName = auctions && auctions.length > 0
    ? `${(auctions[0] as any).sellerFirstName || ''} ${(auctions[0] as any).sellerLastName || ''}`.trim()
    : null;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">
        {/* Back Button */}
        <Link
          to="/listings"
          className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Listings
        </Link>

        {/* Seller Header */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-center gap-4">
            <div className="bg-blue-100 rounded-full p-4">
              <Store className="h-8 w-8 text-blue-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                {sellerName || 'Seller Shop'}
              </h1>
              <p className="text-gray-600">
                {isLoading ? 'Loading...' : `${auctions?.length || 0} ${statusFilter === 'ACTIVE' ? 'active' : 'closed'} auctions`}
              </p>
            </div>
          </div>
        </div>

        {/* Status Filter Tabs */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setStatusFilter('ACTIVE')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'ACTIVE'
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            Active Auctions
          </button>
          <button
            onClick={() => setStatusFilter('CLOSED')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              statusFilter === 'CLOSED'
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            Closed Auctions
          </button>
        </div>

        {/* Auctions Grid */}
        {isLoading ? (
          <div className="flex items-center justify-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load seller's auctions</p>
          </div>
        ) : auctions && auctions.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {auctions.map((auction) => (
              <Link
                key={auction.id}
                to={`/auction/${auction.id}`}
                className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
              >
                <div className="aspect-[4/3] overflow-hidden">
                  <img
                    src={auction.imageUrl || PLACEHOLDER_IMAGE_URL}
                    alt={auction.title}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <span className={`text-xs px-2 py-0.5 rounded ${
                      auction.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-700'
                    }`}>
                      {auction.status === 'ACTIVE' ? 'Live' : auction.status}
                    </span>
                    <span className="text-xs px-2 py-0.5 rounded bg-blue-100 text-blue-700">
                      {auction.category}
                    </span>
                  </div>
                  <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2">
                    {auction.title}
                  </h3>
                  <div className="flex justify-between items-end">
                    <div>
                      <p className="text-xs text-gray-500">Current price</p>
                      <p className="text-lg font-bold text-gray-900">
                        {formatPrice(auction.highestBidAmount || auction.startingPrice)}
                      </p>
                      <p className="text-xs text-gray-500">{auction.bidCount} bids</p>
                    </div>
                    {auction.status === 'ACTIVE' && (
                      <div className="text-right">
                        <p className="text-xs text-gray-500">Ends in</p>
                        <p className="text-sm font-medium text-orange-600 flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {formatTimeRemaining(auction.auctionEndTime)}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <User className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-600">
              {statusFilter === 'ACTIVE'
                ? 'This seller has no active auctions.'
                : 'This seller has no closed auctions.'}
            </p>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};
