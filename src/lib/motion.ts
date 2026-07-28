import type { Transition, Variants } from "framer-motion";

/** Shared premium easing curve — confident deceleration, no bounce. */
export const EASE = [0.22, 1, 0.36, 1] as const;
export const EASE_SOFT = [0.16, 1, 0.3, 1] as const;

export const fadeUp: Variants = {
  hidden: { opacity: 0, y: 28 },
  show: { opacity: 1, y: 0, transition: { duration: 0.6, ease: EASE } },
};

/** Wrap a list with this, then give each child `variants={staggerItem}` — no manual index * delay math needed. */
export function staggerContainer(stagger = 0.09, delayChildren = 0): Variants {
  return {
    hidden: {},
    show: {
      transition: { staggerChildren: stagger, delayChildren },
    },
  };
}

export const staggerItem: Variants = {
  hidden: { opacity: 0, y: 26 },
  show: { opacity: 1, y: 0, transition: { duration: 0.55, ease: EASE } },
};

export const scaleIn: Variants = {
  hidden: { opacity: 0, scale: 0.92 },
  show: { opacity: 1, scale: 1, transition: { duration: 0.55, ease: EASE } },
};

export const viewportOnce = { once: true, margin: "-80px" } as const;

export const springy: Transition = { type: "spring", stiffness: 300, damping: 24 };
