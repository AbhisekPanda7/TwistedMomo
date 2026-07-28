import type { ReactNode } from "react";
import Reveal from "./Reveal";
import Container from "./Container";

/**
 * Shared hero banner for interior pages (About/Menu/Gallery/Contact) — keeps
 * the glow/grain backdrop, kicker, title and optional description in sync
 * across the site instead of four near-identical copies.
 */
export default function PageHero({
  kicker,
  title,
  description,
  children,
}: {
  kicker: string;
  title: ReactNode;
  description?: string;
  children?: ReactNode;
}) {
  return (
    <section className="relative overflow-hidden bg-ink-950 pb-16 pt-36 sm:pt-44">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute left-1/2 top-0 h-[45%] w-[90%] -translate-x-1/2 rounded-full bg-gold-400/10 blur-[120px]" />
        <div className="grain absolute inset-0" />
      </div>
      <Container className="relative text-center">
        <Reveal>
          <span className="font-sans text-xs font-bold uppercase tracking-[0.35em] text-gold-400">{kicker}</span>
        </Reveal>
        <Reveal delay={0.1}>
          <h1 className="mx-auto mt-4 max-w-3xl text-balance font-display text-5xl uppercase leading-[0.9] text-paper-50 sm:text-7xl">
            {title}
          </h1>
        </Reveal>
        {description && (
          <Reveal delay={0.2}>
            <p className="mx-auto mt-6 max-w-lg font-sans text-sm leading-relaxed text-paper-200/70 sm:text-base">
              {description}
            </p>
          </Reveal>
        )}
        {children}
      </Container>
    </section>
  );
}
