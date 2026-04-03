import { LayoutGrid, List } from "lucide-react";
import Button from "@/components/global/Button";

export default function ListingsSortViewBar({ count, sortBy, onSort, viewMode, onViewMode }) {
  return (
    <div className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-gray-100 bg-white px-5 py-4 shadow-blue-sm">
      <p className="text-sm font-medium text-gray-600">
        {count} vehicle{count === 1 ? "" : "s"} found
      </p>
      <div className="flex items-center gap-2">
        <select
          value={sortBy}
          onChange={(event) => onSort(event.target.value)}
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm text-gray-700 outline-none focus:border-blue"
        >
          <option value="recent">Most Recent</option>
          <option value="priceAsc">Price: Low to High</option>
          <option value="priceDesc">Price: High to Low</option>
          <option value="mileageAsc">Mileage: Low to High</option>
        </select>
        <Button
          variant="icon"
          className={viewMode === "grid" ? "border-blue/30 text-blue" : ""}
          onClick={() => onViewMode("grid")}
        >
          <LayoutGrid size={16} strokeWidth={1.5} />
        </Button>
        <Button
          variant="icon"
          className={viewMode === "list" ? "border-blue/30 text-blue" : ""}
          onClick={() => onViewMode("list")}
        >
          <List size={16} strokeWidth={1.5} />
        </Button>
      </div>
    </div>
  );
}

