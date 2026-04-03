import axios from "axios";
import { create } from "zustand";
import { getCurrentUser, logout as authServiceLogout } from "../lib/authService";

// Mirror the same base-URL logic used in api.js (no circular import)
const _envUrl = import.meta.env.VITE_API_URL;
const _apiBase = _envUrl ? `${_envUrl}/api` : "/api";

export const useAuthStore = create((set, get) => ({
  // State
  user: null,
  accessToken: null,
  isAuthenticated: false,
  isLoading: true,
  error: null,

  // Auth Actions
  setAuth: ({ user, accessToken }) => {
    set({
      user: user || null,
      accessToken: accessToken || null,
      isAuthenticated: Boolean(user && accessToken),
      error: null,
    });
  },

  clearAuth: () => {
    set({
      user: null,
      accessToken: null,
      isAuthenticated: false,
      error: null,
    });
  },

  logout: async () => {
    try {
      await authServiceLogout();
    } finally {
      get().clearAuth();
      // Use navigate-style redirect instead of hard reload to avoid loops
      if (!window.location.pathname.startsWith("/auth")) {
        window.location.href = "/auth";
      }
    }
  },

  initialize: async () => {
    set({ isLoading: true });
    try {
      // Try fetching the current user with whatever token is in state
      const user = await getCurrentUser();
      set({ user, isAuthenticated: true, isLoading: false });
    } catch {
      // /auth/me failed (no token or expired) — try refresh via HttpOnly cookie
      try {
        const { data } = await axios.post(
          `${_apiBase}/auth/refresh`,
          {},
          { withCredentials: true }
        );
        // Store the new access token so the next /auth/me carries it
        set({ accessToken: data.accessToken });
        const user = await getCurrentUser();
        set({ user, isAuthenticated: true, isLoading: false });
      } catch {
        // Refresh also failed — user is not logged in, do NOT redirect
        set({ user: null, accessToken: null, isAuthenticated: false, isLoading: false });
      }
    }
  },

  setLoading: (loading) => set({ isLoading: loading }),

  setError: (error) => set({ error }),

  clearError: () => set({ error: null }),

  // User profile update
  updateUser: (updates) =>
    set((state) => ({
      user: { ...state.user, ...updates },
    })),

  // Role checks
  isAdmin: () => get().user?.role === "ADMIN",

  isSeller: () => {
    const role = get().user?.role;
    return role === "SELLER" || role === "DEALER" || role === "ADMIN";
  },

  isBuyer: () => get().user?.role === "BUYER",

  isVerifiedSeller: () => get().user?.verifiedSeller || false,
}));
