import { create } from "zustand";

const MAX_COMPARE_ITEMS = 3;

export const useCompareStore = create((set) => ({
  selectedCars: [],
  addCar: (car) =>
    set((state) => {
      if (state.selectedCars.find((c) => c.id === car.id)) {
        return state;
      }
      if (state.selectedCars.length >= MAX_COMPARE_ITEMS) {
        return state;
      }
      return { selectedCars: [...state.selectedCars, car] };
    }),
  removeCar: (carId) =>
    set((state) => ({
      selectedCars: state.selectedCars.filter((car) => car.id !== carId)
    })),
  clearAll: () => set({ selectedCars: [] })
}));

