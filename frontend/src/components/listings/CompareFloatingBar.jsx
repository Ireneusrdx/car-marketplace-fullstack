import { GitCompare, X } from "lucide-react";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";

export default function CompareFloatingBar({ selectedCars, onRemove, onClear }) {
  if (selectedCars.length < 2) {
    return null;
  }

  return (
    <div className="fixed bottom-6 left-1/2 z-40 w-[min(95%,920px)] -translate-x-1/2 rounded-2xl border border-gray-200 bg-white/90 px-4 py-3 shadow-blue-xl backdrop-blur-xl">
      <div className="flex flex-wrap items-center gap-3">
        <p className="text-sm font-semibold text-gray-900">Compare ({selectedCars.length}/3)</p>
        <div className="flex flex-1 flex-wrap gap-2">
          {selectedCars.map((car) => (
            <span key={car.id} className="inline-flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
              {car.title}
              <button onClick={() => onRemove(car.id)} className="text-gray-500 hover:text-gray-700">
                <X size={12} strokeWidth={1.5} />
              </button>
            </span>
          ))}
        </div>
        <Button as={Link} to="/compare" size="sm">
          <GitCompare size={14} strokeWidth={1.5} />
          Compare Now
        </Button>
        <button onClick={onClear} className="text-sm font-semibold text-blue">
          Clear
        </button>
      </div>
    </div>
  );
}

