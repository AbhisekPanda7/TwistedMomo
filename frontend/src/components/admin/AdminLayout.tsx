import type { ReactNode } from "react";
import { NavLink } from "react-router-dom";
import Container from "../ui/Container";

const tabs = [
  { to: "/admin", label: "Dashboard", end: true },
  { to: "/admin/categories", label: "Categories", end: false },
  { to: "/admin/menu", label: "Menu", end: false },
  { to: "/admin/orders", label: "Orders", end: false },
  { to: "/admin/users", label: "Users", end: false },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-ink-950 pb-24 pt-32 sm:pt-36">
      <Container>
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4 border-b border-ink-600 pb-6">
          <h1 className="font-display text-3xl uppercase text-paper-50">
            Admin <span className="text-gold-400">Console</span>
          </h1>
          <nav className="flex flex-wrap gap-2">
            {tabs.map((tab) => (
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
