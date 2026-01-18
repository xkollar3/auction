import { z } from 'zod';

/**
 * Create Stripe customer schema (with address)
 */
export const createCustomerSchema = z.object({
  line1: z.string().min(1, 'Address line 1 is required'),
  line2: z.string().optional(),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  postalCode: z.string().min(1, 'Postal code is required'),
  country: z.string().length(2, 'Country must be 2-letter code (e.g., US, SK)'),
});

/**
 * Customer response schema
 */
export const customerResponseSchema = z.object({
  customerId: z.string().startsWith('cus_'),
});

export type CreateCustomerInput = z.infer<typeof createCustomerSchema>;
export type CustomerResponse = z.infer<typeof customerResponseSchema>;
