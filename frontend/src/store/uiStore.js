import { create } from "zustand";

export const useUiStore = create((set) => ({
  // Mobile & Menu
  mobileMenuOpen: false,
  compareBarOpen: false,
  filterDrawerOpen: false,

  // Modals
  modals: {
    auth: false,
    emiCalculator: false,
    imageGallery: false,
    bookingFlow: false,
    searchFilters: false,
  },

  // Loading & Status
  isLoading: false,
  globalError: null,
  globalSuccess: null,

  // Theme (can be extended for dark mode)
  theme: 'light',

  // Actions
  toggleMobileMenu: () =>
    set((state) => ({ mobileMenuOpen: !state.mobileMenuOpen })),

  setMobileMenuOpen: (open) => set({ mobileMenuOpen: open }),

  closeMobileMenu: () => set({ mobileMenuOpen: false }),

  setCompareBarOpen: (compareBarOpen) => set({ compareBarOpen }),

  setFilterDrawerOpen: (open) => set({ filterDrawerOpen: open }),

  // Modal Actions
  openModal: (modalName) =>
    set((state) => ({
      modals: { ...state.modals, [modalName]: true },
    })),

  closeModal: (modalName) =>
    set((state) => ({
      modals: { ...state.modals, [modalName]: false },
    })),

  toggleModal: (modalName) =>
    set((state) => ({
      modals: { ...state.modals, [modalName]: !state.modals[modalName] },
    })),

  closeAllModals: () =>
    set({
      modals: {
        auth: false,
        emiCalculator: false,
        imageGallery: false,
        bookingFlow: false,
        searchFilters: false,
      },
    }),

  // Loading & Status Actions
  setLoading: (isLoading) => set({ isLoading }),

  setError: (error) => set({ globalError: error }),

  setSuccess: (message) => set({ globalSuccess: message }),

  clearError: () => set({ globalError: null }),

  clearSuccess: () => set({ globalSuccess: null }),

  // Theme
  setTheme: (theme) => set({ theme }),
}));


