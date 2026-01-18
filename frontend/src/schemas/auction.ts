import { z } from 'zod';

export const AUCTION_CATEGORIES = [
  'FASHION',
  'FURNITURE',
  'ART',
  'JEWELRY',
  'ELECTRONICS',
  'MUSIC',
] as const;

export const addAuctionItemSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title must be at most 100 characters'),
  description: z.string().min(10, 'Description must be at least 10 characters').max(2000, 'Description must be at most 2000 characters'),
  startingPrice: z.coerce.number().min(1, 'Starting price must be at least 1 Kč'),
  category: z.enum(AUCTION_CATEGORIES, { message: 'Please select a category' }),
  auctionEndTime: z.string().optional(),
});

export type AddAuctionItemFormData = z.infer<typeof addAuctionItemSchema>;
