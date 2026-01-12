import { z } from 'zod';

/**
 * Profile display schema
 *
 * Schema for read-only user profile information
 */
export const profileSchema = z.object({
  id: z.string(),
  email: z.string().email(),
  name: z.string().min(1),
  phone: z.string().optional(),
  emailVerified: z.boolean().optional(),
  createdAt: z.string().optional(),
});

/**
 * Profile data type
 */
export type ProfileData = z.infer<typeof profileSchema>;
