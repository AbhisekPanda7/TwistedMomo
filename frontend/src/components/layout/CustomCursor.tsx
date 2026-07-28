import { useEffect, useState } from "react";
import { AnimatePresence, motion, useMotionValue, useSpring } from "framer-motion";

export default function CustomCursor() {
  const [enabled, setEnabled] = useState(false);
  const [hovering, setHovering] = useState(false);
  const [label, setLabel] = useState<string | null>(null);
  const x = useMotionValue(-100);
  const y = useMotionValue(-100);
  const sx = useSpring(x, { stiffness: 500, damping: 40, mass: 0.4 });
  const sy = useSpring(y, { stiffness: 500, damping: 40, mass: 0.4 });
  const ringX = useSpring(x, { stiffness: 200, damping: 26, mass: 0.6 });
  const ringY = useSpring(y, { stiffness: 200, damping: 26, mass: 0.6 });

  useEffect(() => {
    const isFine = window.matchMedia("(pointer: fine)").matches;
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (!isFine || reducedMotion) return;

    setEnabled(true);
    document.documentElement.classList.add("has-custom-cursor");

    const move = (e: MouseEvent) => {
      x.set(e.clientX);
      y.set(e.clientY);
      const el = e.target as HTMLElement;
      const interactive = el.closest<HTMLElement>("a, button, [data-cursor-hover], [data-cursor-label]");
      setHovering(!!interactive);
      setLabel(interactive?.getAttribute("data-cursor-label") ?? null);
    };
    window.addEventListener("mousemove", move);
    return () => {
      window.removeEventListener("mousemove", move);
      document.documentElement.classList.remove("has-custom-cursor");
    };
  }, [x, y]);

  if (!enabled) return null;

  return (
    <>
      {/* trailing ring */}
      <motion.div
        className="pointer-events-none fixed left-0 top-0 z-[90] rounded-full border border-gold-400/50"
        style={{ x: ringX, y: ringY, translateX: "-50%", translateY: "-50%" }}
        animate={{
          width: hovering ? (label ? 64 : 46) : 30,
          height: hovering ? (label ? 64 : 46) : 30,
          opacity: hovering ? 1 : 0.5,
        }}
        transition={{ duration: 0.3, ease: "easeOut" }}
      >
        <AnimatePresence>
          {label && (
            <motion.span
              initial={{ opacity: 0, scale: 0.7 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.7 }}
              className="absolute inset-0 flex items-center justify-center font-sans text-[10px] font-bold uppercase tracking-widest text-gold-400"
            >
              {label}
            </motion.span>
          )}
        </AnimatePresence>
      </motion.div>

      {/* core dot */}
      <motion.div
        className="pointer-events-none fixed left-0 top-0 z-[90] mix-blend-difference"
        style={{ x: sx, y: sy, translateX: "-50%", translateY: "-50%" }}
      >
        <motion.div
          animate={{ scale: hovering ? (label ? 0 : 1.8) : 1 }}
          transition={{ duration: 0.25 }}
          className="h-2.5 w-2.5 rounded-full bg-gold-400"
        />
      </motion.div>
    </>
  );
}
