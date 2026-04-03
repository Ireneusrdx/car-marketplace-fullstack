import { BadgeCheck, Crown, Shield } from "lucide-react";
import { cn } from "@/lib/cn";

const variants = {
  verified: {
    className: "border border-blue/20 bg-blue/10 text-blue",
    icon: BadgeCheck
  },
  new: {
    className: "bg-blue text-white",
    icon: null
  },
  featured: {
    className: "bg-gradient-to-r from-amber-400 to-orange-400 text-white",
    icon: Crown
  },
  certified: {
    className: "border border-emerald-200 bg-emerald-50 text-emerald-700",
    icon: Shield
  },
  used: {
    className: "bg-gray-100 text-gray-600",
    icon: null
  }
};

export default function Badge({ variant = "verified", children, className }) {
  const current = variants[variant] || variants.verified;
  const Icon = current.icon;

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold",
        current.className,
        className
      )}
    >
      {Icon ? <Icon size={12} strokeWidth={1.5} /> : null}
      {children}
    </span>
  );
}

