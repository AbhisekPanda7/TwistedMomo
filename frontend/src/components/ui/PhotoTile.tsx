import { motion } from "framer-motion";
import { DumplingIcon } from "./Icons";

const gradients = [
  "from-gold-500 via-gold-300 to-gold-600",
  "from-ember-600 via-ember-500 to-chili-500",
  "from-gold-300 via-gold-500 to-ember-600",
  "from-chili-600 via-ember-500 to-gold-500",
  "from-gold-600 via-ember-600 to-chili-600",
];

export default function PhotoTile({
  label,
  variant = 0,
  className = "",
  big = false,
  onClick,
}: {
  label: string;
  variant?: number;
  className?: string;
  big?: boolean;
  onClick?: () => void;
}) {
  const g = gradients[variant % gradients.length];
  const interactive = !!onClick;

  return (
    <motion.div
      onClick={onClick}
      whileHover={{ scale: 1.03 }}
      whileTap={interactive ? { scale: 0.97 } : undefined}
      transition={{ duration: 0.4 }}
      data-cursor-label={interactive ? "View" : undefined}
      role={interactive ? "button" : undefined}
      tabIndex={interactive ? 0 : undefined}
      onKeyDown={
        interactive
          ? (e) => {
              if (e.key === "Enter" || e.key === " ") onClick?.();
            }
          : undefined
      }
      className={`group relative overflow-hidden rounded-2xl bg-gradient-to-br shadow-[0_16px_36px_-20px_rgba(0,0,0,0.7)] transition-shadow duration-300 hover:shadow-[0_22px_44px_-16px_rgba(0,0,0,0.8)] ${
        interactive ? "cursor-pointer" : ""
      } ${g} ${className}`}
    >
      <div className="halftone absolute inset-0 opacity-[0.15] mix-blend-overlay" />
      <div className="absolute inset-0 bg-ink-950/10 transition-colors duration-500 group-hover:bg-ink-950/0" />
      <div className="flex h-full w-full flex-col items-center justify-center gap-3 p-6 text-center">
        <DumplingIcon
          className={`${big ? "h-14 w-14" : "h-9 w-9"} text-ink-950/70 transition-transform duration-500 group-hover:-translate-y-1 group-hover:scale-110`}
          aria-hidden="true"
        />
        <span className={`font-display uppercase tracking-wide text-ink-950/80 ${big ? "text-2xl" : "text-sm"}`}>
          {label}
        </span>
      </div>
      <div className="absolute inset-0 opacity-0 ring-2 ring-inset ring-ink-950/10 transition-opacity duration-500 group-hover:opacity-100" />
    </motion.div>
  );
}
