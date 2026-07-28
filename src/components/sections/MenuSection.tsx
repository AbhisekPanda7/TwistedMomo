import { motion } from "framer-motion";
import type { MenuCategory } from "../../lib/menuData";
import { FlameIcon } from "../ui/Icons";

function VegDot({ veg }: { veg: boolean }) {
  return (
    <span
      title={veg ? "Vegetarian" : "Non-vegetarian"}
      className={`flex h-4 w-4 shrink-0 items-center justify-center border ${
        veg ? "border-green-500" : "border-chili-500"
      }`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${veg ? "bg-green-500" : "bg-chili-500"}`} />
      <span className="sr-only">{veg ? "Vegetarian" : "Non-vegetarian"}</span>
    </span>
  );
}

const tagStyles: Record<string, string> = {
  bestseller: "bg-gold-400 text-ink-950",
  signature: "bg-chili-500 text-paper-50",
  new: "bg-green-500 text-ink-950",
};

export default function MenuSection({ category, index }: { category: MenuCategory; index: number }) {
  return (
    <section id={category.id} className="scroll-mt-28 border-b border-ink-600 py-14 sm:py-16">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.5 }}
        className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between"
      >
        <div className="flex items-baseline gap-4">
          <span className="font-display text-3xl text-gold-400/40">
            {String(index + 1).padStart(2, "0")}
          </span>
          <div>
            <div className="relative inline-block w-fit -rotate-1">
              <span className="absolute inset-x-[-8px] inset-y-[-3px] -skew-x-3 rounded bg-gold-400" />
              <h2 className="relative font-marker text-2xl text-ink-950 sm:text-3xl">{category.title}</h2>
            </div>
            <p className="mt-2 font-sans text-sm text-paper-200/60">{category.subtitle}</p>
          </div>
        </div>
      </motion.div>

      <div className="mt-8 grid gap-x-10 gap-y-1 sm:grid-cols-2">
        {category.items.map((item, i) => (
          <motion.div
            key={item.name}
            initial={{ opacity: 0, x: -14 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, margin: "-40px" }}
            transition={{ duration: 0.4, delay: (i % 4) * 0.05 }}
            className="group relative flex items-center justify-between gap-4 overflow-hidden rounded-lg border-b border-ink-700 px-3 py-4 -mx-3 transition-colors duration-300 hover:bg-ink-900/60"
          >
            <span className="absolute left-0 top-0 h-full w-0.5 origin-top scale-y-0 bg-gold-400 transition-transform duration-300 group-hover:scale-y-100" />
            <div className="flex min-w-0 items-center gap-3">
              <VegDot veg={item.veg} />
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="truncate font-sans text-[15px] font-medium text-paper-50 transition-colors group-hover:text-gold-400">
                    {item.name}
                  </span>
                  {item.tag && (
                    <span className={`rounded-full px-2 py-0.5 font-sans text-[9px] font-bold uppercase tracking-wider ${tagStyles[item.tag]}`}>
                      {item.tag}
                    </span>
                  )}
                  {item.spicy && (
                    <span className="flex items-center gap-0.5 text-ember-500" aria-hidden="true">
                      {Array.from({ length: item.spicy }).map((_, s) => (
                        <FlameIcon key={s} className="h-3 w-3" />
                      ))}
                    </span>
                  )}
                </div>
              </div>
            </div>
            <span className="shrink-0 font-display text-lg text-gold-400 transition-transform duration-300 group-hover:scale-110">
              ₹{item.price}
            </span>
          </motion.div>
        ))}
      </div>
    </section>
  );
}
