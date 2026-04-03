import { create } from "zustand";

const initialFilters = {
  query: "",
  bodyType: "all",
  fuelType: "all",
  sortBy: "recent",
  minPrice: null,
  maxPrice: null,
};

export const useCarStore = create((set, get) => ({
  // State
  cars: [],
  featuredCars: [],
  currentListing: null,
  listings: [],
  filters: initialFilters,
  isLoading: false,
  error: null,
  pagination: {
    page: 0,
    size: 12,
    hasMore: true,
    total: 0,
  },

  // Actions
  setCars: (cars) => set({ cars }),

  setFeaturedCars: (featuredCars) => set({ featuredCars }),

  setCurrentListing: (listing) => set({ currentListing: listing }),

  setListings: (listings) => set({ listings }),

  addListings: (newListings) =>
    set((state) => ({
      listings: [...state.listings, ...newListings],
    })),

  updateFilters: (nextFilters) =>
    set((state) => ({
      filters: { ...state.filters, ...nextFilters },
      pagination: { ...state.pagination, page: 0 },
    })),

  resetFilters: () =>
    set({
      filters: initialFilters,
      pagination: { page: 0, size: 12, hasMore: true, total: 0 },
    }),

  setLoading: (loading) => set({ isLoading: loading }),

  setError: (error) => set({ error }),

  setPagination: (pagination) =>
    set((state) => ({
      pagination: { ...state.pagination, ...pagination },
    })),

  setCurrentPage: (page) =>
    set((state) => ({
      pagination: { ...state.pagination, page },
    })),
}));


