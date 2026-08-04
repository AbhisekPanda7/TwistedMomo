import { useRef, type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { hasRole } from "../../lib/tokenStorage";

export default function ProtectedRoute({
  children,
  adminOnly = false,
}: {
  children: ReactNode;
  adminOnly?: boolean;
}) {
  const { user } = useAuth();
  const location = useLocation();
  const mountedPathRef = useRef(location.pathname);

  if (!user) {
    // AnimatePresence keeps an exiting page's component tree mounted (and thus still
    // reactive to context) for its exit animation. That zombie instance's own
    // useLocation() stays pinned to the path it originally matched — it does NOT track
    // the router's live location — so we compare against the real browser URL instead.
    // If they differ, the router has already moved on (e.g. the navbar's Log Out button
    // called navigate() elsewhere) and firing our own redirect here would just race and
    // override that intentional navigation. Only redirect when we're still genuinely on
    // the path we were mounted to guard (e.g. session expired while idle here).
    if (window.location.pathname !== mountedPathRef.current) {
      return null;
    }
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (adminOnly && !hasRole(user, "ADMIN")) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
