import { useRef, type MouseEvent } from "react";
import { motion, useMotionValue, useScroll, useSpring, useTransform } from "framer-motion";
import { ButtonLink } from "../ui/Button";
import MomoPlate from "../ui/MomoPlate";
import StampBadge from "../ui/StampBadge";
import { ChiliIcon, DumplingIcon, FlameIcon } from "../ui/Icons";
import MarqueeStrip from "../ui/MarqueeStrip";

export default function Hero() {
  const sectionRef = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ["start start", "end start"] });

  const glowY = useTransform(scrollYProgress, [0, 1], [0, 90]);
  const copyY = useTransform(scrollYProgress, [0, 1], [0, -40]);
  const plateY = useTransform(scrollYProgress, [0, 1], [0, -80]);
  const contentFade = useTransform(scrollYProgress, [0, 0.85], [1, 0]);

  // mouse-tilt depth effect on the plate visual
  const rx = useMotionValue(0);
  const ry = useMotionValue(0);
  const srx = useSpring(rx, { stiffness: 150, damping: 18 });
  const sry = useSpring(ry, { stiffness: 150, damping: 18 });

  function onTiltMove(e: MouseEvent<HTMLDivElement>) {
    if (!window.matchMedia("(pointer: fine)").matches) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const px = (e.clientX - rect.left) / rect.width - 0.5;
    const py = (e.clientY - rect.top) / rect.height - 0.5;
    ry.set(px * 16);
    rx.set(py * -16);
  }
  function onTiltLeave() {
    rx.set(0);
    ry.set(0);
  }

  return (
    <section ref={sectionRef} className="relative flex min-h-[100svh] flex-col overflow-hidden bg-ink-950 pt-28">
      {/* atmosphere */}
      <motion.div style={{ y: glowY }} className="pointer-events-none absolute inset-0">
        <div className="absolute left-1/2 top-[-10%] h-[60%] w-[90%] -translate-x-1/2 rounded-full bg-gold-400/10 blur-[120px]" />
        <div className="absolute bottom-0 right-[-10%] h-[50%] w-[50%] rounded-full bg-ember-600/20 blur-[110px]" />
      </motion.div>
      <div className="grain pointer-events-none absolute inset-0" />

      <motion.div
        style={{ opacity: contentFade }}
        className="relative mx-auto flex w-full max-w-7xl flex-1 flex-col items-center px-5 sm:px-8 lg:flex-row lg:items-center lg:gap-8 lg:px-12"
      >
        {/* copy */}
        <motion.div
          style={{ y: copyY }}
          className="relative z-10 flex flex-1 flex-col items-center text-center lg:items-start lg:text-left"
        >
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.15, duration: 0.6 }}
            className="mb-5 flex items-center gap-3 rounded-full border border-gold-400/30 bg-gold-400/5 px-4 py-1.5"
          >
            <FlameIcon className="h-3.5 w-3.5 text-gold-400" aria-hidden="true" />
            <span className="font-sans text-xs font-bold uppercase tracking-[0.25em] text-gold-400">
              Est. 2022 &middot; Twisting Flavours
            </span>
          </motion.div>

          <h1 className="font-display leading-[0.86] text-paper-50">
            <motion.span
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.05, duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
              className="block text-stroke-gold text-[16vw] sm:text-[13vw] lg:text-[6.4vw]"
            >
              TWISTED
            </motion.span>
            <motion.span
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
              className="-mt-1 block text-gold-400 text-[16vw] sm:text-[13vw] lg:text-[6.4vw]"
            >
              MOMOS
            </motion.span>
          </h1>

          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.6 }}
            className="mt-6 max-w-md font-sans text-base leading-relaxed text-paper-200/75 sm:text-lg"
          >
            Bold street food, no boring bites. Steamed, fried, tandoori-torched or
            drowned in chilli butter garlic — twisted our way since day one.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.55, duration: 0.6 }}
            className="mt-9 flex flex-wrap items-center justify-center gap-4 lg:justify-start"
          >
            <ButtonLink to="/menu">Explore Menu</ButtonLink>
            <ButtonLink to="/contact" variant="outline">
              Find Us
            </ButtonLink>
          </motion.div>

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.8, duration: 0.6 }}
            className="mt-10 flex items-center gap-6 text-paper-200/60"
          >
            <div className="text-center">
              <div className="font-display text-2xl text-gold-400">40+</div>
              <div className="font-sans text-[11px] uppercase tracking-widest">Twisted Flavours</div>
            </div>
            <div className="h-8 w-px bg-ink-600" />
            <div className="text-center">
              <div className="font-display text-2xl text-gold-400">100%</div>
              <div className="font-sans text-[11px] uppercase tracking-widest">Fresh Daily</div>
            </div>
          </motion.div>
        </motion.div>

        {/* visual */}
        <motion.div
          style={{ y: plateY }}
          initial={{ opacity: 0, scale: 0.85, rotate: -8 }}
          animate={{ opacity: 1, scale: 1, rotate: 0 }}
          transition={{ delay: 0.3, duration: 0.9, ease: [0.22, 1, 0.36, 1] }}
          className="relative z-10 mt-14 flex flex-1 items-center justify-center [perspective:900px] lg:mt-0"
          onMouseMove={onTiltMove}
          onMouseLeave={onTiltLeave}
        >
          <motion.div
            style={{ rotateX: srx, rotateY: sry, transformStyle: "preserve-3d" }}
            className="relative h-[62vw] max-h-[420px] w-[62vw] max-w-[420px] sm:h-[380px] sm:w-[380px]"
          >
            <div className="absolute inset-0 animate-spin-slow">
              <MomoPlate />
            </div>

            <motion.div
              style={{ ["--float-rot" as string]: "-8deg" }}
              className="absolute -left-6 top-2 animate-float text-gold-400 sm:-left-10"
            >
              <ChiliIcon className="h-10 w-10 drop-shadow-[0_0_12px_rgba(245,183,0,0.4)] sm:h-14 sm:w-14" />
            </motion.div>
            <motion.div
              style={{ ["--float-rot" as string]: "10deg", animationDelay: "1.2s" }}
              className="absolute -right-4 top-10 animate-float text-paper-100 sm:-right-8"
            >
              <DumplingIcon className="h-9 w-9 drop-shadow-[0_0_12px_rgba(245,183,0,0.3)] sm:h-12 sm:w-12" />
            </motion.div>

            <div className="absolute -bottom-8 -right-6 sm:-bottom-10 sm:-right-10">
              <StampBadge className="h-24 w-24 sm:h-32 sm:w-32" center={"HOT &\nFRESH"} />
            </div>
          </motion.div>
        </motion.div>
      </motion.div>

      <div className="relative z-10 mt-auto">
        <MarqueeStrip />
      </div>
    </section>
  );
}
