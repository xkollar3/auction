import { TrendingUp } from 'lucide-react';
import type { ListingCardData } from '../types/listing';

interface ListingCardProps {
  listing: ListingCardData;
  onPlaceBid?: (listingId: string) => void;
}

function formatTimeRemaining(endTime: string): string {
  const end = new Date(endTime).getTime();
  const now = Date.now();
  const diff = end - now;

  if (diff <= 0) return 'Ended';

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  if (days > 0) return `${days}d ${hours}h`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('cs-CZ', {
    style: 'currency',
    currency: 'CZK',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

export function ListingCard({ listing, onPlaceBid }: ListingCardProps) {
  const handlePlaceBid = () => {
    if (onPlaceBid) {
      onPlaceBid(listing.id);
    } else {
      console.log('Place bid clicked for listing:', listing.id);
    }
  };

  const timeRemaining = formatTimeRemaining(listing.endTime);
  const isEnding = timeRemaining.includes('m') && !timeRemaining.includes('h');

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
      {/* Image Container */}
      <div className="relative aspect-[4/3] overflow-hidden">
        <img
          src={listing.imageUrl}
          alt={listing.title}
          className="w-full h-full object-cover"
        />
        {/* Live Badge */}
        <div className="absolute top-3 left-3 bg-red-500 text-white text-xs font-semibold px-2 py-1 rounded-md flex items-center gap-1">
          <span className="w-1.5 h-1.5 bg-white rounded-full animate-pulse"></span>
          LIVE
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        <h3 className="font-medium text-gray-900 mb-2 line-clamp-2">
          {listing.title}
        </h3>

        {/* Price */}
        <div className="mb-3">
          <p className="text-lg font-bold text-gray-900">
            {formatCurrency(listing.currentBid ?? listing.startingPrice)}
          </p>
          {listing.currentBid && listing.currentBid > listing.startingPrice && (
            <p className="text-xs text-gray-500">
              Started at {formatCurrency(listing.startingPrice)}
            </p>
          )}
        </div>

        {/* Timer and Bids */}
        <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
          <div className={`flex items-center gap-1 ${isEnding ? 'text-red-500' : ''}`}>
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{timeRemaining}</span>
          </div>
          <div className="flex items-center gap-1">
            <TrendingUp className="w-4 h-4" />
            <span>{listing.bidCount} bid{listing.bidCount !== 1 ? 's' : ''}</span>
          </div>
        </div>

        {/* Place Bid Button */}
        <button
          onClick={handlePlaceBid}
          className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
        >
          <TrendingUp className="w-4 h-4" />
          Place Bid
        </button>
      </div>
    </div>
  );
}
