import { useKeycloak } from "@react-keycloak/web";
import { useAuthStore } from "../stores/authStore";
import { type User } from "../types/user";
import { userSchema, keycloakTokenSchema } from "../schemas/user";
import { useEffect, useCallback } from "react";
import { registerUser } from "../api/user";

// Module-level lock to prevent duplicate registration calls (survives StrictMode remounts)
let registrationInFlight = false;

/**
 * Authentication hook
 *
 * Provides authentication state and methods
 * Combines Keycloak instance with Zustand auth store
 */
export const useAuth = () => {
  const { keycloak, initialized } = useKeycloak();
  const {
    user,
    token,
    isAuthenticated,
    isRegistering,
    registrationError,
    backendUserId,
    setUser,
    setToken,
    clearUser,
    setRegistering,
    setRegistrationError,
    setBackendUserId,
  } = useAuthStore();

  /**
   * Retry registration after failure
   */
  const retryRegistration = useCallback(async () => {
    if (registrationInFlight) return;
    registrationInFlight = true;

    setRegistering(true);
    setRegistrationError(null);

    try {
      console.log("Registering user with backend...");
      const response = await registerUser();
      console.log("Backend registration successful:", response);
      setBackendUserId(response.aggregateId);
    } catch (error: unknown) {
      // 409 means user already exists - treat as success
      if (
        error &&
        typeof error === "object" &&
        "response" in error &&
        (error as { response?: { status?: number } }).response?.status === 409
      ) {
        console.log("User already registered (409), proceeding...");
        // Use keycloak user id as backend id since user exists
        setBackendUserId("existing");
      } else {
        console.error("Backend registration failed:", error);
        const message = error instanceof Error ? error.message : "Registration failed";
        setRegistrationError(message);
      }
    } finally {
      setRegistering(false);
      registrationInFlight = false;
    }
  }, [setRegistering, setRegistrationError, setBackendUserId]);

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

        // Update store with user info and token
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
   * After Keycloak auth and token stored, register with backend if needed
   */
  useEffect(() => {
    const state = useAuthStore.getState();
    if (
      initialized &&
      keycloak.authenticated &&
      token &&
      !state.backendUserId &&
      !registrationInFlight
    ) {
      retryRegistration();
    }
    // Only run when keycloak auth state or token changes
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialized, keycloak.authenticated, token]);

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

  // User is fully ready when KC is authenticated AND backend registration is complete
  const isReady = isAuthenticated && keycloak.authenticated && !!backendUserId;

  return {
    isAuthenticated: isAuthenticated && keycloak.authenticated,
    isReady,
    isRegistering,
    registrationError,
    user,
    token: getToken(),
    login,
    register,
    logout,
    retryRegistration,
    getUserProfile,
    keycloak,
    initialized,
  };
};
