import { useEffect, useState } from "react";
import { Bell, Heart, Menu, X } from "lucide-react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { cn } from "@/lib/cn";
import Button from "@/components/global/Button";
import { notify } from "@/components/global/ToastProvider";
import { logout } from "@/lib/authService";
import { useAuthStore } from "@/store/authStore";

const links = [
  { label: "Browse Cars", to: "/listings" },
  { label: "Sell Your Car", to: "/sell" },
  { label: "AI Finder", to: "/ai-finder" },
  { label: "Compare", to: "/compare" }
];

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const isHome = location.pathname === "/";
  const overlayMode = isHome && !scrolled;

  const handleLogout = async () => {
    await logout();
    clearAuth();
    setMobileOpen(false);
    notify.success("Logged out successfully.");
    navigate("/auth");
  };

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 80);
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={cn(
        "fixed inset-x-0 top-0 z-50 transition-all duration-300",
        scrolled || !isHome
          ? "glass-white border-b border-gray-100/80 shadow-blue-sm"
          : "border-b border-transparent bg-transparent"
      )}
    >
      <div className="mx-auto flex h-20 w-full max-w-content items-center justify-between px-6">
        <NavLink to="/" className="flex items-center text-xl font-black tracking-tight">
          <span className={cn("font-montserrat", overlayMode ? "text-white" : "text-gray-900")}>AUTO</span>
          <span className="mx-1 inline-block h-1.5 w-1.5 rounded-full bg-blue" />
          <span className="font-montserrat text-blue">VAULT</span>
        </NavLink>

        <nav className="hidden items-center gap-7 lg:flex">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                cn(
                  "text-[15px] font-medium transition-colors hover:text-blue",
                  overlayMode ? "text-white/80" : "text-gray-600",
                  isActive && "text-blue"
                )
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          <Button variant="icon" aria-label="Notifications" className={overlayMode ? "border-white/20 bg-white/10 text-white" : ""}>
            <Bell size={18} strokeWidth={1.5} />
          </Button>
          <Button variant="icon" aria-label="Saved cars" className={overlayMode ? "border-white/20 bg-white/10 text-white" : ""}>
            <Heart size={18} strokeWidth={1.5} />
          </Button>
          <span className={cn("mx-1 h-7 w-px", overlayMode ? "bg-white/20" : "bg-gray-200")} />
          {isAuthenticated ? (
            <>
              <Button as={NavLink} to="/dashboard" variant="secondary" size="sm" className={overlayMode ? "bg-white text-gray-900" : ""}>
                {user?.fullName ? user.fullName.split(" ")[0] : "Dashboard"}
              </Button>
              <Button variant="ghost" size="sm" onClick={handleLogout} className={overlayMode ? "border-white/60 text-white hover:border-white hover:bg-white hover:text-gray-900" : ""}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Button as={NavLink} to="/auth" variant="ghost" size="sm" className={overlayMode ? "border-white/60 text-white hover:border-white hover:bg-white hover:text-gray-900" : ""}>
                Sign In
              </Button>
              <Button as={NavLink} to="/sell" variant="primary" size="sm">
                Post Ad
              </Button>
            </>
          )}
        </div>

        <Button
          variant="icon"
          className="lg:hidden"
          onClick={() => setMobileOpen((prev) => !prev)}
          aria-label={mobileOpen ? "Close menu" : "Open menu"}
        >
          {mobileOpen ? <X size={20} strokeWidth={1.5} /> : <Menu size={20} strokeWidth={1.5} />}
        </Button>
      </div>

      {mobileOpen ? (
        <div className="glass-white border-t border-gray-100/80 lg:hidden">
          <div className="mx-auto flex w-full max-w-content flex-col gap-4 px-6 py-6">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className="text-base font-medium text-gray-700"
                onClick={() => setMobileOpen(false)}
              >
                {link.label}
              </NavLink>
            ))}
            <div className="mt-2 flex gap-3">
              {isAuthenticated ? (
                <>
                  <Button as={NavLink} to="/dashboard" variant="secondary" size="sm" className="flex-1" onClick={() => setMobileOpen(false)}>
                    Dashboard
                  </Button>
                  <Button variant="ghost" size="sm" className="flex-1" onClick={handleLogout}>
                    Logout
                  </Button>
                </>
              ) : (
                <>
                  <Button as={NavLink} to="/auth" variant="ghost" size="sm" className="flex-1" onClick={() => setMobileOpen(false)}>
                    Sign In
                  </Button>
                  <Button as={NavLink} to="/sell" variant="primary" size="sm" className="flex-1" onClick={() => setMobileOpen(false)}>
                    Post Ad
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      ) : null}
    </header>
  );
}



