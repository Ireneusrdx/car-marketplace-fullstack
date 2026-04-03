import { AnimatePresence, motion } from "framer-motion";
import { ArrowRight, BrainCircuit, Car, Fuel, MapPin, Wallet } from "lucide-react";
import { useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";
import PageTransition from "@/components/global/PageTransition";
import { aiRecommend } from "@/lib/listingsService";

const usageOptions = [
  { value: "daily_commute", label: "Daily Commute", bodyType: "sedan" },
  { value: "family", label: "Family Car", bodyType: "suv" },
  { value: "performance", label: "Performance", bodyType: "coupe" },
  { value: "cargo", label: "Work & Cargo", bodyType: "truck" }
];

const fuelOptions = ["Petrol", "Diesel", "Electric", "Hybrid", "Any"];
const priorities = ["value", "performance", "comfort", "low-mileage", "efficiency", "brand"];
const radiusOptions = ["25km", "50km", "100km", "anywhere"];

export default function AiFinderPage() {
  const [step, setStep] = useState(1);
  const [processing, setProcessing] = useState(false);
  const [results, setResults] = useState(null);
  const [error, setError] = useState("");
  const [answers, setAnswers] = useState({
    budget: 25000,
    usage: usageOptions[0],
    fuel: "Any",
    priorities: [],
    location: "",
    radius: "50km"
  });

  const progress = (step / 5) * 100;

  const onNext = async () => {
    if (step < 5) {
      setStep((prev) => prev + 1);
      return;
    }

    setProcessing(true);
    setError("");

    try {
      const budgetRange = buildBudgetRange(answers.budget);
      const payload = {
        budget: budgetRange,
        fuelType: answers.fuel === "Any" ? [] : [answers.fuel.toUpperCase()],
        bodyType: answers.usage.bodyType ? [answers.usage.bodyType.toUpperCase()] : [],
        usage: answers.usage.value,
        location: answers.location || null,
        priorities: answers.priorities
      };

      const data = await aiRecommend(payload);
      setResults(data);
    } catch {
      setError("AI service is currently unavailable. Try again shortly.");
    } finally {
      setProcessing(false);
    }
  };

  const togglePriority = (item) => {
    setAnswers((prev) => {
      const exists = prev.priorities.includes(item);
      if (exists) return { ...prev, priorities: prev.priorities.filter((v) => v !== item) };
      if (prev.priorities.length >= 3) return prev;
      return { ...prev, priorities: [...prev.priorities, item] };
    });
  };

  const resetAll = () => {
    setResults(null);
    setError("");
    setStep(1);
    setAnswers({ budget: 25000, usage: usageOptions[0], fuel: "Any", priorities: [], location: "", radius: "50km" });
  };

  if (processing) {
    return (
      <PageTransition>
        <Helmet>
          <title>AI Finder Processing | AutoVault</title>
          <meta name="description" content="Analyzing your preferences to find the best vehicle matches." />
        </Helmet>
        <main className="min-h-screen bg-gradient-to-br from-black to-[#0D1B3E] px-6 py-24 text-white">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mx-auto h-16 w-16 animate-pulse rounded-full bg-blue/20 p-4 text-blue">
              <BrainCircuit size={32} strokeWidth={1.5} />
            </div>
            <h1 className="mt-6 font-montserrat text-4xl font-black">Analyzing your preferences...</h1>
            <div className="mx-auto mt-6 h-2 w-full max-w-lg overflow-hidden rounded-full bg-white/10">
              <motion.div className="h-full bg-blue" initial={{ width: 0 }} animate={{ width: "100%" }} transition={{ duration: 1.2 }} />
            </div>
          </div>
        </main>
      </PageTransition>
    );
  }

  if (results) {
    const hasItems = results.items && results.items.length > 0;
    return (
      <PageTransition>
        <Helmet>
          <title>AI Finder Results | AutoVault</title>
          <meta name="description" content="Explore AI-ranked vehicle recommendations based on your preferences." />
        </Helmet>
        <main className="min-h-screen bg-[var(--white-soft)] px-6 py-16">
          <section className="mx-auto max-w-content">
            <h1 className="font-montserrat text-4xl font-black text-gray-900">YOUR TOP MATCHES</h1>
            {results.summary ? (
              <p className="mt-2 text-gray-500">{results.summary}</p>
            ) : (
              <p className="mt-2 text-gray-500">Based on budget, usage pattern, priorities, and location preferences.</p>
            )}

            {!hasItems ? (
              <div className="mt-8 rounded-3xl border border-dashed border-gray-200 bg-white p-12 text-center shadow-blue-sm">
                <p className="text-lg font-semibold text-gray-900">No strong matches found</p>
                <p className="mt-2 text-gray-500">Try relaxing your budget or preference constraints.</p>
              </div>
            ) : (
              <div className="mt-8 grid gap-5">
                {results.items.map((item) => (
                  <article key={item.id || item.slug} className="rounded-3xl border border-gray-100 bg-white p-5 shadow-blue-sm">
                    <div className="grid gap-4 md:grid-cols-[220px_1fr]">
                      <img
                        src={item.primaryImageUrl || "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80"}
                        alt={item.title}
                        className="h-40 w-full rounded-2xl object-cover"
                      />
                      <div>
                        <div className="flex flex-wrap items-start justify-between gap-3">
                          <div>
                            <h2 className="text-xl font-bold text-gray-900">{item.title}</h2>
                            <p className="mt-1 text-sm text-gray-500">{item.bodyType} • {item.fuelType}</p>
                          </div>
                          {item.score != null && (
                            <span className="rounded-full bg-blue px-3 py-1 text-sm font-bold text-white">
                              {item.score}% Match
                            </span>
                          )}
                        </div>
                        {item.reason && (
                          <p className="mt-3 text-sm italic text-gray-600">{item.reason}</p>
                        )}
                        <p className="mt-2 font-mono text-lg font-bold text-blue">
                          {item.price != null
                            ? new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(item.price)
                            : ""}
                        </p>
                        <div className="mt-4 flex flex-wrap items-center gap-2">
                          <Button as={Link} to={`/cars/${item.slug}`} size="sm">View Details</Button>
                        </div>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            )}

            <div className="mt-8 flex gap-3">
              <Button variant="secondary" onClick={resetAll}>Refine Search</Button>
              <Button variant="ghost" onClick={resetAll}>Start Over</Button>
            </div>
          </section>
        </main>
      </PageTransition>
    );
  }

  return (
    <PageTransition>
      <Helmet>
        <title>AI Car Finder | AutoVault</title>
        <meta name="description" content="Answer a few questions and let AI find the best car options for your needs." />
      </Helmet>
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-16">
        <section className="mx-auto max-w-3xl">
          <div className="text-center">
            <p className="inline-flex items-center gap-2 rounded-full border border-blue/20 bg-blue/5 px-4 py-1.5 text-xs font-semibold tracking-[0.14em] text-blue">
              <BrainCircuit size={14} strokeWidth={1.5} /> AI CAR FINDER
            </p>
            <h1 className="mt-5 font-montserrat text-5xl font-black text-gray-900">Find Your Perfect Match</h1>
            <p className="mt-2 text-gray-500">Step {step} of 5</p>
            <div className="mx-auto mt-4 h-2 w-full max-w-xl overflow-hidden rounded-full bg-gray-100">
              <div className="h-full bg-blue transition-all duration-300" style={{ width: `${progress}%` }} />
            </div>
          </div>

          {error && (
            <p className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">{error}</p>
          )}

          <AnimatePresence mode="wait">
            <motion.div
              key={step}
              initial={{ opacity: 0, x: 80 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -80 }}
              transition={{ duration: 0.28, ease: [0.32, 0.72, 0, 1] }}
              className="mt-8 rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-lg"
            >
              {step === 1 && (
                <div>
                  <p className="text-sm font-semibold text-blue">Budget</p>
                  <h2 className="mt-2 font-montserrat text-3xl font-bold text-gray-900">What is your budget?</h2>
                  <p className="mt-4 font-mono text-5xl font-black text-blue">${answers.budget.toLocaleString()}</p>
                  <input
                    type="range" min={5000} max={120000} step={1000}
                    value={answers.budget}
                    onChange={(e) => setAnswers((p) => ({ ...p, budget: Number(e.target.value) }))}
                    className="mt-6 w-full accent-blue"
                  />
                </div>
              )}

              {step === 2 && (
                <div>
                  <p className="text-sm font-semibold text-blue">Usage</p>
                  <h2 className="mt-2 font-montserrat text-3xl font-bold text-gray-900">How will you use the car?</h2>
                  <div className="mt-5 grid gap-3 sm:grid-cols-2">
                    {usageOptions.map((item) => (
                      <button
                        key={item.value}
                        onClick={() => setAnswers((p) => ({ ...p, usage: item }))}
                        className={answers.usage.value === item.value
                          ? "rounded-2xl border border-blue bg-blue/5 p-4 text-left"
                          : "rounded-2xl border border-gray-200 p-4 text-left hover:border-blue/30"}
                      >
                        <p className="font-semibold text-gray-900">{item.label}</p>
                        <p className="mt-1 text-xs capitalize text-gray-500">Best body type: {item.bodyType}</p>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {step === 3 && (
                <div>
                  <p className="text-sm font-semibold text-blue">Fuel Type</p>
                  <h2 className="mt-2 font-montserrat text-3xl font-bold text-gray-900">Which fuel do you prefer?</h2>
                  <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-5">
                    {fuelOptions.map((item) => (
                      <button
                        key={item}
                        onClick={() => setAnswers((p) => ({ ...p, fuel: item }))}
                        className={answers.fuel === item
                          ? "rounded-xl border border-blue bg-blue/5 px-3 py-3 text-sm font-semibold capitalize text-blue"
                          : "rounded-xl border border-gray-200 bg-gray-50 px-3 py-3 text-sm capitalize text-gray-600"}
                      >
                        {item}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {step === 4 && (
                <div>
                  <p className="text-sm font-semibold text-blue">Priorities</p>
                  <h2 className="mt-2 font-montserrat text-3xl font-bold text-gray-900">Select up to 3 priorities</h2>
                  <div className="mt-5 grid gap-3 sm:grid-cols-2">
                    {priorities.map((item) => {
                      const selected = answers.priorities.includes(item);
                      const disabled = !selected && answers.priorities.length >= 3;
                      return (
                        <button
                          key={item} disabled={disabled}
                          onClick={() => togglePriority(item)}
                          className={selected
                            ? "rounded-2xl border border-blue bg-blue/5 px-4 py-3 text-left text-sm font-semibold capitalize text-blue"
                            : "rounded-2xl border border-gray-200 px-4 py-3 text-left text-sm capitalize text-gray-600 disabled:opacity-40"}
                        >
                          {item.replace("-", " ")}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {step === 5 && (
                <div>
                  <p className="text-sm font-semibold text-blue">Location</p>
                  <h2 className="mt-2 font-montserrat text-3xl font-bold text-gray-900">Where should we search?</h2>
                  <label className="mt-5 block">
                    <span className="mb-2 inline-flex items-center gap-2 text-sm text-gray-500">
                      <MapPin size={14} strokeWidth={1.5} /> City (optional)
                    </span>
                    <input
                      value={answers.location}
                      onChange={(e) => setAnswers((p) => ({ ...p, location: e.target.value }))}
                      placeholder="e.g. New York"
                      className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue"
                    />
                  </label>
                  <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
                    {radiusOptions.map((item) => (
                      <button
                        key={item}
                        onClick={() => setAnswers((p) => ({ ...p, radius: item }))}
                        className={answers.radius === item
                          ? "rounded-xl border border-blue bg-blue/5 px-3 py-2 text-sm font-semibold text-blue"
                          : "rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-600"}
                      >
                        {item}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          </AnimatePresence>

          <div className="mt-6 flex items-center justify-between">
            <Button variant="ghost" onClick={() => setStep((p) => Math.max(1, p - 1))} disabled={step === 1}>
              Back
            </Button>
            <Button onClick={onNext}>
              {step < 5 ? "Next" : "Find Matches"}
              <ArrowRight size={16} strokeWidth={1.5} />
            </Button>
          </div>

          <div className="mt-10 grid gap-3 text-sm text-gray-500 sm:grid-cols-4">
            <span className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 shadow-blue-sm">
              <Wallet size={14} strokeWidth={1.5} className="text-blue" /> Budget
            </span>
            <span className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 shadow-blue-sm">
              <Car size={14} strokeWidth={1.5} className="text-blue" /> Usage
            </span>
            <span className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 shadow-blue-sm">
              <Fuel size={14} strokeWidth={1.5} className="text-blue" /> Fuel Type
            </span>
            <span className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 shadow-blue-sm">
              <MapPin size={14} strokeWidth={1.5} className="text-blue" /> Location
            </span>
          </div>
        </section>
      </main>
    </PageTransition>
  );
}

function buildBudgetRange(budget) {
  return { min: Math.max(0, budget - 5000), max: budget + 5000 };
}
