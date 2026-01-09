import type { ListingCardData } from '../types/listing';
import type { CategoryData } from '../types/category';

// Generate end times for auctions (some ending soon, some later)
const now = new Date();
const addHours = (hours: number) => new Date(now.getTime() + hours * 60 * 60 * 1000).toISOString();
const addDays = (days: number) => new Date(now.getTime() + days * 24 * 60 * 60 * 1000).toISOString();

export const mockListings: ListingCardData[] = [
  {
    id: '1',
    title: 'Vintage Omega Seamaster Automatic Watch',
    imageUrl: 'https://picsum.photos/400/300?random=1',
    currentBid: 65500,
    startingPrice: 45000,
    endTime: addHours(2),
    bidCount: 24,
  },
  {
    id: '2',
    title: 'Leica M6 Film Camera',
    imageUrl: 'https://picsum.photos/400/300?random=2',
    currentBid: 32800,
    startingPrice: 25000,
    endTime: addHours(5),
    bidCount: 18,
  },
  {
    id: '3',
    title: 'Limited Edition Air Jordan 1 Retro',
    imageUrl: 'https://picsum.photos/400/300?random=3',
    currentBid: 20500,
    startingPrice: 12000,
    endTime: addHours(8),
    bidCount: 42,
  },
  {
    id: '4',
    title: 'Hermes Birkin Bag',
    imageUrl: 'https://picsum.photos/400/300?random=4',
    currentBid: 195000,
    startingPrice: 150000,
    endTime: addDays(1),
    bidCount: 15,
  },
  {
    id: '5',
    title: 'Mid-Century Modern Chair - Eames Style',
    imageUrl: 'https://picsum.photos/400/300?random=5',
    currentBid: 18500,
    startingPrice: 12000,
    endTime: addDays(2),
    bidCount: 9,
  },
  {
    id: '6',
    title: 'PlayStation 5 Console Bundle',
    imageUrl: 'https://picsum.photos/400/300?random=6',
    currentBid: 15800,
    startingPrice: 14000,
    endTime: addHours(12),
    bidCount: 31,
  },
  {
    id: '7',
    title: 'Abstract Art Print - Signed Original',
    imageUrl: 'https://picsum.photos/400/300?random=7',
    currentBid: 8500,
    startingPrice: 5000,
    endTime: addDays(3),
    bidCount: 7,
  },
  {
    id: '8',
    title: 'Diamond Tennis Bracelet 18K Gold',
    imageUrl: 'https://picsum.photos/400/300?random=8',
    currentBid: 125000,
    startingPrice: 95000,
    endTime: addHours(18),
    bidCount: 12,
  },
];

export const mockCategories: CategoryData[] = [
  { id: '1', name: 'Watches', icon: 'watch', itemCount: 1243 },
  { id: '2', name: 'Cameras', icon: 'camera', itemCount: 856 },
  { id: '3', name: 'Fashion', icon: 'shirt', itemCount: 2134 },
  { id: '4', name: 'Furniture', icon: 'armchair', itemCount: 672 },
  { id: '5', name: 'Art', icon: 'palette', itemCount: 1089 },
  { id: '6', name: 'Jewelry', icon: 'gem', itemCount: 945 },
  { id: '7', name: 'Electronics', icon: 'monitor', itemCount: 1567 },
  { id: '8', name: 'Music', icon: 'music', itemCount: 432 },
];
