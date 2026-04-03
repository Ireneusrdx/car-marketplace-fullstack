import { ArrowRight, CheckCircle, ChevronDown } from "lucide-react";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";

export default function HeroSection() {
  return (
    <section className="relative min-h-[100vh] overflow-hidden bg-black text-white">
      <img
        src="https://images.unsplash.com/photo-1493238792000-8113da705763?auto=format&fit=crop&w=1920&q=80"
        alt="Premium sports car"
        className="absolute inset-0 h-full w-full object-cover brightness-[0.35]"
      />
      <div className="absolute inset-0 bg-gradient-to-b from-black/60 to-transparent" />
      <div className="absolute inset-0 bg-gradient-to-r from-black via-black/70 to-transparent" />
      <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom_right,rgba(0,87,255,0.2),transparent_55%)]" />

      <div className="relative mx-auto flex min-h-[100vh] w-full max-w-content items-center px-6 pb-28 pt-32">
        <div className="max-w-2xl">
          <p className="inline-flex items-center rounded-full border border-blue/30 bg-blue/20 px-4 py-2 text-xs font-semibold tracking-[0.16em] text-blue">
            ● PREMIUM CAR MARKETPLACE
          </p>
          <h1 className="mt-6 font-montserrat text-5xl font-black leading-[0.96] tracking-tight md:text-7xl">
            FIND YOUR
            <br />
            DREAM
            <br />
            <span className="text-blue">MACHINE</span>
          </h1>
          <p className="mt-6 max-w-md text-lg text-gray-400">
            Discover 50,000+ verified vehicles from trusted sellers across the country.
          </p>

          <div className="mt-8 flex flex-wrap items-center gap-3">
            <Button as={Link} to="/listings" size="lg">
              BROWSE CARS
              <ArrowRight size={16} strokeWidth={1.5} />
            </Button>
            <Button
              variant="ghost"
              size="lg"
              className="border-white/30 text-white hover:border-white hover:bg-white hover:text-gray-900"
              onClick={() => document.getElementById("featured-listings")?.scrollIntoView({ behavior: "smooth" })}
            >
              HOW IT WORKS
            </Button>
          </div>

          <div className="mt-8 flex flex-wrap items-center gap-4 text-sm text-white/70">
            {[
              "50K+ Listings",
              "Verified Sellers",
              "Secure Payments"
            ].map((item) => (
              <span key={item} className="inline-flex items-center gap-1.5">
                <CheckCircle size={14} strokeWidth={1.5} className="text-blue" />
                {item}
              </span>
            ))}
          </div>
        </div>
      </div>

      <div className="absolute bottom-8 left-1/2 flex -translate-x-1/2 items-center gap-2 text-sm text-white/50">
        <ChevronDown size={16} strokeWidth={1.5} className="animate-bounce" />
        Scroll to explore
      </div>
    </section>
  );
}

