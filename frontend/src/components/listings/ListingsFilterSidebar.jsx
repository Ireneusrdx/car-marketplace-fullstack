import Button from "@/components/global/Button";

const fuelTypes = ["all", "petrol", "diesel", "electric", "hybrid"];
const bodyTypes = [
  { label: "All", value: "all" },
  { label: "Sedan", value: "SEDAN" },
  { label: "SUV", value: "SUV" },
  { label: "Coupe", value: "COUPE" },
  { label: "Truck", value: "TRUCK" }
];

export default function ListingsFilterSidebar({ filters, onUpdate, onReset }) {
  return (
    <aside className="sticky top-24 h-fit rounded-3xl border border-gray-100 bg-white p-6 shadow-blue-sm">
      <div className="mb-6 flex items-center justify-between">
        <h3 className="text-sm font-semibold uppercase tracking-[0.14em] text-gray-900">Filters</h3>
        <button onClick={onReset} className="text-sm font-semibold text-blue">
          Clear All
        </button>
      </div>

      <div className="space-y-6">
        <div className="border-b border-gray-100 pb-6">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.14em] text-gray-500">Price Range</p>
          <div className="grid grid-cols-2 gap-2">
            <input
              type="number"
              value={filters.minPrice ?? ""}
              placeholder="Min"
              onChange={(event) =>
                onUpdate({ minPrice: event.target.value ? Number(event.target.value) : null })
              }
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue"
            />
            <input
              type="number"
              value={filters.maxPrice ?? ""}
              placeholder="Max"
              onChange={(event) =>
                onUpdate({ maxPrice: event.target.value ? Number(event.target.value) : null })
              }
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue"
            />
          </div>
        </div>

        <div className="border-b border-gray-100 pb-6">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.14em] text-gray-500">Fuel Type</p>
          <div className="grid grid-cols-2 gap-2">
            {fuelTypes.map((item) => (
              <button
                key={item}
                onClick={() => onUpdate({ fuelType: item })}
                className={
                  filters.fuelType === item
                    ? "rounded-xl border border-blue bg-blue/5 px-3 py-2 text-sm font-semibold capitalize text-blue"
                    : "rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm capitalize text-gray-600"
                }
              >
                {item}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.14em] text-gray-500">Body Type</p>
          <div className="grid grid-cols-2 gap-2">
            {bodyTypes.map((item) => (
              <button
                key={item.value}
                onClick={() => onUpdate({ bodyType: item.value })}
                className={
                  filters.bodyType === item.value
                    ? "rounded-xl border border-blue bg-blue/5 px-3 py-2 text-sm font-semibold text-blue"
                    : "rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-600"
                }
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <Button className="mt-6 w-full">Apply Filters</Button>
    </aside>
  );
}

