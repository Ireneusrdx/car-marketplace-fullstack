import { CheckCircle } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import Button from "@/components/global/Button";
import api from "@/lib/api";

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0
  }).format(value);
}

export default function EmiTeaserSection() {
  const [price, setPrice] = useState(50000);
  const [downPaymentPct, setDownPaymentPct] = useState(20);
  const [months, setMonths] = useState(60);
  const [result, setResult] = useState({ emi: 0, totalAmount: 0, totalInterest: 0 });

  const calculate = useCallback(async () => {
    const downPayment = price * (downPaymentPct / 100);
    try {
      const res = await api.post("/calculator/emi", {
        price,
        downPayment,
        interestRate: 8.0,
        tenureMonths: months
      });
      setResult({
        emi: Math.round(Number(res.data.emi || 0)),
        totalAmount: Math.round(Number(res.data.totalAmount || 0)),
        totalInterest: Math.round(Number(res.data.totalInterest || 0))
      });
    } catch {
      // Fallback: local calculation if API is down
      const principal = price - downPayment;
      const monthlyRate = 0.08 / 12;
      const monthlyEmi =
        (principal * monthlyRate * (1 + monthlyRate) ** months) /
        ((1 + monthlyRate) ** months - 1);
      const amount = monthlyEmi * months;
      setResult({
        emi: Math.round(monthlyEmi),
        totalAmount: Math.round(amount),
        totalInterest: Math.round(amount - principal)
      });
    }
  }, [price, downPaymentPct, months]);

  useEffect(() => {
    const timer = setTimeout(calculate, 300);
    return () => clearTimeout(timer);
  }, [calculate]);

  return (
    <section className="bg-[var(--white-soft)] px-6 py-24">
      <div className="mx-auto grid w-full max-w-content items-center gap-10 lg:grid-cols-2">
        <div>
          <h2 className="font-montserrat text-5xl font-extrabold text-gray-900">OWN IT TODAY</h2>
          <p className="mt-4 max-w-lg text-gray-500">
            Estimate your monthly payment instantly and choose the plan that fits your lifestyle.
          </p>

          <div className="mt-6 space-y-3">
            {[
              "Flexible loan tenures up to 84 months",
              "Interest rates from 6.9% per annum",
              "Instant approval in 2 minutes"
            ].map((item) => (
              <p key={item} className="inline-flex items-center gap-2 text-gray-600">
                <CheckCircle size={16} strokeWidth={1.5} className="text-blue" />
                {item}
              </p>
            ))}
          </div>

          <Button className="mt-8" onClick={calculate}>CALCULATE MY EMI</Button>
        </div>

        <div className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-xl">
          <label className="block text-sm font-medium text-gray-600">Car Price: {formatCurrency(price)}</label>
          <input
            type="range" min={5000} max={200000} step={1000}
            value={price}
            onChange={(e) => setPrice(Number(e.target.value))}
            className="mt-2 w-full accent-blue"
          />

          <label className="mt-4 block text-sm font-medium text-gray-600">Down Payment: {downPaymentPct}%</label>
          <input
            type="range" min={0} max={50}
            value={downPaymentPct}
            onChange={(e) => setDownPaymentPct(Number(e.target.value))}
            className="mt-2 w-full accent-blue"
          />

          <label className="mt-4 block text-sm font-medium text-gray-600">Tenure: {months} months</label>
          <input
            type="range" min={12} max={84} step={12}
            value={months}
            onChange={(e) => setMonths(Number(e.target.value))}
            className="mt-2 w-full accent-blue"
          />

          <div className="mt-6 rounded-2xl bg-blue/5 p-4">
            <p className="text-sm text-gray-500">Monthly EMI</p>
            <p className="font-mono text-5xl font-extrabold text-blue">{formatCurrency(result.emi)}</p>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
            <div className="rounded-xl bg-gray-50 p-3">
              <p className="text-gray-500">Total Amount</p>
              <p className="mt-1 font-mono font-semibold text-gray-700">{formatCurrency(result.totalAmount)}</p>
            </div>
            <div className="rounded-xl bg-gray-50 p-3">
              <p className="text-gray-500">Total Interest</p>
              <p className="mt-1 font-mono font-semibold text-gray-700">{formatCurrency(result.totalInterest)}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
