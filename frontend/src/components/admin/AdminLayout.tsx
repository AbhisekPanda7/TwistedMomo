import type { ReactNode } from "react";
import { NavLink } from "react-router-dom";
import Container from "../ui/Container";
import { useAuth } from "../../context/AuthContext";
import { hasRole } from "../../lib/tokenStorage";

const tabs = [
  { to: "/admin", label: "Dashboard", end: true, adminOnly: true },
  { to: "/admin/categories", label: "Categories", end: false, adminOnly: true },
  { to: "/admin/menu", label: "Menu", end: false, adminOnly: true },
  { to: "/admin/orders", label: "Orders", end: false, adminOnly: false },
  { to: "/admin/users", label: "Users", end: false, adminOnly: true },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const isAdmin = hasRole(user, "ADMIN");
  // Presentation only — the ProtectedRoute allow list is what actually blocks staff.
  const visibleTabs = tabs.filter((tab) => !tab.adminOnly || isAdmin);

  return (
    <div className="min-h-screen bg-ink-950 pb-24 pt-32 sm:pt-36">
      <Container>
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4 border-b border-ink-600 pb-6">
          <h1 className="font-display text-3xl uppercase text-paper-50">
            Admin <span className="text-gold-400">Console</span>
          </h1>
          <nav className="flex flex-wrap gap-2">
            {visibleTabs.map((tab) => (
              <NavLink
                key={tab.to}
                to={tab.to}
                end={tab.end}
                data-cursor-hover
                className={({ isActive }) =>
                  `rounded-full px-4 py-2 font-sans text-xs font-bold uppercase tracking-wider transition-colors ${
                    isActive ? "bg-gold-400 text-ink-950" : "text-paper-200/60 hover:text-gold-400"
                  }`
                }
              >
                {tab.label}
              </NavLink>
            ))}
          </nav>
        </div>
        {children}
      </Container>
    </div>
  );
}
