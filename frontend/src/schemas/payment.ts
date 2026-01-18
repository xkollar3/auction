import { z } from 'zod';

/**
 * Setup Intent request schema
 */
export const setupIntentRequestSchema = z.object({
  customerId: z.string().startsWith('cus_'),
});

/**
 * Setup Intent response schema
 */
export const setupIntentResponseSchema = z.object({
  clientSecret: z.string(),
  setupIntentId: z.string().startsWith('seti_'),
});

/**
 * Attach payment method schema
 */
export const attachPaymentMethodSchema = z.object({
  paymentMethodId: z.string().startsWith('pm_'),
});

/**
 * Payment method schema
 */
export const paymentMethodSchema = z.object({
  id: z.string().startsWith('pm_'),
  brand: z.string(),
  last4: z.string().length(4),
  expMonth: z.number().min(1).max(12),
  expYear: z.number(),
  isDefault: z.boolean(),
});

export type SetupIntentRequest = z.infer<typeof setupIntentRequestSchema>;
export type SetupIntentResponse = z.infer<typeof setupIntentResponseSchema>;
export type AttachPaymentMethodInput = z.infer<typeof attachPaymentMethodSchema>;
export type PaymentMethodData = z.infer<typeof paymentMethodSchema>;
