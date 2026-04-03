import { AnimatePresence } from "framer-motion";
import { useEffect } from "react";
import { Route, Routes, useLocation } from "react-router-dom";
import { cn } from "@/lib/cn";
import ErrorBoundary from "@/components/global/ErrorBoundary";
import Footer from "@/components/global/Footer";
import Navbar from "@/components/global/Navbar";
import AiFinderPage from "@/pages/AiFinderPage";
import AuthPage from "@/pages/AuthPage";
import CarDetailPage from "@/pages/CarDetailPage";
import ComparePage from "@/pages/ComparePage";
import DashboardPage from "@/pages/DashboardPage";
import HomePage from "@/pages/HomePage";
import ListingsPage from "@/pages/ListingsPage";
import NotFoundPage from "@/pages/NotFoundPage";
import PostListingPage from "@/pages/PostListingPage";
import { useAuthStore } from "@/store/authStore";
import ProtectedRoute from "@/components/ProtectedRoute";

export default function App() {
  const location = useLocation();
  const initialize = useAuthStore((s) => s.initialize);

  useEffect(() => {
    initialize();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div className="min-h-screen bg-white">
      <Navbar />
      <div className={cn(location.pathname !== "/" && "pt-20")}>
        <AnimatePresence mode="wait">
          <Routes location={location} key={location.pathname}>
            <Route path="/" element={<HomePage />} />
            <Route path="/listings" element={<ListingsPage />} />
            <Route path="/cars/:slug" element={<CarDetailPage />} />
            <Route path="/ai-finder" element={<AiFinderPage />} />
            <Route path="/compare" element={<ComparePage />} />
            <Route 
              path="/sell" 
              element={
                <ProtectedRoute>
                  <PostListingPage />
                </ProtectedRoute>
              } 
            />
            <Route path="/auth" element={<AuthPage />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <ErrorBoundary>
                    <DashboardPage />
                  </ErrorBoundary>
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </AnimatePresence>
      </div>
      <Footer />
    </div>
  );
}













