import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import Container from "../ui/Container";
import SectionHeading from "../ui/SectionHeading";
import { menuCategories } from "../../lib/menuData";
import { DumplingIcon } from "../ui/Icons";
import { staggerContainer, staggerItem, viewportOnce } from "../../lib/motion";

export default function MenuCategoriesPreview() {
  return (
    <section className="relative bg-ink-950 py-24 sm:py-32">
      <Container>
        <SectionHeading kicker="The Full Spread" title="Menu" highlight="Categories" align="center" />

        <motion.div
          initial="hidden"
          whileInView="show"
          viewport={viewportOnce}
          variants={staggerContainer(0.07)}
          className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-4"
        >
          {menuCategories.map((cat) => {
            const prices = cat.items.map((it) => it.price);
            return (
              <motion.div key={cat.id} variants={staggerItem} whileHover={{ y: -6 }} transition={{ type: "spring", stiffness: 260, damping: 22 }}>
                <Link
                  to={`/menu#${cat.id}`}
                  data-cursor-hover
                  className="card-elevate group relative flex h-full flex-col overflow-hidden rounded-2xl border border-ink-600 bg-ink-900 p-6 transition-colors duration-300 hover:border-gold-400/60"
                >
                  <div className="pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full bg-gold-400/0 blur-2xl transition-all duration-500 group-hover:bg-gold-400/20" />

                  <DumplingIcon
                    className="h-8 w-8 text-gold-400 transition-transform duration-500 group-hover:-rotate-12 group-hover:scale-110"
                    aria-hidden="true"
                  />

                  <div className="relative mt-5 inline-block w-fit -rotate-1">
                    <span className="absolute inset-x-[-6px] inset-y-[-2px] -skew-x-3 rounded bg-gold-400/90" />
                    <h3 className="relative font-marker text-lg leading-tight text-ink-950">{cat.title}</h3>
                  </div>

                  <p className="mt-3 flex-1 font-sans text-sm text-paper-200/60">{cat.subtitle}</p>

                  <div className="mt-5 flex items-center justify-between border-t border-ink-600 pt-4 font-sans text-sm">
                    <span className="text-paper-200/50">{cat.items.length} items</span>
                    <span className="font-bold text-gold-400">
                      ₹{Math.min(...prices)}–{Math.max(...prices)}
                    </span>
                  </div>
                </Link>
              </motion.div>
            );
          })}
        </motion.div>
      </Container>
    </section>
  );
}
