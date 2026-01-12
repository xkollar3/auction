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
  setUser: (user: User | null) => void;
  setToken: (token: string | null) => void;
  clearUser: () => void;
  setCustomerId: (customerId: string) => void;
  setSellerId: (sellerId: string) => void;
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
    }),
    {
      name: 'auth-storage', // localStorage key
      partialize: (state) => ({
        // Only persist user and token, not isAuthenticated (computed from user)
        user: state.user,
        token: state.token,
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
