import { motion } from "framer-motion";
import Container from "../components/ui/Container";
import SectionHeading from "../components/ui/SectionHeading";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import BooziMascot from "../components/ui/BooziMascot";
import StampBadge from "../components/ui/StampBadge";
import { ButtonLink } from "../components/ui/Button";
import { ChiliIcon, DumplingIcon, FlameIcon, LeafIcon } from "../components/ui/Icons";
import MarqueeStrip from "../components/ui/MarqueeStrip";
import { staggerContainer, staggerItem, viewportOnce } from "../lib/motion";

const timeline = [
  { year: "2022", title: "The First Fold", body: "Twisted Momos opens as a single street cart with four flavours and one loud attitude." },
  { year: "2023", title: "The Twist Expands", body: "Kurkure, Tandoori and Schezwan momos join the menu. Boozi becomes the face of the brand." },
  { year: "2024", title: "TM Times Launches", body: "Our tabloid-style poster drops — 'The City's Best Momo' — and regulars start collecting them." },
  { year: "2025", title: "Cult Following", body: "40+ twisted recipes, a line out the door on weekends, and zero plans to play it safe." },
];

const values = [
  { icon: FlameIcon, title: "Bold, Always", body: "If a flavour doesn't make you sit up, it doesn't make the menu." },
  { icon: LeafIcon, title: "Fresh, Never Frozen", body: "Dough folded daily, sauces made from scratch, nothing sitting in a freezer." },
  { icon: ChiliIcon, title: "Heat With Purpose", body: "Spice that builds flavour, not just pain. Every chilli earns its place." },
  { icon: DumplingIcon, title: "Street Food, Elevated", body: "Cart energy, restaurant-level consistency. Best of both worlds." },
];

export default function About() {
  return (
    <>
      <PageHero
        kicker="Our Story"
        title={
          <>
            Twisted By Name, <span className="text-gold-400">Delicious</span> By Nature
          </>
        }
        description="We're not just serving momos. We're creating an experience — one wrapped in attitude, folded with love, and finished with a twist you won't forget."
      />

      <MarqueeStrip text="EST. 2022" />

      {/* Origin story */}
      <section className="relative bg-ink-950 py-24 sm:py-32">
        <Container className="grid items-center gap-14 lg:grid-cols-2">
          <Reveal>
            <SectionHeading kicker="How It Started" title="One Cart," highlight="One Rule" />
            <p className="mt-6 max-w-lg font-sans text-base leading-relaxed text-paper-200/70">
              Twisted Momos began in 2022 with a simple frustration: every momo stall in the
              city tasted the same. So we broke the recipe apart and rebuilt it — kurkure
              shells, tandoori smoke, chilli butter garlic heat — and gave every batch a twist
              nobody else was doing.
            </p>
            <p className="mt-4 max-w-lg font-sans text-base leading-relaxed text-paper-200/70">
              The name stuck. The attitude stuck harder. And somewhere along the way, a
              German Shepherd in sunglasses became the unofficial face of the brand — because
              if you're going to be loud about flavour, you might as well look good doing it.
            </p>
            <div className="mt-8">
              <ButtonLink to="/menu">Taste The Twist</ButtonLink>
            </div>
          </Reveal>

          <Reveal delay={0.15} className="relative mx-auto w-full max-w-sm">
            <motion.div
              whileHover={{ y: -6 }}
              transition={{ type: "spring", stiffness: 260, damping: 22 }}
              className="card-elevate relative rounded-[2.5rem] border border-ink-600 bg-ink-900 p-8"
            >
              <BooziMascot className="mx-auto w-48" />
              <div className="mt-6 text-center">
                <p className="font-marker text-gold-400">"Facts are good.</p>
                <p className="font-marker text-gold-400">Fresh momos are better."</p>
                <p className="mt-3 font-sans text-xs uppercase tracking-widest text-paper-200/50">
                  — Boozi, Chief Flavour Officer
                </p>
              </div>
              <div className="absolute -right-6 -top-6">
                <StampBadge className="h-24 w-24" center={"BOOZI\nCFO"} />
              </div>
            </motion.div>
          </Reveal>
        </Container>
      </section>

      {/* Timeline */}
      <section className="relative bg-ink-900 py-24 sm:py-32">
        <Container>
          <SectionHeading kicker="The Journey" title="Twisted" highlight="Timeline" align="center" />

          <motion.div
            initial="hidden"
            whileInView="show"
            viewport={viewportOnce}
            variants={staggerContainer(0.12)}
            className="relative mt-16"
          >
            <div className="absolute left-1/2 top-0 hidden h-full w-px -translate-x-1/2 bg-ink-600 lg:block" />
            <div className="space-y-10 lg:space-y-0">
              {timeline.map((t, i) => (
                <motion.div
                  key={t.year}
                  variants={staggerItem}
                  className={`relative flex flex-col gap-4 lg:mb-10 lg:flex-row lg:items-center ${
                    i % 2 === 0 ? "" : "lg:flex-row-reverse"
                  }`}
                >
                  <div className={`lg:w-1/2 ${i % 2 === 0 ? "lg:pr-14 lg:text-right" : "lg:pl-14"}`}>
                    <motion.div
                      whileHover={{ y: -4 }}
                      transition={{ type: "spring", stiffness: 260, damping: 22 }}
                      className="card-elevate rounded-2xl border border-ink-600 bg-ink-950 p-6"
                    >
                      <span className="font-display text-3xl text-gold-400">{t.year}</span>
                      <h3 className="mt-2 font-sans text-lg font-bold text-paper-50">{t.title}</h3>
                      <p className="mt-2 font-sans text-sm leading-relaxed text-paper-200/60">{t.body}</p>
                    </motion.div>
                  </div>
                  <motion.div
                    initial={{ scale: 0 }}
                    whileInView={{ scale: 1 }}
                    viewport={viewportOnce}
                    transition={{ delay: 0.2 + i * 0.12, type: "spring", stiffness: 300 }}
                    className="absolute left-1/2 top-1/2 hidden h-3 w-3 -translate-x-1/2 -translate-y-1/2 rounded-full bg-gold-400 ring-4 ring-ink-900 lg:block"
                  />
                  <div className="hidden lg:block lg:w-1/2" />
                </motion.div>
              ))}
            </div>
          </motion.div>
        </Container>
      </section>

      {/* Values */}
      <section className="relative bg-ink-950 py-24 sm:py-32">
        <Container>
          <SectionHeading kicker="What We Stand For" title="Twisted" highlight="Values" align="center" />

          <motion.div
            initial="hidden"
            whileInView="show"
            viewport={viewportOnce}
            variants={staggerContainer(0.1)}
            className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4"
          >
            {values.map((v) => (
              <motion.div
                key={v.title}
                variants={staggerItem}
                whileHover={{ y: -6 }}
                transition={{ type: "spring", stiffness: 260, damping: 22 }}
                className="card-elevate group rounded-2xl border border-ink-600 bg-ink-900 p-7 text-center transition-colors duration-300 hover:border-gold-400/40"
              >
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-gold-400/10 text-gold-400 transition-transform duration-500 group-hover:scale-110 group-hover:rotate-6">
                  <v.icon className="h-6 w-6" aria-hidden="true" />
                </div>
                <h3 className="mt-5 font-display text-lg uppercase tracking-wide text-paper-50">{v.title}</h3>
                <p className="mt-2 font-sans text-sm leading-relaxed text-paper-200/60">{v.body}</p>
              </motion.div>
            ))}
          </motion.div>
        </Container>
      </section>
    </>
  );
}
