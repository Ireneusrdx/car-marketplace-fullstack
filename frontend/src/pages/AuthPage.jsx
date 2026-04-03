import {
  CheckCircle,
  Eye,
  EyeOff,
  Lock,
  Mail,
  Phone,
  Sparkles
} from "lucide-react";
import { useEffect, useState } from "react";
import { Helmet } from "react-helmet-async";
import { useNavigate } from "react-router-dom";
import Button from "@/components/global/Button";
import Input from "@/components/global/Input";
import PageTransition from "@/components/global/PageTransition";
import { notify } from "@/components/global/ToastProvider";
import { loginWithEmail, registerWithEmail } from "@/lib/authService";
import { firebaseAuth, firebaseEnabled } from "@/lib/firebase";
import api from "@/lib/api";
import { useAuthStore } from "@/store/authStore";

function SocialButton({ label, icon, onClick, disabled }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm font-semibold text-gray-700 transition-colors hover:border-blue/30 hover:bg-blue/5 disabled:opacity-50"
    >
      <span className="text-base">{icon}</span>
      {label}
    </button>
  );
}

function OtpBoxes() {
  const [otp, setOtp] = useState(Array(6).fill(""));

  const handleChange = (element, index) => {
    if (isNaN(element.value)) return false;
    setOtp([...otp.map((d, idx) => (idx === index ? element.value : d))]);
    if (element.nextSibling && element.value !== "") {
      element.nextSibling.focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace") {
      if (otp[index] === "") {
        if (e.target.previousSibling) {
          e.target.previousSibling.focus();
        }
      } else {
        setOtp([...otp.map((d, idx) => (idx === index ? "" : d))]);
      }
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").slice(0, 6).split("");
    if (pastedData.some(char => isNaN(char))) return;
    
    const newOtp = [...otp];
    pastedData.forEach((char, index) => {
      newOtp[index] = char;
    });
    setOtp(newOtp);
    
    const inputs = e.target.parentElement.querySelectorAll("input");
    const focusIndex = Math.min(pastedData.length, 5);
    if (inputs[focusIndex]) {
      inputs[focusIndex].focus();
    }
  };

  return (
    <div className="flex items-center gap-2 sm:gap-3 justify-between">
      {otp.map((data, index) => (
        <input
          key={index}
          type="text"
          maxLength={1}
          value={data}
          onChange={(e) => handleChange(e.target, index)}
          onKeyDown={(e) => handleKeyDown(e, index)}
          onPaste={handlePaste}
          onFocus={(e) => e.target.select()}
          className={`h-10 sm:h-12 md:h-14 flex-1 w-full min-w-0 rounded-xl border bg-white shadow-sm text-center text-lg sm:text-xl font-bold outline-none transition-all duration-200 ${
            data
              ? "border-blue text-blue ring-4 ring-blue/10 bg-blue/5"
              : "border-gray-200 text-gray-900 focus:border-blue focus:ring-4 focus:ring-blue/10"
          }`}
        />
      ))}
    </div>
  );
}

export default function AuthPage() {
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, setAuth } = useAuthStore((state) => ({
    isAuthenticated: state.isAuthenticated,
    isLoading: state.isLoading,
    setAuth: state.setAuth,
  }));
  const [tab, setTab] = useState("signin");
  // All useState / useEffect hooks MUST come before any early returns
  // (React Rules of Hooks). These were previously placed after the
  // isLoading guard — that caused a runtime hook-order violation.
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    fullName: "",
    phone: "",
    email: "",
    password: ""
  });

  // Redirect already-authenticated users away from the auth page
  useEffect(() => {
    if (!isLoading && isAuthenticated && !submitting) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, isLoading, submitting, navigate]);

  const updateForm = (patch) => setForm((prev) => ({ ...prev, ...patch }));

  const [socialLoading, setSocialLoading] = useState(false);

  const handleGoogleSignIn = async () => {
    if (!firebaseEnabled || !firebaseAuth) {
      notify.error("Google sign-in is not configured.");
      return;
    }
    setSocialLoading(true);
    try {
      const { GoogleAuthProvider, signInWithPopup } = await import("firebase/auth");
      const provider = new GoogleAuthProvider();
      const result = await signInWithPopup(firebaseAuth, provider);
      const idToken = await result.user.getIdToken();

      const response = await api.post("/auth/firebase", { idToken });
      const data = response.data;
      setAuth({
        user: data.user,
        accessToken: data.accessToken,
      });
      notify.success("Signed in with Google!");
      navigate("/dashboard");
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || "Google sign-in failed.";
      notify.error(msg);
    } finally {
      setSocialLoading(false);
    }
  };

  // Show skeleton while initialize() is still running
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-blue/20 animate-pulse" />
          <div className="w-32 h-3 rounded-full bg-gray-200 animate-pulse" />
        </div>
      </div>
    );
  }

  const onSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);

    try {
      const payload =
        tab === "signin"
          ? {
              email: form.email,
              password: form.password
            }
          : {
              email: form.email,
              password: form.password,
              fullName: form.fullName,
              phone: form.phone || null
            };

      const authResult =
        tab === "signin"
          ? await loginWithEmail(payload)
          : await registerWithEmail(payload);

      setAuth({
        user: authResult.user,
        accessToken: authResult.accessToken
      });

      notify.success(tab === "signin" ? "Signed in successfully." : "Account created successfully.");
      navigate("/dashboard");
    } catch (error) {
      notify.error(error.message || "Authentication failed.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageTransition>
      <Helmet>
        <title>Sign In | AutoVault</title>
        <meta name="description" content="Sign in or create an AutoVault account to save cars, compare listings, and manage bookings." />
      </Helmet>
      <main className="relative min-h-screen overflow-hidden bg-black">
        <img
          src="https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1920&q=80"
          alt="Luxury car background"
          className="absolute inset-0 h-full w-full object-cover opacity-35"
        />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_40%,rgba(0,87,255,0.25),transparent_45%)]" />
        <div className="absolute inset-0 bg-gradient-to-r from-black/75 via-black/60 to-black/35" />

        <section className="relative mx-auto grid min-h-screen w-full max-w-content items-center gap-6 px-6 py-10 lg:grid-cols-2">
          <div className="hidden h-full flex-col justify-between rounded-3xl border border-white/10 bg-black/50 p-10 backdrop-blur lg:flex">
            <div>
              <p className="inline-flex items-center rounded-full border border-blue/30 bg-blue/20 px-4 py-1.5 text-xs font-semibold tracking-[0.16em] text-blue">
                AUTOVAULT SECURE ACCESS
              </p>
              <h1 className="mt-6 font-montserrat text-5xl font-black leading-tight text-white">
                YOUR NEXT CAR
                <br />
                AWAITS.
              </h1>
            </div>

            <div className="space-y-3 text-white/80">
              {[
                "50,000+ verified listings",
                "AI-powered recommendations",
                "Secure booking and payments"
              ].map((item) => (
                <p key={item} className="inline-flex items-center gap-2 text-sm">
                  <CheckCircle size={15} strokeWidth={1.5} className="text-blue" />
                  {item}
                </p>
              ))}
            </div>
          </div>

          <div className="rounded-3xl border border-white/40 bg-white/95 p-8 shadow-blue-xl backdrop-blur-xl md:p-10">
            <p className="mb-6 text-center font-montserrat text-2xl font-black text-gray-900">AUTO<span className="text-blue">VAULT</span></p>

            <div className="mb-6 grid grid-cols-2 rounded-full bg-gray-100 p-1">
              <button
                onClick={() => setTab("signin")}
                className={
                  tab === "signin"
                    ? "rounded-full bg-white px-4 py-2 text-sm font-semibold text-blue shadow-blue-sm"
                    : "rounded-full px-4 py-2 text-sm font-medium text-gray-600"
                }
              >
                Sign In
              </button>
              <button
                onClick={() => setTab("signup")}
                className={
                  tab === "signup"
                    ? "rounded-full bg-white px-4 py-2 text-sm font-semibold text-blue shadow-blue-sm"
                    : "rounded-full px-4 py-2 text-sm font-medium text-gray-600"
                }
              >
                Create Account
              </button>
            </div>

            <form className="space-y-4" onSubmit={onSubmit}>
              {tab === "signup" ? (
                <Input
                  label="Full Name"
                  placeholder="Enter your full name"
                  value={form.fullName}
                  onChange={(event) => updateForm({ fullName: event.target.value })}
                  required
                />
              ) : null}

              {tab === "signup" ? (
                <Input
                  label="Phone (optional)"
                  placeholder="+1 555 000 0000"
                  value={form.phone}
                  onChange={(event) => updateForm({ phone: event.target.value })}
                />
              ) : null}

              <Input
                icon={Mail}
                label="Email"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={(event) => updateForm({ email: event.target.value })}
                required
              />

              <label className="block">
                <span className="mb-2 block text-sm font-medium text-gray-600">Password</span>
                <div className="relative">
                  <Lock size={18} strokeWidth={1.5} className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter password"
                    value={form.password}
                    onChange={(event) => updateForm({ password: event.target.value })}
                    minLength={tab === "signup" ? 8 : 1}
                    required
                    className="w-full rounded-xl border border-gray-200 px-5 py-3.5 pl-11 pr-11 text-[15px] outline-none transition-all duration-200 focus:border-blue focus:ring-4 focus:ring-blue/10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((prev) => !prev)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-400 hover:text-blue"
                  >
                    {showPassword ? <EyeOff size={18} strokeWidth={1.5} /> : <Eye size={18} strokeWidth={1.5} />}
                  </button>
                </div>
              </label>

              {tab === "signin" ? (
                <div className="text-right text-sm font-medium text-blue">Forgot password?</div>
              ) : (
                <p className="text-xs text-gray-500">By creating an account you agree to our Terms and Privacy Policy.</p>
              )}

              <Button className="w-full" type="submit" disabled={submitting}>
                {submitting ? "Please wait..." : tab === "signin" ? "Sign In" : "Create Account"}
              </Button>
            </form>

            {firebaseEnabled && (
              <>
                <div className="my-6 flex items-center gap-3 text-xs text-gray-400">
                  <span className="h-px flex-1 bg-gray-200" />
                  OR CONTINUE WITH
                  <span className="h-px flex-1 bg-gray-200" />
                </div>

                <div className="grid gap-3 md:grid-cols-1">
                  <SocialButton label="Google" icon="G" onClick={handleGoogleSignIn} disabled={socialLoading} />
                </div>
              </>
            )}

            <div className="relative mt-8 overflow-hidden rounded-2xl border border-blue/20 bg-gradient-to-b from-blue/5 to-white/50 p-6 shadow-blue-sm backdrop-blur-sm">
              <div className="mb-4 flex items-center justify-between">
                <p className="inline-flex items-center gap-2 text-sm font-bold tracking-wide text-gray-900">
                  <span className="flex h-6 w-6 items-center justify-center rounded-full bg-blue/10">
                    <Phone size={12} strokeWidth={2} className="text-blue" />
                  </span>
                  Verify Number
                </p>
                <div className="rounded-full bg-white px-2.5 py-1 text-[10px] font-black uppercase tracking-widest text-blue shadow-sm ring-1 ring-blue/10">
                  Preview
                </div>
              </div>
              <p className="mb-5 text-balance text-sm font-medium text-gray-500">
                We've sent a 6-digit verification code to your phone.
              </p>
              
              <OtpBoxes />
              
              <div className="mt-6 flex flex-col gap-2">
                <button className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-blue px-4 py-3 text-sm font-bold text-white shadow-blue-md transition-all hover:bg-blue-600 active:scale-[0.98]">
                  Verify Code
                </button>
                <button className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-xs font-semibold text-gray-600 transition-all hover:bg-gray-50 hover:text-blue">
                  <Sparkles size={14} strokeWidth={1.5} className="text-blue" />
                  Resend code in 24s
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </PageTransition>
  );
}



