import type { ReactNode } from "react";
import { motion, useReducedMotion } from "framer-motion";
import Logo from "../ui/Logo";

const EASE = [0.76, 0, 0.24, 1] as const;

export default function PageTransition({ children }: { children: ReactNode }) {
  const reduced = useReducedMotion();

  if (reduced) {
    return <div>{children}</div>;
  }

  return (
    <>
      {/* trailing ink panel — slightly offset for a two-tone curtain depth effect */}
      <motion.div
        initial={{ scaleY: 1 }}
        animate={{ scaleY: 0 }}
        exit={{ scaleY: 1 }}
        transition={{ duration: 0.5, ease: EASE, delay: 0.06 }}
        style={{ originY: 0 }}
        className="pointer-events-none fixed inset-0 z-[69] bg-ink-900"
      />
      {/* primary gold curtain */}
      <motion.div
        initial={{ scaleY: 1 }}
        animate={{ scaleY: 0 }}
        exit={{ scaleY: 1 }}
        transition={{ duration: 0.5, ease: EASE }}
        style={{ originY: 0 }}
        className="pointer-events-none fixed inset-0 z-[70] flex items-center justify-center bg-gold-400"
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.7 }}
          animate={{ opacity: [0, 1, 0], scale: 1 }}
          transition={{ duration: 0.5, times: [0, 0.5, 1] }}
        >
          <Logo className="h-16 w-16" />
        </motion.div>
      </motion.div>
      {children}
    </>
  );
}
