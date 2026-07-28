import { useEffect, useState } from "react";
import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import Logo from "../ui/Logo";

export default function Loader() {
  const [done, setDone] = useState(false);
  const reduced = useReducedMotion();

  useEffect(() => {
    const t = setTimeout(() => setDone(true), reduced ? 250 : 1500);
    return () => clearTimeout(t);
  }, [reduced]);

  return (
    <AnimatePresence>
      {!done && (
        <motion.div
          key="loader"
          exit={{ clipPath: "circle(0% at 50% 50%)" }}
          transition={{ duration: reduced ? 0.2 : 0.7, ease: [0.76, 0, 0.24, 1] }}
          className="fixed inset-0 z-[100] flex flex-col items-center justify-center gap-6 bg-ink-950"
        >
          <motion.div
            exit={{ scale: 1.15, opacity: 0 }}
            transition={{ duration: 0.5 }}
            className="relative flex flex-col items-center gap-6"
          >
            {!reduced &&
              [0, 1, 2].map((i) => (
                <span
                  key={i}
                  className="absolute -top-3 left-1/2 h-8 w-3 -translate-x-1/2 rounded-full bg-gold-400/30 blur-[3px] animate-steam"
                  style={{ animationDelay: `${i * 0.9}s`, marginLeft: `${(i - 1) * 14}px` }}
                />
              ))}

            <motion.div
              initial={{ scale: 0.6, rotate: -20, opacity: 0 }}
              animate={{ scale: 1, rotate: 0, opacity: 1 }}
              transition={{ duration: reduced ? 0.2 : 0.6, ease: "backOut" }}
            >
              <Logo className="h-28 w-28 sm:h-36 sm:w-36" />
            </motion.div>

            <div className="relative h-[3px] w-40 overflow-hidden rounded-full bg-ink-700 sm:w-56">
              <motion.div
                initial={{ scaleX: 0 }}
                animate={{ scaleX: 1 }}
                transition={{ duration: reduced ? 0.2 : 1.1, ease: "easeInOut" }}
                style={{ originX: 0 }}
                className="h-full w-full bg-gold-400"
              />
              {!reduced && (
                <span className="absolute inset-0 -translate-x-full animate-shimmer bg-gradient-to-r from-transparent via-white/40 to-transparent" />
              )}
            </div>

            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: reduced ? 0 : 0.3 }}
              className="font-sans text-xs font-bold uppercase tracking-[0.4em] text-paper-200/70"
            >
              Twisting Flavours
            </motion.p>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
