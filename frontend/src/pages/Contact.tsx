import { useState, type FormEvent } from "react";
import { AnimatePresence, motion } from "framer-motion";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import { Button } from "../components/ui/Button";
import { MotorcycleIcon } from "../components/ui/Icons";
import { staggerContainer, staggerItem, viewportOnce } from "../lib/motion";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-900 px-4 py-3 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";

type Status = "idle" | "sending" | "sent";

export default function Contact() {
  const [status, setStatus] = useState<Status>("idle");

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setStatus("sending");
    window.setTimeout(() => setStatus("sent"), 900);
  }

  return (
    <>
      <PageHero
        kicker="Get In Touch"
        title={
          <>
            Let's Talk <span className="text-gold-400">Momos</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        <div className="grid gap-10 lg:grid-cols-[1fr_1.2fr]">
          <motion.div
            initial="hidden"
            whileInView="show"
            viewport={viewportOnce}
            variants={staggerContainer(0.1)}
            className="space-y-6"
          >
            <motion.div
              variants={staggerItem}
              whileHover={{ y: -4 }}
              transition={{ type: "spring", stiffness: 260, damping: 22 }}
              className="card-elevate rounded-3xl border border-ink-600 bg-ink-900 p-7"
            >
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Visit</h3>
              <p className="mt-3 font-sans text-sm leading-relaxed text-paper-100/80">
                Twisted Momos HQ<br />MG Road, City Center<br />500001
              </p>
            </motion.div>
            <motion.div
              variants={staggerItem}
              whileHover={{ y: -4 }}
              transition={{ type: "spring", stiffness: 260, damping: 22 }}
              className="card-elevate rounded-3xl border border-ink-600 bg-ink-900 p-7"
            >
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Hours</h3>
              <ul className="mt-3 space-y-1.5 font-sans text-sm text-paper-100/80">
                <li className="flex justify-between"><span>Mon – Fri</span><span>12pm – 11pm</span></li>
                <li className="flex justify-between"><span>Sat – Sun</span><span>12pm – 1am</span></li>
              </ul>
            </motion.div>
            <motion.div
              variants={staggerItem}
              whileHover={{ y: -4 }}
              transition={{ type: "spring", stiffness: 260, damping: 22 }}
              className="card-elevate rounded-3xl border border-ink-600 bg-ink-900 p-7"
            >
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">Contact</h3>
              <p className="mt-3 space-y-1 font-sans text-sm text-paper-100/80">
                <a href="tel:+910000000000" className="block hover:text-gold-400">+91 00000 00000</a>
                <a href="mailto:hello@twistedmomos.com" className="block hover:text-gold-400">
                  hello@twistedmomos.com
                </a>
              </p>
            </motion.div>

            <motion.div
              variants={staggerItem}
              className="relative aspect-[4/3] overflow-hidden rounded-3xl border border-ink-600 bg-ink-950"
            >
              <div
                className="absolute inset-0 opacity-40"
                style={{
                  backgroundImage:
                    "linear-gradient(rgba(245,183,0,0.15) 1px, transparent 1px), linear-gradient(90deg, rgba(245,183,0,0.15) 1px, transparent 1px)",
                  backgroundSize: "28px 28px",
                }}
              />
              <div className="absolute inset-0 flex items-center justify-center">
                <span className="absolute h-14 w-14 animate-pulse-glow rounded-full bg-gold-400/30 blur-md" />
                <div className="relative flex h-14 w-14 items-center justify-center rounded-full bg-gold-400 text-ink-950 shadow-[0_0_40px_rgba(245,183,0,0.5)]">
                  <MotorcycleIcon className="h-7 w-7" />
                </div>
              </div>
            </motion.div>
          </motion.div>

          <Reveal delay={0.1}>
            <div className="card-elevate relative overflow-hidden rounded-3xl border border-ink-600 bg-ink-900 p-8 sm:p-10">
              <AnimatePresence mode="wait">
                {status === "sent" ? (
                  <motion.div
                    key="success"
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex min-h-[380px] flex-col items-center justify-center text-center"
                  >
                    <motion.div
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      transition={{ type: "spring", stiffness: 250, damping: 15 }}
                      className="flex h-16 w-16 items-center justify-center rounded-full bg-gold-400 text-3xl text-ink-950"
                    >
                      ✓
                    </motion.div>
                    <h3 className="mt-6 font-display text-2xl uppercase text-paper-50">Message Sent</h3>
                    <p className="mt-2 max-w-xs font-sans text-sm text-paper-200/60">
                      We'll get back to you faster than a momo leaves the steamer.
                    </p>
                    <button
                      onClick={() => setStatus("idle")}
                      data-cursor-hover
                      className="mt-6 font-sans text-xs font-bold uppercase tracking-widest text-gold-400 hover:underline"
                    >
                      Send another message
                    </button>
                  </motion.div>
                ) : (
                  <motion.form
                    key="form"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    onSubmit={handleSubmit}
                    className="space-y-5"
                  >
                    <div className="grid gap-5 sm:grid-cols-2">
                      <div>
                        <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                          Name
                        </label>
                        <input required className={inputClass} placeholder="Your name" />
                      </div>
                      <div>
                        <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                          Phone
                        </label>
                        <input className={inputClass} placeholder="+91 00000 00000" />
                      </div>
                    </div>
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        Email
                      </label>
                      <input required type="email" className={inputClass} placeholder="you@email.com" />
                    </div>
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        What's the occasion?
                      </label>
                      <div className="flex flex-wrap gap-2">
                        {["Feedback", "Bulk Order", "Catering", "Just Saying Hi"].map((tag) => (
                          <label key={tag} className="cursor-pointer" data-cursor-hover>
                            <input type="radio" name="reason" value={tag} className="peer sr-only" />
                            <span className="block rounded-full border border-ink-600 px-4 py-2 font-sans text-xs font-semibold text-paper-200/70 transition-colors peer-checked:border-gold-400 peer-checked:bg-gold-400 peer-checked:text-ink-950">
                              {tag}
                            </span>
                          </label>
                        ))}
                      </div>
                    </div>
                    <div>
                      <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                        Message
                      </label>
                      <textarea required rows={4} className={inputClass} placeholder="Tell us what's on your mind..." />
                    </div>
                    <Button type="submit" className="w-full" disabled={status === "sending"}>
                      {status === "sending" ? (
                        <motion.span
                          animate={{ opacity: [1, 0.4, 1] }}
                          transition={{ duration: 1, repeat: Infinity }}
                        >
                          Sending…
                        </motion.span>
                      ) : (
                        "Send Message"
                      )}
                    </Button>
                  </motion.form>
                )}
              </AnimatePresence>
            </div>
          </Reveal>
        </div>
      </Container>
    </>
  );
}
