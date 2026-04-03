import { useEffect, useMemo, useState } from "react";
import { Helmet } from "react-helmet-async";
import CarCard from "@/components/home/CarCard";
import CompareFloatingBar from "@/components/listings/CompareFloatingBar";
import ListingsFilterSidebar from "@/components/listings/ListingsFilterSidebar";
import ListingsSearchHero from "@/components/listings/ListingsSearchHero";
import ListingsSortViewBar from "@/components/listings/ListingsSortViewBar";
import { fetchListings } from "@/lib/listingsService";
import { notify } from "@/components/global/ToastProvider";
import { useCarStore } from "@/store/carStore";
import { useCompareStore } from "@/store/compareStore";

export default function ListingsPage() {
  const { cars, filters, setCars, updateFilters, resetFilters } = useCarStore();
  const { selectedCars, addCar, removeCar, clearAll } = useCompareStore();
  const [viewMode, setViewMode] = useState("grid");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadListings() {
      setLoading(true);
      try {
        const remoteCars = await fetchListings(filters);
        if (!isMounted) return;
        setCars(remoteCars);
      } catch {
        if (!isMounted) return;
        setCars([]);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadListings();
    return () => {
      isMounted = false;
    };
  }, [filters, setCars]);

  const filteredCars = useMemo(() => {
    const filtered = cars.filter((car) => {
      const query = filters.query.trim().toLowerCase();
      const queryMatch = !query || car.title.toLowerCase().includes(query) || car.location.toLowerCase().includes(query);
      const bodyMatch = filters.bodyType === "all" || car.bodyType.toUpperCase() === filters.bodyType.toUpperCase();
      const fuelMatch = filters.fuelType === "all" || car.fuel.toLowerCase() === filters.fuelType;
      const minMatch = filters.minPrice == null || car.priceValue >= filters.minPrice;
      const maxMatch = filters.maxPrice == null || car.priceValue <= filters.maxPrice;
      return queryMatch && bodyMatch && fuelMatch && minMatch && maxMatch;
    });

    switch (filters.sortBy) {
      case "priceAsc":
        return [...filtered].sort((a, b) => a.priceValue - b.priceValue);
      case "priceDesc":
        return [...filtered].sort((a, b) => b.priceValue - a.priceValue);
      case "mileageAsc":
        return [...filtered].sort((a, b) => a.mileageValue - b.mileageValue);
      case "recent":
      default:
        return [...filtered].sort((a, b) => a.listedDays - b.listedDays);
    }
  }, [cars, filters]);

  const toggleCompare = (car) => {
    const exists = selectedCars.some((item) => item.id === car.id);
    if (exists) {
      removeCar(car.id);
      return;
    }
    if (selectedCars.length >= 3) {
      notify.error("You can compare up to 3 vehicles.");
      return;
    }
    addCar(car);
  };

  return (
    <main className="min-h-screen bg-[var(--white-soft)]">
      <Helmet>
        <title>Browse Listings | AutoVault</title>
        <meta name="description" content="Search and filter verified car listings by price, fuel type, body style, and more." />
      </Helmet>
      <ListingsSearchHero filters={filters} onUpdate={updateFilters} />

      <section className="mx-auto flex w-full max-w-content gap-8 px-6 py-8">
        <div className="hidden w-[280px] lg:block">
          <ListingsFilterSidebar filters={filters} onUpdate={updateFilters} onReset={resetFilters} />
        </div>

        <div className="min-w-0 flex-1">
          <ListingsSortViewBar
            count={loading ? 0 : filteredCars.length}
            sortBy={filters.sortBy}
            onSort={(sortBy) => updateFilters({ sortBy })}
            viewMode={viewMode}
            onViewMode={setViewMode}
          />

          {loading ? (
            <div className="rounded-2xl border border-gray-100 bg-white p-12 text-center shadow-blue-sm">
              <h3 className="text-xl font-semibold text-gray-900">Loading vehicles...</h3>
              <p className="mt-2 text-gray-500">Fetching latest listings from the backend.</p>
            </div>
          ) : null}

          {!loading ? (
            <div className={viewMode === "grid" ? "grid gap-6 md:grid-cols-2 xl:grid-cols-3" : "space-y-4"}>
              {filteredCars.map((car) => (
                <CarCard
                  key={car.id}
                  car={car}
                  viewMode={viewMode}
                  compareSelected={selectedCars.some((item) => item.id === car.id)}
                  onToggleCompare={toggleCompare}
                />
              ))}
            </div>
          ) : null}

          {!loading && !filteredCars.length ? (
            <div className="rounded-2xl border border-gray-100 bg-white p-12 text-center shadow-blue-sm">
              <h3 className="text-xl font-semibold text-gray-900">No vehicles found</h3>
              <p className="mt-2 text-gray-500">Try adjusting filters or clearing them to see more listings.</p>
            </div>
          ) : null}
        </div>
      </section>

      <CompareFloatingBar selectedCars={selectedCars} onRemove={removeCar} onClear={clearAll} />
    </main>
  );
}



