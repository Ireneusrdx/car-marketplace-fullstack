import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";

// Full-page skeleton shown while initialize() is resolving.
// Critical: must NOT redirect while isLoading is true — doing so causes an
// infinite loop (redirect → initialize → 401 → redirect → …).
function FullPageSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="w-12 h-12 rounded-full bg-blue/20 animate-pulse" />
        <div className="w-32 h-3 rounded-full bg-gray-200 animate-pulse" />
      </div>
    </div>
  );
}

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

  // Wait for initialize() to finish before making any auth decision
  if (isLoading) {
    return <FullPageSkeleton />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/auth" state={{ from: location }} replace />;
  }

  return children;
}
