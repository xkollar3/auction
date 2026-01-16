import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Plus, Clock, Coins, Tag } from 'lucide-react';
import { Header } from '../shared/Header';
import { Footer } from '../shared/Footer';
import { getUserProfile } from '../api/user';
import { getSellerAuctions } from '../api/auction';
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

export const SellerDashboardPage = () => {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<AuctionStatus>('ACTIVE');

  const { data: profile, isLoading: profileLoading } = useQuery({
    queryKey: ['userProfile'],
    queryFn: getUserProfile,
  });

  const { data: auctions, isLoading: auctionsLoading, error } = useQuery({
    queryKey: ['sellerAuctions', statusFilter],
    queryFn: () => getSellerAuctions(statusFilter),
    enabled: !!profile?.sellerAccountEnabled,
  });

  if (profileLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Header />
        <div className="flex items-center justify-center py-20 flex-1">
          <p className="text-gray-600">Loading...</p>
        </div>
        <Footer />
      </div>
    );
  }

  if (!profile?.sellerAccountEnabled) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Header />
        <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">Seller Account Required</h1>
            <p className="text-gray-600 mb-6">
              You need an enabled seller account to access the dashboard.
              Please complete your seller onboarding first.
            </p>
            <button
              onClick={() => navigate('/profile')}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Go to Profile
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Seller Dashboard</h1>
            <p className="text-gray-600 mt-2">Manage your auction listings</p>
          </div>
          <Link
            to="/seller/post-item"
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="h-5 w-5" />
            Post New Item
          </Link>
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
        {auctionsLoading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-600">Loading auctions...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-red-600">Failed to load auctions</p>
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
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">{auction.title}</h3>
                      <span className={`text-xs px-2 py-0.5 rounded ${
                        auction.status === 'ACTIVE'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-700'
                      }`}>
                        {auction.status}
                      </span>
                    </div>
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
                      {auction.status === 'ACTIVE' && (
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
                      {auction.highestBidAmount
                        ? formatPrice(auction.highestBidAmount)
                        : 'No bids yet'}
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
                ? "You don't have any active auctions."
                : "You don't have any closed auctions."}
            </p>
            {statusFilter === 'ACTIVE' && (
              <Link
                to="/seller/post-item"
                className="inline-flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                <Plus className="h-5 w-5" />
                Post Your First Item
              </Link>
            )}
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};
