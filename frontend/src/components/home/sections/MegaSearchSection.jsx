import { Search } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "@/components/global/Button";

const chips = [
  { label: "Electric", params: "fuelType=electric" },
  { label: "SUVs", params: "bodyType=suv" },
  { label: "Sedan", params: "bodyType=sedan" },
  { label: "Under $15K", params: "maxPrice=15000" },
  { label: "Under $25K", params: "maxPrice=25000" }
];

export default function MegaSearchSection() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [bodyType, setBodyType] = useState("");
  const [priceRange, setPriceRange] = useState("");

  const handleSearch = () => {
    const params = new URLSearchParams();
    if (query.trim()) params.set("q", query.trim());
    if (bodyType) params.set("bodyType", bodyType);
    if (priceRange) params.set("maxPrice", priceRange);
    navigate(`/listings?${params.toString()}`);
  };

  const handleChip = (chipParams) => {
    navigate(`/listings?${chipParams}`);
  };

  return (
    <section className="relative z-20 -mt-20 px-6">
      <div className="mx-auto w-full max-w-content">
        <div className="rounded-2xl border border-gray-100 bg-white p-2 shadow-blue-xl">
          <div className="flex flex-col gap-2 lg:flex-row lg:items-center">
            <label className="relative flex-1">
              <Search size={18} strokeWidth={1.5} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                placeholder="Search make, model, or keyword..."
                className="w-full rounded-xl border border-transparent bg-transparent py-4 pl-11 pr-3 text-[16px] outline-none transition-all focus:border-blue/20 focus:bg-blue/5"
              />
            </label>
            <select
              value={bodyType}
              onChange={(e) => setBodyType(e.target.value)}
              className="rounded-xl border border-gray-200 px-4 py-4 text-sm text-gray-700 outline-none focus:border-blue"
            >
              <option value="">All Types</option>
              <option value="sedan">Sedan</option>
              <option value="suv">SUV</option>
              <option value="hatchback">Hatchback</option>
              <option value="truck">Truck</option>
              <option value="van">Van</option>
              <option value="coupe">Coupe</option>
            </select>
            <select
              value={priceRange}
              onChange={(e) => setPriceRange(e.target.value)}
              className="rounded-xl border border-gray-200 px-4 py-4 text-sm text-gray-700 outline-none focus:border-blue"
            >
              <option value="">Any Price</option>
              <option value="10000">Under $10K</option>
              <option value="25000">Under $25K</option>
              <option value="50000">Under $50K</option>
              <option value="100000">Under $100K</option>
            </select>
            <Button className="rounded-xl px-8 py-4" onClick={handleSearch}>Search</Button>
          </div>
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          {chips.map((chip) => (
            <button
              key={chip.label}
              onClick={() => handleChip(chip.params)}
              className="rounded-full bg-gray-100 px-4 py-2 text-sm font-medium text-gray-600 transition-colors hover:bg-blue hover:text-white"
            >
              {chip.label}
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
