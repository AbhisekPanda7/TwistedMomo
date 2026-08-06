import { Suspense, type ReactNode } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import Navbar from "./Navbar";
import Footer from "./Footer";
import ScrollToTop from "./ScrollToTop";
import CustomCursor from "./CustomCursor";
import PageTransition from "./PageTransition";

const FALLBACK = <div className="min-h-screen bg-ink-950" />;

/** Cursor + scroll restoration mount here, once, so no page can end up under two layout routes at once. */
function ShellChrome({ children }: { children: ReactNode }) {
  return (
    <div className="relative min-h-screen bg-ink-950">
      <CustomCursor />
      <ScrollToTop />
      {children}
    </div>
  );
}

/** Admin layout route: brand cursor and scroll restoration, no marketing chrome. */
export function BareLayout() {
  return (
    <ShellChrome>
      <Suspense fallback={FALLBACK}>
        <Outlet />
      </Suspense>
    </ShellChrome>
  );
}

export default function Layout() {
  const location = useLocation();

  return (
    <ShellChrome>
      <Navbar />
      <AnimatePresence mode="wait" initial={false}>
        <PageTransition key={location.pathname}>
          <main>
            <Suspense fallback={FALLBACK}>
              <Outlet />
            </Suspense>
          </main>
        </PageTransition>
      </AnimatePresence>
      <Footer />
    </ShellChrome>
  );
}
