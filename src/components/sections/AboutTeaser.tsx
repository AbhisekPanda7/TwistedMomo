import { useRef } from "react";
import { motion, useScroll, useTransform } from "framer-motion";
import Container from "../ui/Container";
import BooziMascot from "../ui/BooziMascot";
import { ButtonLink } from "../ui/Button";
import { StarIcon } from "../ui/Icons";
import { staggerContainer, staggerItem, viewportOnce } from "../../lib/motion";

const stats = [
  { value: "2022", label: "Est. Since" },
  { value: "40+", label: "Twisted Recipes" },
  { value: "9", label: "Signature Categories" },
  { value: "5★", label: "Street-Cred Rating" },
];

export default function AboutTeaser() {
  const sectionRef = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ["start end", "end start"] });
  const watermarkY = useTransform(scrollYProgress, [0, 1], [-40, 40]);

  return (
    <section
      ref={sectionRef}
      className="relative overflow-hidden bg-gradient-to-b from-gold-400 via-gold-300 to-gold-400 py-24 text-ink-950 sm:py-32"
    >
      <div className="halftone pointer-events-none absolute inset-0 opacity-[0.12]" />
      <motion.div
        style={{ y: watermarkY }}
        aria-hidden="true"
        className="pointer-events-none absolute -top-10 left-1/2 w-full -translate-x-1/2 text-center font-display text-[22vw] leading-none text-ink-950/5 sm:text-[14vw]"
      >
        BOOZI
      </motion.div>

      <Container className="relative grid items-center gap-14 lg:grid-cols-2">
        <motion.div
          initial={{ opacity: 0, x: -50, rotate: -6 }}
          whileInView={{ opacity: 1, x: 0, rotate: 0 }}
          viewport={viewportOnce}
          transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
          className="order-2 mx-auto w-full max-w-sm lg:order-1"
        >
          <motion.div whileHover={{ scale: 1.03, rotate: -2 }} transition={{ type: "spring", stiffness: 220, damping: 16 }} className="relative">
            <div className="absolute -inset-4 rounded-full border-2 border-dashed border-ink-950/30" />
            <BooziMascot className="relative w-full drop-shadow-[0_20px_40px_rgba(0,0,0,0.25)]" />
            <div className="absolute -bottom-3 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-ink-950 px-5 py-2 font-marker text-sm text-gold-400 shadow-lg">
              Meet Boozi 🏍
            </div>
          </motion.div>
        </motion.div>

        <div className="order-1 lg:order-2">
          <div className="mb-3 flex items-center gap-3">
            <span className="h-[2px] w-8 bg-ink-950" />
            <span className="font-sans text-xs font-bold uppercase tracking-[0.3em]">Our Story</span>
          </div>
          <h2 className="font-masthead text-4xl font-black leading-[1.05] sm:text-5xl lg:text-6xl">
            The City's Best <span className="italic">Momo,</span> Twisted Daily.
          </h2>
          <p className="mt-6 max-w-lg font-sans text-base leading-relaxed text-ink-800/80">
            Twisted Momos started with one rule: never serve a boring momo. Every batch is
            hand-folded, every sauce is made from scratch, and every flavour gets a twist you
            won't find on any other street cart in the city. Boozi — our self-appointed brand
            ambassador — approves of nothing less.
          </p>

          <motion.div
            initial="hidden"
            whileInView="show"
            viewport={viewportOnce}
            variants={staggerContainer(0.08)}
            className="mt-8 grid grid-cols-2 gap-5 sm:grid-cols-4"
          >
            {stats.map((s) => (
              <motion.div
                key={s.label}
                variants={staggerItem}
                whileHover={{ y: -3, scale: 1.03 }}
                className="rounded-2xl border-2 border-ink-950/10 bg-ink-950/5 p-4 text-center transition-colors duration-300 hover:bg-ink-950/10"
              >
                <div className="font-display text-2xl">{s.value}</div>
                <div className="mt-1 font-sans text-[10px] font-bold uppercase tracking-wider text-ink-800/70">
                  {s.label}
                </div>
              </motion.div>
            ))}
          </motion.div>

          <div className="mt-9 flex flex-wrap items-center gap-5">
            <ButtonLink to="/about" variant="invert">
              Read Our Full Story
            </ButtonLink>
            <div className="flex items-center gap-1 text-ink-950">
              {Array.from({ length: 5 }).map((_, i) => (
                <StarIcon key={i} className="h-4 w-4" aria-hidden="true" />
              ))}
              <span className="ml-2 font-sans text-sm font-semibold">Loved by regulars</span>
            </div>
          </div>
        </div>
      </Container>
    </section>
  );
}
