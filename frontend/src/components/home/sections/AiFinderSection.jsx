import { ArrowRight, BrainCircuit, Fuel, MapPin, Wallet } from "lucide-react";
import Button from "@/components/global/Button";

const chips = [
  { icon: Wallet, label: "Budget Range" },
  { icon: Fuel, label: "Fuel Type" },
  { icon: BrainCircuit, label: "Priorities" },
  { icon: MapPin, label: "Location" }
];

export default function AiFinderSection() {
  return (
    <section className="relative overflow-hidden bg-gradient-to-br from-black to-[#0D1B3E] px-6 py-24 text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle,rgba(0,87,255,0.16),transparent_55%)]" />
      <div className="relative mx-auto w-full max-w-3xl text-center">
        <p className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-black/40 px-4 py-2 text-xs font-semibold tracking-[0.16em] text-blue">
          <BrainCircuit size={14} strokeWidth={1.5} /> POWERED BY AI
        </p>
        <h2 className="mt-6 font-montserrat text-5xl font-black leading-tight md:text-6xl">
          LET AI FIND YOUR <span className="text-blue">PERFECT MATCH</span>
        </h2>
        <p className="mx-auto mt-5 max-w-2xl text-lg text-gray-400">
          Answer 5 quick questions and get tailored recommendations from 50,000+ listings in seconds.
        </p>

        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          {chips.map(({ icon: Icon, label }) => (
            <span
              key={label}
              className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-4 py-2 text-sm text-gray-200"
            >
              <Icon size={14} strokeWidth={1.5} className="text-blue" />
              {label}
            </span>
          ))}
        </div>

        <Button size="lg" className="mt-8">
          START AI FINDER
          <ArrowRight size={16} strokeWidth={1.5} />
        </Button>
      </div>
    </section>
  );
}

