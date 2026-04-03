/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        blue: {
          DEFAULT: "#0057FF",
          light: "#3D7FFF",
          dark: "#0040CC",
          glow: "rgba(0,87,255,0.15)",
          ultra: "rgba(0,87,255,0.06)"
        },
        gray: {
          50: "#F4F6FA",
          100: "#E8ECF4",
          200: "#D0D6E8",
          400: "#8B96B0",
          500: "#6B7A99",
          600: "#4A5568",
          900: "#0D1117"
        },
        black: {
          DEFAULT: "#080C14",
          700: "#161C2D",
          800: "#0F1420"
        },
        success: "#00C853",
        warning: "#FFB300",
        error: "#FF3D57"
      },
      fontFamily: {
        montserrat: ["Montserrat", "sans-serif"],
        inter: ["Inter", "sans-serif"],
        mono: ["JetBrains Mono", "monospace"],
        playfair: ["Playfair Display", "serif"]
      },
      boxShadow: {
        "blue-sm": "0 1px 3px rgba(0,87,255,0.08), 0 1px 2px rgba(0,0,0,0.06)",
        "blue-md": "0 4px 24px rgba(0,87,255,0.10), 0 2px 8px rgba(0,0,0,0.08)",
        "blue-lg": "0 20px 60px rgba(0,87,255,0.15), 0 8px 24px rgba(0,0,0,0.10)",
        "blue-xl": "0 32px 80px rgba(0,87,255,0.20), 0 16px 40px rgba(0,0,0,0.12)",
        car: "0 40px 100px rgba(0,87,255,0.25)"
      },
      backdropBlur: {
        xs: "4px"
      },
      animation: {
        float: "float 6s ease-in-out infinite",
        "glow-pulse": "glow-pulse 3s ease-in-out infinite",
        shimmer: "shimmer 1.5s infinite",
        "count-up": "count-up 2s ease-out forwards",
        "slide-up": "slide-up 0.4s ease-out",
        "bounce-in": "bounce-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)"
      },
      keyframes: {
        float: {
          "0%, 100%": { transform: "translateY(0px)" },
          "50%": { transform: "translateY(-12px)" }
        },
        "glow-pulse": {
          "0%, 100%": { opacity: "0.4" },
          "50%": { opacity: "0.8" }
        },
        shimmer: {
          "0%": { backgroundPosition: "-200% 0" },
          "100%": { backgroundPosition: "200% 0" }
        },
        "count-up": {
          "0%": { opacity: "0.2", transform: "translateY(8px)" },
          "100%": { opacity: "1", transform: "translateY(0)" }
        },
        "slide-up": {
          "0%": { opacity: "0", transform: "translateY(20px)" },
          "100%": { opacity: "1", transform: "translateY(0)" }
        },
        "bounce-in": {
          "0%": { opacity: "0", transform: "scale(0.3)" },
          "50%": { opacity: "1" },
          "100%": { transform: "scale(1)" }
        }
      },
      transitionTimingFunction: {
        smooth: "cubic-bezier(0.25, 0.1, 0.25, 1)"
      },
      maxWidth: {
        content: "1280px"
      },
      borderRadius: {
        card: "1rem"
      },
      backgroundImage: {
        chrome: "linear-gradient(135deg, #E8ECF4 0%, #FFFFFF 50%, #D0D6E8 100%)"
      }
    }
  },
  plugins: []
};
