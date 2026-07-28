import { useRef } from "react";
import { motion, useScroll, useTransform } from "framer-motion";
import Container from "../ui/Container";
import { ButtonLink } from "../ui/Button";
import { ChiliIcon, DumplingIcon } from "../ui/Icons";
import { viewportOnce } from "../../lib/motion";

export default function FinalCTA() {
  const sectionRef = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ["start end", "end start"] });
  const chiliY = useTransform(scrollYProgress, [0, 1], [-50, 50]);
  const dumplingY = useTransform(scrollYProgress, [0, 1], [50, -50]);

  return (
    <section ref={sectionRef} className="relative overflow-hidden bg-ink-950 py-24 sm:py-32">
      <div className="pointer-events-none absolute left-1/2 top-1/2 h-[500px] w-[500px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-gold-400/10 blur-[140px]" />

      <motion.div
        aria-hidden="true"
        style={{ y: chiliY, ["--float-rot" as string]: "-12deg" }}
        className="pointer-events-none absolute left-[8%] top-10 hidden animate-float text-gold-400/30 sm:block"
      >
        <ChiliIcon className="h-16 w-16" />
      </motion.div>
      <motion.div
        aria-hidden="true"
        style={{ y: dumplingY, ["--float-rot" as string]: "15deg", animationDelay: "1s" }}
        className="pointer-events-none absolute right-[10%] bottom-10 hidden animate-float text-gold-400/30 sm:block"
      >
        <DumplingIcon className="h-20 w-20" />
      </motion.div>

      <Container className="relative text-center">
        <motion.span
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={viewportOnce}
          className="font-sans text-xs font-bold uppercase tracking-[0.35em] text-gold-400"
        >
          Hungry Yet?
        </motion.span>
        <motion.h2
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={viewportOnce}
          transition={{ duration: 0.6 }}
          className="mx-auto mt-4 max-w-3xl text-balance font-display text-4xl uppercase leading-[0.95] text-paper-50 sm:text-6xl"
        >
          Your Next Favourite <span className="text-gold-400">Momo</span> Is One Order Away
        </motion.h2>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={viewportOnce}
          transition={{ duration: 0.6, delay: 0.15 }}
          className="mt-10 flex flex-wrap items-center justify-center gap-4"
        >
          <ButtonLink to="/menu">Order Now</ButtonLink>
          <ButtonLink to="/contact" variant="outline">
            Visit Us Today
          </ButtonLink>
        </motion.div>
      </Container>
    </section>
  );
}
