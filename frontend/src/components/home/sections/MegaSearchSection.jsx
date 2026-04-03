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
        <div className="rounded-2xl border border-white/40 bg-white/95 p-3 shadow-2xl shadow-blue/15 backdrop-blur-xl">
          <div className="flex flex-col gap-3 md:flex-row md:flex-wrap lg:flex-nowrap lg:items-center">
            {/* Search Input */}
            <div className="relative flex h-14 w-full md:w-full lg:w-auto lg:flex-1 items-center rounded-xl border border-gray-200 bg-gray-50/50 transition-all focus-within:border-blue/30 focus-within:bg-white focus-within:ring-4 focus-within:ring-blue/10">
              <div className="pointer-events-none ml-2 flex h-10 w-10 shrink-0 items-center justify-center rounded-[10px] bg-blue/10 text-blue shadow-sm shadow-blue/20">
                <Search size={18} strokeWidth={2} />
              </div>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                placeholder="Search make, model, or keyword..."
                className="h-full w-full bg-transparent px-3 text-[15px] outline-none text-gray-800 placeholder:text-gray-500"
              />
            </div>
            
            {/* Body Type Select */}
            <select
              value={bodyType}
              onChange={(e) => setBodyType(e.target.value)}
              className="h-14 w-full md:flex-1 lg:w-44 xl:w-48 lg:flex-none appearance-none rounded-xl border border-gray-200 bg-gray-50/50 px-4 pr-8 text-[15px] text-gray-700 outline-none transition-all focus:border-blue/30 focus:bg-white focus:ring-4 focus:ring-blue/10 cursor-pointer"
            >
              <option value="">All Body Types</option>
              <option value="sedan">Sedan</option>
              <option value="suv">SUV</option>
              <option value="hatchback">Hatchback</option>
              <option value="truck">Truck</option>
              <option value="van">Van</option>
              <option value="coupe">Coupe</option>
            </select>

            {/* Price Range Select */}
            <select
              value={priceRange}
              onChange={(e) => setPriceRange(e.target.value)}
              className="h-14 w-full md:flex-1 lg:w-44 xl:w-48 lg:flex-none appearance-none rounded-xl border border-gray-200 bg-gray-50/50 px-4 pr-8 text-[15px] text-gray-700 outline-none transition-all focus:border-blue/30 focus:bg-white focus:ring-4 focus:ring-blue/10 cursor-pointer"
            >
              <option value="">Any Price</option>
              <option value="10000">Under $10K</option>
              <option value="25000">Under $25K</option>
              <option value="50000">Under $50K</option>
              <option value="100000">Under $100K</option>
            </select>

            {/* Submit Button */}
            <Button 
              className="h-14 w-full md:flex-1 lg:w-36 xl:w-40 lg:flex-none rounded-xl bg-blue text-white flex items-center justify-center text-[15px] font-semibold shadow-lg shadow-blue/25" 
              onClick={handleSearch}
            >
              Search Cars
            </Button>
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
