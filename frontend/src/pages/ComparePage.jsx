import { Plus, X } from "lucide-react";
import { useEffect, useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";
import PageTransition from "@/components/global/PageTransition";
import { fetchListings } from "@/lib/listingsService";
import { quickCompare } from "@/lib/compareService";
import { useCompareStore } from "@/store/compareStore";

const EMPTY_SLOTS = 3;

function formatMoney(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0
  }).format(value);
}

export default function ComparePage() {
  const { selectedCars, addCar, removeCar, clearAll } = useCompareStore();
  const [availableCars, setAvailableCars] = useState([]);
  const [compareData, setCompareData] = useState(null);

  // Load available cars from real API to populate selectors
  useEffect(() => {
    fetchListings({})
      .then((cars) => setAvailableCars(cars))
      .catch(() => setAvailableCars([]));
  }, []);

  // Fetch server-side comparison when selection changes
  useEffect(() => {
    if (selectedCars.length < 2) {
      setCompareData(null);
      return;
    }
    const ids = selectedCars.map((c) => c.id);
    quickCompare(ids)
      .then((data) => setCompareData(data))
      .catch(() => setCompareData(null));
  }, [selectedCars]);

  const dropdownOptions = availableCars.filter(
    (car) => !selectedCars.some((selected) => selected.id === car.id)
  );

  const bestPrice = selectedCars.length ? Math.min(...selectedCars.map((c) => c.priceValue)) : null;
  const bestMileage = selectedCars.length ? Math.min(...selectedCars.map((c) => c.mileageValue)) : null;
  const bestYear = selectedCars.length ? Math.max(...selectedCars.map((c) => c.year)) : null;

  const rows = [
    { label: "Price", getDisplay: (c) => formatMoney(c.priceValue), isBest: (c) => c.priceValue === bestPrice },
    { label: "Mileage", getDisplay: (c) => `${c.mileageValue.toLocaleString()} km`, isBest: (c) => c.mileageValue === bestMileage },
    { label: "Year", getDisplay: (c) => c.year, isBest: (c) => c.year === bestYear },
    { label: "Fuel", getDisplay: (c) => c.fuel },
    { label: "Transmission", getDisplay: (c) => c.transmission },
    { label: "Body Type", getDisplay: (c) => c.bodyType.toUpperCase() }
  ];

  const handleSelect = (value, eventTarget) => {
    const next = availableCars.find((car) => car.id === value) ||
                 selectedCars.find((car) => car.id === value);
    if (next) addCar(next);
    if (eventTarget) eventTarget.value = "";
  };

  return (
    <PageTransition>
      <Helmet>
        <title>Compare Vehicles | AutoVault</title>
        <meta name="description" content="Compare up to three vehicles side-by-side across key specifications and pricing." />
      </Helmet>
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-10">
        <section className="mx-auto max-w-content">
          <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h1 className="font-montserrat text-3xl font-black text-gray-900">COMPARE VEHICLES</h1>
              <p className="mt-1 text-gray-500">Side-by-side comparison of up to 3 vehicles.</p>
            </div>
            <div className="flex items-center gap-2">
              <select
                className="rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue"
                defaultValue=""
                onChange={(e) => { handleSelect(e.target.value, e.target); }}
                disabled={selectedCars.length >= EMPTY_SLOTS}
              >
                <option value="" disabled>+ Add Vehicle</option>
                {dropdownOptions.map((car) => (
                  <option key={car.id} value={car.id}>{car.title}</option>
                ))}
              </select>
              {selectedCars.length ? (
                <Button variant="ghost" size="sm" onClick={clearAll}>Clear</Button>
              ) : null}
            </div>
          </div>

          {!selectedCars.length ? (
            <div className="rounded-3xl border border-dashed border-gray-200 bg-white p-12 text-center shadow-blue-sm">
              <p className="text-lg font-semibold text-gray-900">No cars selected for comparison yet</p>
              <p className="mt-2 text-gray-500">Add vehicles from Listings or use the selector above.</p>
              <Button as={Link} to="/listings" className="mt-5">Browse Listings</Button>
            </div>
          ) : (
            <div className="overflow-x-auto rounded-3xl border border-gray-100 bg-white shadow-blue-sm">
              <table className="w-full min-w-[880px] border-collapse">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="w-56 bg-gray-50 px-5 py-4 text-left text-sm font-semibold text-gray-500">
                      Specification
                    </th>
                    {Array.from({ length: EMPTY_SLOTS }).map((_, index) => {
                      const car = selectedCars[index];
                      return (
                        <th key={index} className="px-5 py-4 text-left align-top">
                          {car ? (
                            <div className="relative rounded-2xl border border-gray-100 p-3">
                              <button
                                onClick={() => removeCar(car.id)}
                                className="absolute right-2 top-2 rounded-full bg-gray-100 p-1 text-gray-500 hover:text-gray-700"
                              >
                                <X size={14} strokeWidth={1.5} />
                              </button>
                              <img src={car.image} alt={car.title} className="h-28 w-full rounded-xl object-cover" />
                              <p className="mt-2 text-sm font-semibold text-gray-900">{car.title}</p>
                              <p className="mt-1 font-mono text-lg font-bold text-blue">{car.price}</p>
                            </div>
                          ) : (
                            <label className="flex h-full cursor-pointer flex-col items-center justify-center rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-6 text-gray-500">
                              <Plus size={18} strokeWidth={1.5} />
                              <span className="mt-1 text-xs font-semibold">Add Vehicle</span>
                              <select
                                className="mt-2 w-full rounded-lg border border-gray-200 bg-white px-2 py-1 text-xs"
                                defaultValue=""
                                onChange={(e) => { handleSelect(e.target.value, e.target); }}
                              >
                                <option value="" disabled>Select...</option>
                                {dropdownOptions.map((item) => (
                                  <option key={item.id} value={item.id}>{item.title}</option>
                                ))}
                              </select>
                            </label>
                          )}
                        </th>
                      );
                    })}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => (
                    <tr key={row.label} className="border-b border-gray-100 last:border-b-0">
                      <td className="bg-gray-50 px-5 py-4 text-sm font-semibold text-gray-700">{row.label}</td>
                      {Array.from({ length: EMPTY_SLOTS }).map((_, index) => {
                        const car = selectedCars[index];
                        const best = car && row.isBest ? row.isBest(car) : false;
                        return (
                          <td
                            key={`${row.label}-${index}`}
                            className={best
                              ? "bg-emerald-50 px-5 py-4 text-sm font-semibold text-emerald-700"
                              : "px-5 py-4 text-sm text-gray-700"}
                          >
                            {car ? row.getDisplay(car) : "-"}
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </PageTransition>
  );
}
