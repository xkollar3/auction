import Keycloak from 'keycloak-js';

/**
 * Keycloak client instance configuration
 *
 * Configuration sourced from environment variables:
 * - VITE_KEYCLOAK_URL: Keycloak server URL (e.g., http://localhost:8089)
 * - VITE_KEYCLOAK_REALM: Realm name (auction-marketplace)
 * - VITE_KEYCLOAK_CLIENT_ID: Client ID (auction-ui-client)
 */
export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});

/**
 * Keycloak initialization configuration
 *
 * onLoad: 'check-sso' - Check if user is already logged in without forcing login
 * pkceMethod: 'S256' - Use PKCE for enhanced security
 */
export const keycloakInitConfig = {
  onLoad: 'check-sso' as const,
  pkceMethod: 'S256' as const,
  checkLoginIframe: false, // Disable iframe checks for better performance
};

/**
 * Helper function to initiate Keycloak registration flow
 */
export const initiateRegistration = () => {
  keycloak.register();
};

/**
 * Helper function to initiate Keycloak login flow
 */
export const initiateLogin = () => {
  keycloak.login();
};

/**
 * Helper function to initiate Keycloak logout flow
 */
export const initiateLogout = () => {
  keycloak.logout();
};

/**
 * Get the current JWT token from Keycloak
 */
export const getToken = (): string | undefined => {
  return keycloak.token;
};

/**
 * Get user information from the token
 */
export const getUserInfo = () => {
  return keycloak.tokenParsed;
};

/**
 * Check if user is authenticated
 */
export const isAuthenticated = (): boolean => {
  return keycloak.authenticated ?? false;
};
