import { z } from 'zod';

/**
 * Seller account creation schema
 */
export const createSellerAccountSchema = z.object({
  // No fields needed - authenticated request using JWT
});

/**
 * Seller account response schema
 */
export const sellerAccountResponseSchema = z.object({
  sellerId: z.string().startsWith('acct_'),
  onboardingUrl: z.string().url(),
  dashboardUrl: z.string().url(),
});

export type CreateSellerAccountInput = z.infer<typeof createSellerAccountSchema>;
export type SellerAccountResponse = z.infer<typeof sellerAccountResponseSchema>;
