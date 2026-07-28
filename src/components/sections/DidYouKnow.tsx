import { motion } from "framer-motion";
import Container from "../ui/Container";

const facts = [
  {
    tag: "Did You Know?",
    title: "600 years in the making",
    body: "The word “Momo” traces back to Tibetan cuisine, believed to have originated in the Himalayan region over 600 years ago.",
    rotate: -3,
  },
  {
    tag: "TM Report",
    title: "Twisted, not traditional",
    body: "We took the classic momo and gave it a street-food makeover — kurkure crunch, tandoori smoke, and chilli butter garlic heat.",
    rotate: 2,
  },
  {
    tag: "Boozi's Verdict",
    title: "Facts are good. Fresh momos are better.",
    body: "Every momo is folded fresh, steamed or fried to order — nothing sits around waiting for you. That's the twist.",
    rotate: -1.5,
  },
];

export default function DidYouKnow() {
  return (
    <section className="relative overflow-hidden bg-ink-900 py-24 sm:py-32">
      <div className="pointer-events-none absolute inset-0 diagonal-stripes opacity-[0.03]" />

      <Container className="relative">
        <div className="mx-auto mb-14 max-w-2xl text-center">
          <span className="font-marker text-gold-400 text-sm">Twisted Momo Times — Est. 2022</span>
          <h2 className="mt-2 font-masthead text-4xl font-black uppercase text-paper-50 sm:text-5xl">
            Did You Know?
          </h2>
        </div>

        <div className="grid gap-8 sm:grid-cols-3">
          {facts.map((f, i) => (
            <motion.div
              key={f.title}
              initial={{ opacity: 0, y: 40, rotate: 0 }}
              whileInView={{ opacity: 1, y: 0, rotate: f.rotate }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.6, delay: i * 0.1 }}
              whileHover={{ rotate: 0, scale: 1.03 }}
              className="relative bg-paper-100 p-6 text-ink-950 shadow-[0_20px_40px_-15px_rgba(0,0,0,0.6)]"
              style={{ clipPath: "polygon(0 0,100% 0,100% 100%,0 100%)" }}
            >
              <div className="absolute -top-3 left-6 rotate-[-4deg] rounded-sm bg-chili-500 px-3 py-1 font-sans text-[10px] font-bold uppercase tracking-widest text-paper-50 shadow">
                {f.tag}
              </div>
              <h3 className="mt-3 font-masthead text-2xl font-bold leading-tight">{f.title}</h3>
              <p className="mt-3 font-sans text-sm leading-relaxed text-ink-800/75">{f.body}</p>
              <div className="mt-5 border-t border-dashed border-ink-950/20 pt-3 font-sans text-[10px] uppercase tracking-widest text-ink-800/40">
                Twisted Flavours &middot; Fresh Ingredients &middot; Made With Love
              </div>
            </motion.div>
          ))}
        </div>
      </Container>
    </section>
  );
}
