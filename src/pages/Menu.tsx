import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import MenuSection from "../components/sections/MenuSection";
import { menuCategories } from "../lib/menuData";
import MarqueeStrip from "../components/ui/MarqueeStrip";

export default function Menu() {
  const [active, setActive] = useState(menuCategories[0].id);
  const navRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) setActive(entry.target.id);
        });
      },
      { rootMargin: "-30% 0px -60% 0px", threshold: 0 }
    );
    menuCategories.forEach((c) => {
      const el = document.getElementById(c.id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const btn = navRef.current?.querySelector<HTMLElement>(`[data-id="${active}"]`);
    btn?.scrollIntoView({ behavior: "smooth", inline: "center", block: "nearest" });
  }, [active]);

  return (
    <>
      <PageHero
        kicker="The Full Menu"
        title={
          <>
            Pick Your <span className="text-gold-400">Twist</span>
          </>
        }
        description="9 categories, 40+ flavours, zero boring options. Everything's made fresh, folded daily, and priced to order twice."
      />

      <MarqueeStrip text="STEAM · FRIED · KURKURE · TANDOORI · SCHEZWAN · AFGHANI · C.B.G" />

      {/* sticky category nav */}
      <div className="sticky top-[66px] z-30 border-b border-ink-600 bg-ink-950/95 backdrop-blur-md sm:top-[78px]">
        <div ref={navRef} className="mx-auto flex max-w-7xl gap-2 overflow-x-auto px-5 py-3 sm:px-8 lg:px-12">
          {menuCategories.map((c) => (
            <motion.a
              key={c.id}
              href={`#${c.id}`}
              data-id={c.id}
              whileHover={{ scale: 1.06 }}
              whileTap={{ scale: 0.95 }}
              className={`relative shrink-0 whitespace-nowrap rounded-full px-4 py-2 font-sans text-xs font-bold uppercase tracking-wider transition-colors ${
                active === c.id ? "text-ink-950" : "text-paper-200/60 hover:text-gold-400"
              }`}
            >
              {active === c.id && (
                <motion.span
                  layoutId="menu-pill"
                  className="absolute inset-0 rounded-full bg-gold-400"
                  transition={{ type: "spring", stiffness: 400, damping: 30 }}
                />
              )}
              <span className="relative">{c.title.replace("'s", "")}</span>
            </motion.a>
          ))}
        </div>
      </div>

      <Container>
        {menuCategories.map((cat, i) => (
          <MenuSection key={cat.id} category={cat} index={i} />
        ))}
      </Container>

      <section className="bg-ink-900 py-16 text-center">
        <p className="font-sans text-xs uppercase tracking-widest text-paper-200/50">
          Prices in ₹ · Dine-in, takeaway & delivery available · Ask about our combo twists
        </p>
      </section>
    </>
  );
}
