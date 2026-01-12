/**
 * User entity interface
 *
 * Represents a user in the system with authentication and payment info
 */
export interface User {
  id: string;
  email: string;
  name: string;
  phone?: string;
  customerId?: string; // Stripe customer ID for buyers
  sellerId?: string; // Stripe seller account ID for sellers
}

/**
 * User profile interface
 *
 * Subset of user data for profile display (read-only)
 */
export interface UserProfile {
  id: string;
  email: string;
  name: string;
  phone?: string;
  emailVerified?: boolean;
  createdAt?: string;
}

/**
 * Keycloak token parsed data
 *
 * Fields extracted from Keycloak JWT token
 */
export interface KeycloakToken {
  sub: string; // User ID
  email: string;
  name?: string;
  given_name?: string;
  family_name?: string;
  preferred_username?: string;
  email_verified?: boolean;
  phone_number?: string;
}

/**
 * Authentication context
 *
 * Provides auth state and methods to components
 */
export interface AuthContext {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  login: () => void;
  logout: () => void;
  register: () => void;
}
