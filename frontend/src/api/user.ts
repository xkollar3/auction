import { api } from "../lib/api";

/**
 * Response from user registration endpoint
 */
export interface UserRegistrationResponse {
  aggregateId: string;
}

/**
 * Response from user profile endpoint
 */
export interface UserProfileResponse {
  id: string;
  keycloakUserId: string;
  stripeCustomerId: string | null;
  stripeSellerAccountId: string | null;
  stripeOnboardingLink: string | null;
  stripePaymentMethodId: string | null;
  sellerAccountEnabled: boolean;
}

/**
 * Register user in the backend system
 *
 * Should be called after Keycloak registration/login to create
 * the user record in the backend (via JWT token)
 *
 * @returns User aggregate ID
 */
export const registerUser = async (): Promise<UserRegistrationResponse> => {
  const response = await api.post<UserRegistrationResponse>("/api/users/register");
  return response.data;
};

/**
 * Delete the current user account
 */
export const deleteUser = async (): Promise<void> => {
  await api.delete("/api/users/me");
};

/**
 * Get current user's profile from backend
 */
export const getUserProfile = async (): Promise<UserProfileResponse> => {
  const response = await api.get<UserProfileResponse>("/api/users/me");
  return response.data;
};
