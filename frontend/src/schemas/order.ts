import { z } from 'zod';

export const enterTrackingNumberSchema = z.object({
  trackingNumber: z
    .string()
    .min(1, 'Tracking number is required')
    .max(100, 'Tracking number must be at most 100 characters'),
});

export type EnterTrackingNumberFormData = z.infer<typeof enterTrackingNumberSchema>;
