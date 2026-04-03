import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchHomeMakes } from "@/lib/homeService";

export default function BrowseMakeSection() {
  const [brandItems, setBrandItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadMakes() {
      try {
        const remote = await fetchHomeMakes(8);
        if (isMounted) setBrandItems(remote);
      } catch {
        if (isMounted) setBrandItems([]);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadMakes();
    return () => { isMounted = false; };
  }, []);

  if (!loading && !brandItems.length) return null;

  return (
    <section className="bg-[var(--white-soft)] px-6 py-24">
      <div className="mx-auto w-full max-w-content text-center">
        <h2 className="font-montserrat text-4xl font-bold text-gray-900">BROWSE BY BRAND</h2>
        <p className="mt-2 text-gray-500">Find top listings from your favorite automotive brands.</p>

        <div className="mx-auto mt-10 grid max-w-5xl grid-cols-2 gap-4 sm:grid-cols-4 lg:grid-cols-8">
          {(loading ? Array.from({ length: 8 }, (_, i) => ({ id: i, name: "...", count: "" })) : brandItems).map((make) => (
            <Link
              key={make.id || make.name}
              to={`/listings?make=${make.name}`}
              className="group cursor-pointer rounded-2xl border border-gray-100 bg-white p-5 transition-all duration-300 hover:border-blue/30 hover:shadow-blue-md"
            >
              <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-gray-50 font-montserrat text-sm font-bold text-gray-500 transition-colors group-hover:text-blue">
                {make.name.slice(0, 2).toUpperCase()}
              </div>
              <p className="mt-3 text-sm font-medium text-gray-600 group-hover:text-blue">{make.name}</p>
              <p className="mt-1 text-xs text-gray-400">{make.count}</p>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}
