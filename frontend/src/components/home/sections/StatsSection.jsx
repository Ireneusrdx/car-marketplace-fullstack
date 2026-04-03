import { BadgeCheck, Car, Star, UserCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { fetchHomeStats } from "@/lib/homeService";

const defaultStats = [
  { value: "—", label: "Cars Listed" },
  { value: "—", label: "Happy Buyers" },
  { value: "—", label: "Verified Sellers" },
  { value: "—", label: "Average Rating" }
];

const icons = [Car, UserCheck, BadgeCheck, Star];

export default function StatsSection() {
  const [statItems, setStatItems] = useState(defaultStats);

  useEffect(() => {
    let isMounted = true;

    async function loadStats() {
      try {
        const remote = await fetchHomeStats();
        if (!isMounted) return;
        setStatItems([
          { value: remote.totalListingsLabel || "—", label: "Cars Listed" },
          { value: "12,000+", label: "Happy Buyers" },
          { value: "8,500+", label: "Verified Sellers" },
          { value: "4.9", label: "Average Rating" }
        ]);
      } catch {
        // Keep defaults
      }
    }

    loadStats();
    return () => { isMounted = false; };
  }, []);

  return (
    <section className="bg-white px-6 py-16">
      <div className="mx-auto grid w-full max-w-content gap-8 md:grid-cols-4">
        {statItems.map((item, index) => {
          const Icon = icons[index];
          return (
            <div key={item.label} className="text-center md:border-r md:border-gray-100 md:last:border-r-0">
              <Icon className="mx-auto mb-3 text-blue" size={28} strokeWidth={1.5} />
              <p className="font-montserrat text-4xl font-extrabold text-blue">{item.value}</p>
              <p className="mt-2 text-xs font-medium uppercase tracking-[0.16em] text-gray-500">{item.label}</p>
            </div>
          );
        })}
      </div>
    </section>
  );
}
