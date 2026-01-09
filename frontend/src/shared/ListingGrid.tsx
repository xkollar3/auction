import type { ListingCardData } from '../types/listing';
import { ListingCard } from './ListingCard';

interface ListingGridProps {
  listings: ListingCardData[];
  onPlaceBid?: (listingId: string) => void;
}

export function ListingGrid({ listings, onPlaceBid }: ListingGridProps) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      {listings.map((listing) => (
        <ListingCard
          key={listing.id}
          listing={listing}
          onPlaceBid={onPlaceBid}
        />
      ))}
    </div>
  );
}
