import { useRef, type MouseEvent, type ReactNode } from "react";
import { motion, useMotionValue, useSpring } from "framer-motion";
import { Link } from "react-router-dom";

type BaseProps = {
  children: ReactNode;
  variant?: "solid" | "outline" | "ghost" | "invert";
  className?: string;
  magnetic?: boolean;
};

const variants: Record<string, string> = {
  solid:
    "bg-gold-400 text-ink-950 shadow-[0_0_0_0_rgba(245,183,0,0)] hover:shadow-[0_10px_34px_-8px_rgba(245,183,0,0.6)]",
  outline: "border-2 border-gold-400 text-gold-400 hover:text-ink-950",
  ghost: "text-paper-50 hover:text-gold-400",
  /** dark-on-light CTA — for use on gold/paper section backgrounds */
  invert: "bg-ink-950 text-gold-400 shadow-[0_0_0_0_rgba(0,0,0,0)] hover:shadow-[0_10px_34px_-8px_rgba(0,0,0,0.5)]",
};

const fillOverlay: Record<string, string> = {
  solid: "bg-gold-300",
  outline: "bg-gold-400",
  ghost: "bg-transparent",
  invert: "bg-ink-800",
};

const base =
  "group relative isolate inline-flex items-center justify-center gap-2 overflow-hidden rounded-full px-7 py-3.5 font-sans text-sm font-bold uppercase tracking-widest whitespace-nowrap transition-colors duration-300";

/** Pulls the element a few px toward the cursor within its bounds, springs back on leave. Skipped on touch/coarse pointers. */
function useMagnetic<T extends HTMLElement>(strength = 0.35) {
  const ref = useRef<T>(null);
  const x = useMotionValue(0);
  const y = useMotionValue(0);
  const sx = useSpring(x, { stiffness: 250, damping: 18, mass: 0.5 });
  const sy = useSpring(y, { stiffness: 250, damping: 18, mass: 0.5 });

  function onMouseMove(e: MouseEvent<T>) {
    if (!ref.current || !window.matchMedia("(pointer: fine)").matches) return;
    const rect = ref.current.getBoundingClientRect();
    x.set((e.clientX - rect.left - rect.width / 2) * strength);
    y.set((e.clientY - rect.top - rect.height / 2) * strength);
  }
  function onMouseLeave() {
    x.set(0);
    y.set(0);
  }

  return { ref, x: sx, y: sy, onMouseMove, onMouseLeave };
}

function ButtonInner({ children, variant }: { children: ReactNode; variant: string }) {
  return (
    <>
      <span
        aria-hidden="true"
        className={`absolute inset-0 -z-10 -translate-x-full skew-x-[-20deg] transition-transform duration-500 ease-out group-hover:translate-x-0 ${fillOverlay[variant]}`}
      />
      <span className="relative z-10">{children}</span>
    </>
  );
}

export function Button({
  children,
  variant = "solid",
  className = "",
  magnetic = true,
  disabled = false,
  onClick,
  type = "button",
}: BaseProps & { onClick?: () => void; type?: "button" | "submit"; disabled?: boolean }) {
  const mag = useMagnetic<HTMLButtonElement>();
  const active = magnetic && !disabled;

  return (
    <motion.button
      ref={mag.ref}
      style={active ? { x: mag.x, y: mag.y } : undefined}
      onMouseMove={active ? mag.onMouseMove : undefined}
      onMouseLeave={active ? mag.onMouseLeave : undefined}
      whileTap={disabled ? undefined : { scale: 0.95 }}
      type={type}
      onClick={onClick}
      disabled={disabled}
      data-cursor-hover
      className={`${base} ${variants[variant]} ${className} disabled:cursor-not-allowed disabled:opacity-60`}
    >
      <ButtonInner variant={variant}>{children}</ButtonInner>
    </motion.button>
  );
}

export function ButtonLink({
  children,
  variant = "solid",
  className = "",
  magnetic = true,
  to,
  onClick,
}: BaseProps & { to: string; onClick?: () => void }) {
  const mag = useMagnetic<HTMLDivElement>();

  return (
    <motion.div
      ref={mag.ref}
      style={magnetic ? { x: mag.x, y: mag.y } : undefined}
      onMouseMove={magnetic ? mag.onMouseMove : undefined}
      onMouseLeave={magnetic ? mag.onMouseLeave : undefined}
      whileTap={{ scale: 0.95 }}
      className="inline-block"
    >
      <Link to={to} onClick={onClick} data-cursor-hover className={`${base} ${variants[variant]} ${className}`}>
        <ButtonInner variant={variant}>{children}</ButtonInner>
      </Link>
    </motion.div>
  );
}
