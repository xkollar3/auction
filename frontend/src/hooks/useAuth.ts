import { useKeycloak } from "@react-keycloak/web";
import { useAuthStore } from "../stores/authStore";
import { type User } from "../types/user";
import { userSchema, keycloakTokenSchema } from "../schemas/user";
import { useEffect } from "react";

/**
 * Authentication hook
 *
 * Provides authentication state and methods
 * Combines Keycloak instance with Zustand auth store
 */
export const useAuth = () => {
  const { keycloak, initialized } = useKeycloak();
  const { user, token, isAuthenticated, setUser, setToken, clearUser } =
    useAuthStore();

  /**
   * Sync Keycloak state with auth store on initialization
   */
  useEffect(() => {
    if (initialized && keycloak.authenticated) {
      try {
        console.log("Keycloak authenticated, parsing token...");
        console.log("Token parsed data:", keycloak.tokenParsed);

        // Parse and validate token data
        const tokenData = keycloakTokenSchema.parse(keycloak.tokenParsed);
        console.log("Token data validated:", tokenData);

        // Create user object from token
        // Email fallback: use preferred_username or a placeholder if email is missing
        const email = tokenData.email || `${tokenData.preferred_username || tokenData.sub}@temp.local`;

        const userData: User = {
          id: tokenData.sub,
          email: email,
          name:
            tokenData.name || tokenData.preferred_username || email,
          phone: tokenData.phone_number,
        };

        // Validate user data
        const validatedUser = userSchema.parse(userData);
        console.log("User data validated:", validatedUser);

        // Update store
        setUser(validatedUser);
        setToken(keycloak.token || null);
        console.log("User authenticated and stored successfully");
      } catch (error) {
        console.error("Failed to parse Keycloak token:", error);
        console.error("Token parsed data:", keycloak.tokenParsed);
        clearUser();
      }
    } else if (initialized && !keycloak.authenticated) {
      // User is not authenticated, clear store
      console.log("Keycloak not authenticated, clearing user");
      clearUser();
    }
  }, [
    initialized,
    keycloak.authenticated,
    keycloak.token,
    keycloak.tokenParsed,
    setUser,
    setToken,
    clearUser,
  ]);

  /**
   * Initiate login flow
   */
  const login = () => {
    keycloak.login();
  };

  /**
   * Initiate registration flow
   */
  const register = () => {
    keycloak.register();
  };

  /**
   * Initiate logout flow
   */
  const logout = () => {
    clearUser();
    keycloak.logout();
  };

  /**
   * Get current JWT token
   */
  const getToken = (): string | null => {
    return keycloak.token || token;
  };

  /**
   * Get user profile data from token
   */
  const getUserProfile = () => {
    if (!user) return null;

    return {
      id: user.id,
      email: user.email,
      name: user.name,
      phone: user.phone,
      emailVerified: keycloak.tokenParsed?.email_verified,
    };
  };

  return {
    isAuthenticated: isAuthenticated && keycloak.authenticated,
    user,
    token: getToken(),
    login,
    register,
    logout,
    getUserProfile,
    keycloak, // Expose keycloak instance for advanced usage
    initialized,
  };
};
