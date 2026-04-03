import { Search, X } from "lucide-react";
import { cn } from "@/lib/cn";

export default function ListingsSearchHero({ filters, onUpdate }) {
  const quickFilters = [
    { label: "Electric", key: "fuelType", value: "electric" },
    { label: "SUV", key: "bodyType", value: "SUV" },
    { label: "Under $30K", key: "maxPrice", value: 30000 }
  ];

  return (
    <section className="border-b border-gray-100 bg-white px-6 py-6">
      <div className="mx-auto w-full max-w-content">
        <div className="rounded-2xl border border-gray-100 bg-white p-2 shadow-blue-sm">
          <div className="flex flex-col gap-2 lg:flex-row lg:items-center">
            <label className="relative flex-1">
              <Search size={18} strokeWidth={1.5} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                value={filters.query}
                onChange={(event) => onUpdate({ query: event.target.value })}
                placeholder="Search make, model, or keyword..."
                className="w-full rounded-xl border border-transparent py-3.5 pl-11 pr-3 text-[15px] outline-none transition-all focus:border-blue/20 focus:bg-blue/5"
              />
            </label>
            <select
              value={filters.bodyType}
              onChange={(event) => onUpdate({ bodyType: event.target.value })}
              className="rounded-xl border border-gray-200 px-4 py-3.5 text-sm text-gray-700 outline-none focus:border-blue"
            >
              <option value="all">All Types</option>
              <option value="SEDAN">Sedan</option>
              <option value="SUV">SUV</option>
              <option value="COUPE">Coupe</option>
              <option value="TRUCK">Truck</option>
            </select>
            <select
              value={filters.fuelType}
              onChange={(event) => onUpdate({ fuelType: event.target.value })}
              className="rounded-xl border border-gray-200 px-4 py-3.5 text-sm text-gray-700 outline-none focus:border-blue"
            >
              <option value="all">Any Fuel</option>
              <option value="petrol">Petrol</option>
              <option value="diesel">Diesel</option>
              <option value="electric">Electric</option>
              <option value="hybrid">Hybrid</option>
            </select>
          </div>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-2">
          {quickFilters.map((chip) => {
            const active = filters[chip.key] === chip.value;
            return (
              <button
                key={chip.label}
                onClick={() => onUpdate({ [chip.key]: active ? (chip.key === "maxPrice" ? null : "all") : chip.value })}
                className={cn(
                  "inline-flex items-center gap-1 rounded-full px-3 py-1.5 text-xs font-semibold transition-colors",
                  active ? "bg-blue text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                )}
              >
                {chip.label}
                {active ? <X size={12} strokeWidth={1.5} /> : null}
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
}

