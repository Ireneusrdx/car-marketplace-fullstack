import { cn } from "@/lib/cn";

const variants = {
  primary:
    "bg-blue text-white shadow-blue-md hover:bg-blue-light hover:shadow-blue-lg active:bg-blue-dark",
  secondary:
    "border border-gray-200 bg-white/80 text-gray-900 shadow-blue-sm hover:border-blue/30 hover:bg-blue-ultra hover:shadow-blue-md",
  ghost: "border-2 border-blue bg-transparent text-blue hover:bg-blue hover:text-white",
  icon: "h-11 w-11 rounded-full border border-gray-100 bg-white text-gray-700 shadow-blue-sm hover:border-blue/30 hover:shadow-blue-md"
};

const sizes = {
  md: "px-8 py-3.5 text-sm",
  sm: "px-5 py-2 text-sm",
  lg: "px-10 py-5 text-base",
  icon: "p-0"
};

export default function Button({
  as: Component = "button",
  type = "button",
  variant = "primary",
  size = "md",
  className,
  children,
  ...props
}) {
  const isIcon = variant === "icon" || size === "icon";

  return (
    <Component
      type={Component === "button" ? type : undefined}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-full font-semibold transition-all duration-300 hover:-translate-y-0.5 active:translate-y-0 disabled:pointer-events-none disabled:opacity-60",
        variants[variant],
        isIcon ? sizes.icon : sizes[size],
        className
      )}
      {...props}
    >
      {children}
    </Component>
  );
}

