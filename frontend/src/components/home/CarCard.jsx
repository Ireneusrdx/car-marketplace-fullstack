import {
  ArrowRight,
  BadgeCheck,
  Check,
  Clock,
  Cog,
  Fuel,
  Gauge,
  Heart,
  Image,
  MapPin
} from "lucide-react";
import { Link } from "react-router-dom";
import Badge from "@/components/global/Badge";
import Button from "@/components/global/Button";
import { cn } from "@/lib/cn";

export default function CarCard({ car, viewMode = "grid", compareSelected = false, onToggleCompare }) {
  const isList = viewMode === "list";

  return (
    <article
      className={cn(
        "group overflow-hidden rounded-3xl border border-gray-100 bg-white shadow-blue-sm transition-all duration-500 hover:shadow-blue-xl",
        isList ? "flex flex-col md:flex-row" : "hover:-translate-y-2"
      )}
    >
      <div className={cn("relative overflow-hidden", isList ? "md:w-[340px] md:shrink-0" : "aspect-[16/10]")}>
        <img src={car.image} alt={car.title} className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105" />

        <div className="absolute left-4 top-4 flex gap-2">
          {car.featured ? <Badge variant="featured">Featured</Badge> : null}
          {car.isNew ? <Badge variant="new">New</Badge> : null}
          {car.certified ? <Badge variant="certified">Certified</Badge> : null}
        </div>

        <div className="absolute right-4 top-4">
          <Button variant="icon" className="glass-white">
            <Heart size={16} strokeWidth={1.5} />
          </Button>
        </div>

        <div className="absolute bottom-4 right-4 inline-flex items-center gap-1 rounded-full bg-black/60 px-3 py-1 text-xs font-medium text-white">
          <Image size={12} strokeWidth={1.5} />
          {car.photos} Photos
        </div>
      </div>

      <div className="flex-1 p-5">
        <div className="flex items-center justify-between gap-2">
          <h3 className="text-lg font-semibold text-gray-900">{car.title}</h3>
          <span className="rounded-md bg-gray-100 px-2 py-0.5 font-mono text-xs text-gray-600">{car.year}</span>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-4 font-mono text-[13px] text-gray-500">
          <span className="inline-flex items-center gap-1.5">
            <Gauge size={14} strokeWidth={1.5} className="text-blue/70" />
            {car.mileage}
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Fuel size={14} strokeWidth={1.5} className="text-blue/70" />
            {car.fuel}
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Cog size={14} strokeWidth={1.5} className="text-blue/70" />
            {car.transmission}
          </span>
        </div>

        <div className="mt-3 flex items-center gap-3 text-xs text-gray-400">
          <span className="inline-flex items-center gap-1">
            <MapPin size={12} strokeWidth={1.5} />
            {car.location}
          </span>
          <span className="h-1 w-1 rounded-full bg-gray-300" />
          <span className="inline-flex items-center gap-1">
            <Clock size={12} strokeWidth={1.5} />
            {car.listedAgo}
          </span>
        </div>

        <div className="my-4 border-t border-gray-100" />

        <div className="flex items-end justify-between">
          <div>
            <p className="font-mono text-2xl font-bold text-gray-900">{car.price}</p>
            <p className="text-sm text-gray-400 line-through">{car.originalPrice}</p>
          </div>
          <Link
            to={`/cars/${car.slug || car.id}`}
            className="inline-flex items-center gap-1 text-sm font-semibold text-blue transition-all group-hover:translate-x-1"
          >
            View Details
            <ArrowRight size={14} strokeWidth={1.5} />
          </Link>
        </div>

        <div className="mt-3 flex items-center gap-1.5 text-sm text-gray-500">
          <span>{car.seller}</span>
          {car.verifiedSeller ? <BadgeCheck size={14} strokeWidth={1.5} className="text-blue" /> : null}
        </div>

        {onToggleCompare ? (
          <button
            onClick={() => onToggleCompare(car)}
            className={cn(
              "mt-4 inline-flex items-center gap-1 rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors",
              compareSelected
                ? "border-blue bg-blue/10 text-blue"
                : "border-gray-200 bg-gray-50 text-gray-600 hover:border-blue/30 hover:text-blue"
            )}
          >
            {compareSelected ? <Check size={12} strokeWidth={1.5} /> : null}
            {compareSelected ? "Added to Compare" : "Add to Compare"}
          </button>
        ) : null}
      </div>
    </article>
  );
}



