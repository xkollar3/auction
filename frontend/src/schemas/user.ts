import { z } from 'zod';

/**
 * User profile schema
 *
 * Validates user data from Keycloak JWT token
 */
export const userSchema = z.object({
  id: z.string().min(1), // Changed from UUID to allow any string ID from Keycloak
  email: z.string().email(),
  name: z.string().min(1),
  phone: z.string().optional(),
  customerId: z.string().optional(), // Stripe customer ID
  sellerId: z.string().optional(), // Stripe seller account ID
});

/**
 * User profile display schema
 *
 * Subset of user data for profile display
 */
export const userProfileSchema = z.object({
  id: z.string(),
  email: z.string().email(),
  name: z.string(),
  phone: z.string().optional(),
  emailVerified: z.boolean().optional(),
  createdAt: z.string().optional(),
});

/**
 * Keycloak token parsed data schema
 *
 * Expected fields from Keycloak JWT token
 * Note: Making email optional since it might not be immediately available after registration
 */
export const keycloakTokenSchema = z.object({
  sub: z.string(), // User ID
  email: z.string().email().optional(),
  name: z.string().optional(),
  given_name: z.string().optional(),
  family_name: z.string().optional(),
  preferred_username: z.string().optional(),
  email_verified: z.boolean().optional(),
  phone_number: z.string().optional(),
});

/**
 * Infer types from schemas
 */
export type UserSchemaType = z.infer<typeof userSchema>;
export type UserProfileSchemaType = z.infer<typeof userProfileSchema>;
export type KeycloakTokenType = z.infer<typeof keycloakTokenSchema>;
