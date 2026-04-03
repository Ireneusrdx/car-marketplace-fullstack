import { cn } from "@/lib/cn";

export default function Input({
  icon: Icon,
  label,
  className,
  inputClassName,
  dark = false,
  ...props
}) {
  const wrapperClass = dark
    ? "border-white/10 bg-black-700 text-white"
    : "border-gray-200 bg-white text-gray-900";
  const placeholderClass = dark ? "placeholder:text-gray-400" : "placeholder:text-gray-400";

  return (
    <label className={cn("relative block", className)}>
      {label ? (
        <span className={cn("mb-2 block text-sm font-medium", dark ? "text-gray-100" : "text-gray-600")}>
          {label}
        </span>
      ) : null}
      {Icon ? <Icon className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} strokeWidth={1.5} /> : null}
      <input
        className={cn(
          "w-full rounded-xl border px-5 py-3.5 text-[15px] outline-none transition-all duration-200 focus:border-blue focus:ring-4 focus:ring-blue/10",
          wrapperClass,
          placeholderClass,
          Icon ? "pl-11" : "",
          inputClassName
        )}
        {...props}
      />
    </label>
  );
}

