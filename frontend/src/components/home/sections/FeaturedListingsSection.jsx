import { RotateCcw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";
import CarCard from "@/components/home/CarCard";
import Skeleton from "@/components/global/Skeleton";
import { fetchFeaturedListings, fetchRecentListings } from "@/lib/listingsService";

const tabs = ["All", "Sedan", "SUV", "Electric", "Under $20K"];

export default function FeaturedListingsSection() {
  const [activeTab, setActiveTab] = useState("All");
  const [featured, setFeatured] = useState([]);
  const [recent, setRecent] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadHomeListings() {
      setLoading(true);
      try {
        const [featuredData, recentData] = await Promise.all([
          fetchFeaturedListings(6),
          fetchRecentListings(6)
        ]);
        if (!isMounted) return;
        setFeatured(featuredData);
        setRecent(recentData);
      } catch {
        if (!isMounted) return;
        setFeatured([]);
        setRecent([]);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadHomeListings();
    return () => { isMounted = false; };
  }, []);

  const filtered = useMemo(() => {
    switch (activeTab) {
      case "Sedan":
        return featured.filter((car) => car.bodyType === "sedan");
      case "SUV":
        return featured.filter((car) => car.bodyType === "suv");
      case "Electric":
        return featured.filter((car) => String(car.fuel).toLowerCase() === "electric");
      case "Under $20K":
        return featured.filter((car) => Number(car.priceValue) <= 20000);
      case "All":
      default:
        return featured;
    }
  }, [featured, activeTab]);

  const visibleCars = filtered.length ? filtered : featured;

  const handleLoadMore = () => {
    if (!recent.length) return;
    setFeatured((prev) => {
      const merged = [...prev];
      recent.forEach((item) => {
        if (!merged.find((car) => car.id === item.id)) {
          merged.push(item);
        }
      });
      return merged;
    });
  };

  return (
    <section id="featured-listings" className="bg-white px-6 py-24">
      <div className="mx-auto w-full max-w-content">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h2 className="font-montserrat text-4xl font-bold text-gray-900">FEATURED VEHICLES</h2>
            <p className="mt-2 text-gray-500">Handpicked listings from verified sellers.</p>
          </div>
          <Link to="/listings" className="text-sm font-semibold text-blue hover:underline">View All Cars →</Link>
        </div>

        <div className="mt-8 flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={activeTab === tab ? "rounded-full bg-blue px-4 py-2 text-sm font-semibold text-white" : "rounded-full bg-gray-100 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-200"}
            >
              {tab}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {[1, 2, 3].map((i) => <Skeleton key={i} className="h-72 rounded-3xl" />)}
          </div>
        ) : visibleCars.length > 0 ? (
          <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {visibleCars.map((car) => (
              <CarCard key={car.id} car={car} />
            ))}
          </div>
        ) : (
          <div className="mt-8 rounded-3xl border border-dashed border-gray-200 bg-gray-50 p-12 text-center">
            <p className="text-lg font-semibold text-gray-900">No featured vehicles available</p>
            <p className="mt-2 text-gray-500">Check back soon or browse all listings.</p>
            <Button as={Link} to="/listings" className="mt-4">Browse All Listings</Button>
          </div>
        )}

        {!loading && recent.length > 0 && (
          <div className="mt-10 text-center">
            <Button variant="ghost" onClick={handleLoadMore}>
              <RotateCcw size={16} strokeWidth={1.5} />
              Load More
            </Button>
          </div>
        )}
      </div>
    </section>
  );
}
