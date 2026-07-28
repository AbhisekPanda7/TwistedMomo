import type { ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import Navbar from "./Navbar";
import Footer from "./Footer";
import ScrollToTop from "./ScrollToTop";
import CustomCursor from "./CustomCursor";
import PageTransition from "./PageTransition";

export default function Layout({ children }: { children: ReactNode }) {
  const location = useLocation();

  return (
    <div className="relative min-h-screen bg-ink-950">
      <CustomCursor />
      <ScrollToTop />
      <Navbar />
      <AnimatePresence mode="wait" initial={false}>
        <PageTransition key={location.pathname}>
          <main>{children}</main>
        </PageTransition>
      </AnimatePresence>
      <Footer />
    </div>
  );
}
