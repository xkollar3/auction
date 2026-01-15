import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * User state interface
 */
export interface User {
  id: string;
  email: string;
  name: string;
  phone?: string;
  customerId?: string; // Stripe customer ID
  sellerId?: string; // Stripe seller account ID
}

/**
 * Authentication store state
 */
interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isRegistering: boolean;
  registrationError: string | null;
  backendUserId: string | null;
  setUser: (user: User | null) => void;
  setToken: (token: string | null) => void;
  clearUser: () => void;
  setCustomerId: (customerId: string) => void;
  setSellerId: (sellerId: string) => void;
  setRegistering: (isRegistering: boolean) => void;
  setRegistrationError: (error: string | null) => void;
  setBackendUserId: (id: string) => void;
}

/**
 * Authentication store using Zustand
 *
 * Persisted to localStorage for session persistence
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isRegistering: false,
      registrationError: null,
      backendUserId: null,

      /**
       * Set the current user and mark as authenticated
       */
      setUser: (user) =>
        set({
          user,
          isAuthenticated: !!user,
        }),

      /**
       * Set the JWT token
       */
      setToken: (token) =>
        set({
          token,
        }),

      /**
       * Clear user data and logout
       */
      clearUser: () =>
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isRegistering: false,
          registrationError: null,
          backendUserId: null,
        }),

      /**
       * Update user's Stripe customer ID
       */
      setCustomerId: (customerId) =>
        set((state) => ({
          user: state.user ? { ...state.user, customerId } : null,
        })),

      /**
       * Update user's Stripe seller ID
       */
      setSellerId: (sellerId) =>
        set((state) => ({
          user: state.user ? { ...state.user, sellerId } : null,
        })),

      /**
       * Set registration loading state
       */
      setRegistering: (isRegistering) =>
        set({ isRegistering }),

      /**
       * Set registration error
       */
      setRegistrationError: (registrationError) =>
        set({ registrationError }),

      /**
       * Set backend user ID after successful registration
       */
      setBackendUserId: (backendUserId) =>
        set({ backendUserId }),
    }),
    {
      name: 'auth-storage', // localStorage key
      partialize: (state) => ({
        // Persist user, token, and backendUserId
        user: state.user,
        token: state.token,
        backendUserId: state.backendUserId,
      }),
      onRehydrateStorage: () => (state) => {
        // After rehydration, set isAuthenticated based on user presence
        if (state && state.user) {
          state.isAuthenticated = true;
        }
      },
    }
  )
);
