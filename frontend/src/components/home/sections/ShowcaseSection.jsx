import { ArrowRight, Gauge, Sparkles, Zap } from "lucide-react";
import { useState } from "react";
import Button from "@/components/global/Button";

export default function ShowcaseSection() {
  const [rotation, setRotation] = useState({ x: 0, y: 0 });

  const onMove = (event) => {
    const rect = event.currentTarget.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;
    setRotation({ x: y * -12, y: x * 12 });
  };

  return (
    <section className="relative overflow-hidden bg-black px-6 py-24 text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_50%,rgba(0,87,255,0.16),transparent_45%)]" />
      <div className="relative mx-auto grid w-full max-w-content items-center gap-12 lg:grid-cols-5">
        <div className="lg:col-span-2">
          <p className="inline-flex rounded-full border border-blue/30 bg-blue/20 px-4 py-1 text-xs font-semibold tracking-[0.14em] text-blue">
            EXPLORE IN 3D
          </p>
          <h2 className="mt-4 font-montserrat text-4xl font-extrabold md:text-5xl">EVERY ANGLE. EVERY DETAIL.</h2>
          <p className="mt-4 text-gray-400">
            Explore featured listings with immersive 360 visuals and cinematic presentation.
          </p>
          <div className="mt-6 flex flex-wrap gap-2">
            {[
              "360 View",
              "HD Photos",
              "Interior Tour"
            ].map((tag) => (
              <span key={tag} className="rounded-full border border-white/15 bg-white/5 px-4 py-2 text-sm text-gray-300">
                {tag}
              </span>
            ))}
          </div>
          <Button className="mt-6">
            Explore Showcase
            <ArrowRight size={16} strokeWidth={1.5} />
          </Button>
        </div>

        <div
          className="relative lg:col-span-3"
          onMouseMove={onMove}
          onMouseLeave={() => setRotation({ x: 0, y: 0 })}
        >
          <div
            className="relative mx-auto w-full max-w-3xl transition-transform duration-100"
            style={{ transform: `perspective(1000px) rotateX(${rotation.x}deg) rotateY(${rotation.y}deg)` }}
          >
            <img
              src="https://images.unsplash.com/photo-1611821064430-0d40291d0f0b?auto=format&fit=crop&w=1600&q=80"
              alt="3D showcase vehicle"
              className="w-full rounded-3xl shadow-car"
            />
            <div className="absolute -bottom-6 left-1/2 h-16 w-3/4 -translate-x-1/2 rounded-full bg-blue/25 blur-3xl" />

            <div className="absolute left-6 top-6 rounded-2xl border border-white/15 bg-black-800/90 px-4 py-3 shadow-xl backdrop-blur-md transition-transform hover:scale-105">
              <p className="text-xs text-gray-400">Power</p>
              <p className="mt-1 inline-flex items-center gap-1 text-sm font-semibold">
                <Zap size={14} strokeWidth={1.5} className="text-blue" />523 BHP
              </p>
            </div>
            <div className="absolute right-6 top-6 rounded-2xl border border-white/15 bg-black-800/90 px-4 py-3 shadow-xl backdrop-blur-md transition-transform hover:scale-105">
              <p className="text-xs text-gray-400">Engine</p>
              <p className="mt-1 inline-flex items-center gap-1 text-sm font-semibold">
                <Sparkles size={14} strokeWidth={1.5} className="text-blue" />V8 4.4L
              </p>
            </div>
            <div className="absolute bottom-6 left-6 rounded-2xl border border-white/15 bg-black-800/90 px-4 py-3 shadow-xl backdrop-blur-md transition-transform hover:scale-105">
              <p className="text-xs text-gray-400">Acceleration</p>
              <p className="mt-1 inline-flex items-center gap-1 text-sm font-semibold">
                <Gauge size={14} strokeWidth={1.5} className="text-blue" />0-60 in 3.8s
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

