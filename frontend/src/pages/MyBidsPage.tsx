import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Clock, Coins, Tag } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getMyBids } from '../api/auction';
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

export const MyBidsPage = () => {
  const [statusFilter, setStatusFilter] = useState<AuctionStatus>('ACTIVE');

  const { data: auctions, isLoading, error } = useQuery({
    queryKey: ['myBids', statusFilter],
    queryFn: () => getMyBids(statusFilter),
  });

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Bids</h1>
          <p className="text-gray-600 mt-2">Track auctions you've placed bids on</p>
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

        {/* Auctions List */}
        {isLoading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600">Loading your bids...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load your bids</p>
          </div>
        ) : auctions && auctions.length > 0 ? (
          <div className="grid gap-4">
            {auctions.map((auction) => (
              <Link
                key={auction.id}
                to={`/auction/${auction.id}`}
                className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">{auction.title}</h3>
                    <p className="text-sm text-gray-600 line-clamp-2 mb-3">{auction.description}</p>
                    <div className="flex items-center gap-6 text-sm">
                      <div className="flex items-center gap-1 text-gray-500">
                        <Tag className="h-4 w-4" />
                        <span>{auction.category}</span>
                      </div>
                      <div className="flex items-center gap-1 text-gray-500">
                        <Coins className="h-4 w-4" />
                        <span>Starting: {formatPrice(auction.startingPrice)}</span>
                      </div>
                      {statusFilter === 'ACTIVE' && (
                        <div className="flex items-center gap-1 text-orange-600">
                          <Clock className="h-4 w-4" />
                          <span>{formatTimeRemaining(auction.auctionEndTime)}</span>
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="text-right ml-6">
                    <div className="text-sm text-gray-500 mb-1">{auction.bidCount} bids</div>
                    <div className="text-lg font-bold text-gray-900">
                      {formatPrice(auction.currentPrice)}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600 mb-4">
              {statusFilter === 'ACTIVE'
                ? "You haven't placed any bids on active auctions."
                : "You don't have any bids on closed auctions."}
            </p>
            <Link
              to="/listings"
              className="inline-flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Browse Auctions
            </Link>
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};
