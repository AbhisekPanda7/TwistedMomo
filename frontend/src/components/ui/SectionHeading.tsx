import { motion } from "framer-motion";

export default function SectionHeading({
  kicker,
  title,
  highlight,
  align = "left",
  dark = false,
  desc,
}: {
  kicker: string;
  title: string;
  highlight?: string;
  align?: "left" | "center";
  dark?: boolean;
  desc?: string;
}) {
  return (
    <div className={align === "center" ? "text-center" : "text-left"}>
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.5 }}
        className={`mb-3 flex items-center gap-3 ${align === "center" ? "justify-center" : ""}`}
      >
        <span className="h-[2px] w-8 bg-gold-400" />
        <span className="font-sans text-xs font-bold uppercase tracking-[0.3em] text-gold-400">
          {kicker}
        </span>
        {align === "center" && <span className="h-[2px] w-8 bg-gold-400" />}
      </motion.div>
      <motion.h2
        initial={{ opacity: 0, y: 24 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.6, delay: 0.05 }}
        className={`font-display text-4xl uppercase leading-[0.95] sm:text-5xl lg:text-6xl ${
          dark ? "text-ink-950" : "text-paper-50"
        }`}
      >
        {title} {highlight && <span className="text-gold-400">{highlight}</span>}
      </motion.h2>
      {desc && (
        <motion.p
          initial={{ opacity: 0, y: 16 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: "-80px" }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className={`mt-4 max-w-xl font-sans text-base leading-relaxed ${
            dark ? "text-ink-700" : "text-paper-200/80"
          } ${align === "center" ? "mx-auto" : ""}`}
        >
          {desc}
        </motion.p>
      )}
    </div>
  );
}
