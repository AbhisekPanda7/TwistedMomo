import { motion } from "framer-motion";
import Container from "../ui/Container";
import SectionHeading from "../ui/SectionHeading";
import { DumplingIcon, FlameIcon } from "../ui/Icons";
import { signatureSpecials } from "../../lib/menuData";
import { ButtonLink } from "../ui/Button";
import { staggerContainer, staggerItem, viewportOnce } from "../../lib/motion";

export default function FeaturedSpecials() {
  return (
    <section className="relative bg-ink-950 py-24 sm:py-32">
      <Container>
        <div className="flex flex-col items-start justify-between gap-6 sm:flex-row sm:items-end">
          <SectionHeading kicker="Fan Favourites" title="Signature" highlight="Specials" />
          <p className="max-w-xs font-sans text-sm text-paper-200/60">
            The four twists our regulars can't stop ordering. Start here if it's your first time.
          </p>
        </div>

        <motion.div
          initial="hidden"
          whileInView="show"
          viewport={viewportOnce}
          variants={staggerContainer(0.1)}
          className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4"
        >
          {signatureSpecials.map((s) => (
            <motion.div
              key={s.id}
              variants={staggerItem}
              whileHover={{ y: -8 }}
              transition={{ type: "spring", stiffness: 260, damping: 22 }}
              className="card-elevate group relative flex flex-col overflow-hidden rounded-3xl border border-ink-600 bg-ink-900 p-6 transition-colors duration-300 hover:border-gold-400/30"
            >
              <div className="absolute right-0 top-0 h-24 w-24 -translate-y-8 translate-x-8 rounded-full bg-gold-400/10 blur-2xl transition-all duration-500 group-hover:bg-gold-400/25" />

              <div className="flex items-center justify-between">
                <span className="rounded-full border border-gold-400/40 px-3 py-1 font-sans text-[10px] font-bold uppercase tracking-widest text-gold-400">
                  {s.badge}
                </span>
                <FlameIcon className="h-4 w-4 text-ember-500" aria-hidden="true" />
              </div>

              <div className="my-8 flex h-28 items-center justify-center">
                <motion.div
                  whileHover={{ rotate: 8, scale: 1.1 }}
                  transition={{ type: "spring", stiffness: 250, damping: 12 }}
                  className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-gold-300 to-gold-600 text-ink-950 shadow-[0_10px_30px_-8px_rgba(245,183,0,0.6)]"
                >
                  <DumplingIcon className="h-11 w-11" aria-hidden="true" />
                </motion.div>
              </div>

              <h3 className="font-display text-xl uppercase tracking-wide text-paper-50">{s.name}</h3>
              <p className="mt-2 flex-1 font-sans text-sm leading-relaxed text-paper-200/60">{s.desc}</p>

              <div className="mt-6 flex items-center justify-between border-t border-ink-600 pt-4">
                <span className="font-display text-2xl text-gold-400">₹{s.price}</span>
                <ButtonLink to="/menu" variant="ghost" magnetic={false} className="!px-0 !py-0 text-xs">
                  Order →
                </ButtonLink>
              </div>
            </motion.div>
          ))}
        </motion.div>
      </Container>
    </section>
  );
}
