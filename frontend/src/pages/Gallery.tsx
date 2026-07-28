import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import PhotoTile from "../components/ui/PhotoTile";
import { DumplingIcon } from "../components/ui/Icons";

type Item = { id: string; label: string; cat: "Food" | "Kitchen" | "Vibes" | "Mascot"; big?: boolean };

const items: Item[] = [
  { id: "1", label: "Chilli Butter Garlic", cat: "Food", big: true },
  { id: "2", label: "Kurkure Crunch", cat: "Food" },
  { id: "3", label: "Boozi On The Bike", cat: "Mascot" },
  { id: "4", label: "Tandoori Fire", cat: "Food" },
  { id: "5", label: "Fresh Folds", cat: "Kitchen" },
  { id: "6", label: "Steam Rising", cat: "Kitchen", big: true },
  { id: "7", label: "Friday Night Rush", cat: "Vibes" },
  { id: "8", label: "Afghani Creamy", cat: "Food" },
  { id: "9", label: "Schezwan Heat", cat: "Food" },
  { id: "10", label: "The Sauce Station", cat: "Kitchen" },
  { id: "11", label: "Boozi's Verdict", cat: "Mascot", big: true },
  { id: "12", label: "Late Night Regulars", cat: "Vibes" },
  { id: "13", label: "Corn Cheese Special", cat: "Food" },
  { id: "14", label: "The Twisted Cart", cat: "Vibes" },
];

const filters = ["All", "Food", "Kitchen", "Vibes", "Mascot"] as const;

export default function Gallery() {
  const [filter, setFilter] = useState<(typeof filters)[number]>("All");
  const [activeId, setActiveId] = useState<string | null>(null);
  const shown = filter === "All" ? items : items.filter((i) => i.cat === filter);
  const activeIndex = shown.findIndex((i) => i.id === activeId);
  const active = activeIndex >= 0 ? shown[activeIndex] : null;

  useEffect(() => {
    if (!active) return;
    document.body.style.overflow = "hidden";
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setActiveId(null);
      if (e.key === "ArrowRight") setActiveId(shown[(activeIndex + 1) % shown.length].id);
      if (e.key === "ArrowLeft") setActiveId(shown[(activeIndex - 1 + shown.length) % shown.length].id);
    };
    window.addEventListener("keydown", onKey);
    return () => {
      document.body.style.overflow = "";
      window.removeEventListener("keydown", onKey);
    };
  }, [active, activeIndex, shown]);

  return (
    <>
      <PageHero
        kicker="The Gallery"
        title={
          <>
            Twisted, <span className="text-gold-400">Captured</span>
          </>
        }
        description="A peek behind the counter — food, kitchen, late-night vibes, and Boozi being Boozi."
      />

      <Container className="pb-24 sm:pb-32">
        <div className="mb-10 flex flex-wrap justify-center gap-2">
          {filters.map((f) => (
            <motion.button
              key={f}
              onClick={() => setFilter(f)}
              whileHover={{ scale: 1.06 }}
              whileTap={{ scale: 0.95 }}
              data-cursor-hover
              className={`relative rounded-full px-5 py-2 font-sans text-xs font-bold uppercase tracking-wider transition-colors ${
                filter === f ? "text-ink-950" : "text-paper-200/60 hover:text-gold-400"
              }`}
            >
              {filter === f && (
                <motion.span
                  layoutId="gallery-pill"
                  className="absolute inset-0 rounded-full bg-gold-400"
                  transition={{ type: "spring", stiffness: 400, damping: 30 }}
                />
              )}
              <span className="relative">{f}</span>
            </motion.button>
          ))}
        </div>

        {shown.length > 0 ? (
          <motion.div layout className="grid auto-rows-[160px] grid-cols-2 gap-4 sm:auto-rows-[180px] sm:grid-cols-4">
            <AnimatePresence mode="popLayout">
              {shown.map((item, i) => (
                <motion.div
                  key={item.id}
                  layout
                  initial={{ opacity: 0, scale: 0.85 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.85 }}
                  transition={{ duration: 0.35, delay: i * 0.03 }}
                  className={item.big ? "col-span-2 row-span-2" : ""}
                >
                  <PhotoTile
                    label={item.label}
                    variant={i}
                    big={item.big}
                    className="h-full w-full"
                    onClick={() => setActiveId(item.id)}
                  />
                </motion.div>
              ))}
            </AnimatePresence>
          </motion.div>
        ) : (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="flex flex-col items-center gap-3 py-24 text-center"
          >
            <DumplingIcon className="h-10 w-10 text-gold-400/40" />
            <p className="font-sans text-sm text-paper-200/50">Nothing here yet — check another category.</p>
          </motion.div>
        )}
      </Container>

      <AnimatePresence>
        {active && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[80] flex items-center justify-center bg-ink-950/90 backdrop-blur-md p-6"
            onClick={() => setActiveId(null)}
          >
            <motion.div
              key={active.id}
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.92 }}
              transition={{ type: "spring", stiffness: 300, damping: 28 }}
              onClick={(e) => e.stopPropagation()}
              className="relative aspect-square w-full max-w-lg"
            >
              <PhotoTile label={active.label} variant={activeIndex} big className="h-full w-full" />

              <span className="absolute -top-3 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-gold-400 px-4 py-1 font-sans text-[10px] font-bold uppercase tracking-widest text-ink-950">
                {active.cat}
              </span>

              <button
                onClick={() => setActiveId(null)}
                aria-label="Close"
                data-cursor-hover
                className="absolute -right-3 -top-3 flex h-9 w-9 items-center justify-center rounded-full bg-ink-950 text-gold-400 ring-2 ring-gold-400/40 transition-transform hover:scale-110"
              >
                ✕
              </button>

              <button
                onClick={() => setActiveId(shown[(activeIndex - 1 + shown.length) % shown.length].id)}
                aria-label="Previous image"
                data-cursor-hover
                className="absolute left-3 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-gold-400/30 bg-ink-950/60 text-gold-400 backdrop-blur-sm transition-colors hover:bg-gold-400 hover:text-ink-950 lg:-left-14 lg:bg-transparent lg:backdrop-blur-none"
              >
                ←
              </button>
              <button
                onClick={() => setActiveId(shown[(activeIndex + 1) % shown.length].id)}
                aria-label="Next image"
                data-cursor-hover
                className="absolute right-3 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-gold-400/30 bg-ink-950/60 text-gold-400 backdrop-blur-sm transition-colors hover:bg-gold-400 hover:text-ink-950 lg:-right-14 lg:bg-transparent lg:backdrop-blur-none"
              >
                →
              </button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
