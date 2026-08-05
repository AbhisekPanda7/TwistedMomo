import type { ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import Navbar from "./Navbar";
import Footer from "./Footer";
import ScrollToTop from "./ScrollToTop";
import CustomCursor from "./CustomCursor";
import PageTransition from "./PageTransition";

/** Shared by both shells: brand cursor and scroll restoration, no chrome. */
export function BareLayout({ children }: { children: ReactNode }) {
  return (
    <div className="relative min-h-screen bg-ink-950">
      <CustomCursor />
      <ScrollToTop />
      {children}
    </div>
  );
}

export default function Layout({ children }: { children: ReactNode }) {
  const location = useLocation();

  return (
    <BareLayout>
      <Navbar />
      <AnimatePresence mode="wait" initial={false}>
        <PageTransition key={location.pathname}>
          <main>{children}</main>
        </PageTransition>
      </AnimatePresence>
      <Footer />
    </BareLayout>
  );
}
