import { motion } from "framer-motion";
import Container from "../ui/Container";
import SectionHeading from "../ui/SectionHeading";
import { Button } from "../ui/Button";
import { googleMapsDirectionsUrl, googleMapsViewUrl, restaurantLocation } from "../../lib/location";

/** Golden map-pin glyph for the custom location preview below. */
function LocationPin() {
  return (
    <svg
      viewBox="0 0 24 24"
      className="relative z-10 h-9 w-9 fill-gold-400 drop-shadow-[0_6px_16px_rgba(245,183,0,0.65)]"
      aria-hidden="true"
    >
      <path d="M12 2C7.58 2 4 5.58 4 10c0 5.7 7 11.2 7.3 11.44a1.1 1.1 0 0 0 1.4 0C13 21.2 20 15.7 20 10c0-4.42-3.58-8-8-8Zm0 11a3 3 0 1 1 0-6 3 3 0 0 1 0 6Z" />
    </svg>
  );
}

export default function LocationCTA() {
  function openDirections() {
    window.open(googleMapsDirectionsUrl, "_blank", "noopener,noreferrer");
  }

  return (
    <section className="relative bg-ink-900 py-24 sm:py-32">
      <Container>
        <div className="grid items-center gap-12 lg:grid-cols-2">
          <div>
            <SectionHeading kicker="Come Find Us" title="Twisted Is" highlight="Nearby" />
            <p className="mt-5 max-w-md font-sans text-sm leading-relaxed text-paper-200/70">
              One stop, city center. Boozi's parked outside most nights — follow the smoke,
              you'll find the momos.
            </p>

            <div className="mt-8 space-y-4 font-sans text-sm text-paper-100/85">
              <div className="flex items-start gap-3">
                <span className="mt-0.5 h-2 w-2 shrink-0 rounded-full bg-gold-400" />
                <span>{restaurantLocation.address}</span>
              </div>
              <div className="flex items-start gap-3">
                <span className="mt-0.5 h-2 w-2 shrink-0 rounded-full bg-gold-400" />
                <span>Mon–Fri 12pm–11pm · Sat–Sun 12pm–1am</span>
              </div>
              <div className="flex items-start gap-3">
                <span className="mt-0.5 h-2 w-2 shrink-0 rounded-full bg-gold-400" />
                <span>+91 00000 00000</span>
              </div>
            </div>

            <div className="mt-9">
              <Button onClick={openDirections}>Get Directions</Button>
            </div>
          </div>

          <motion.a
            href={googleMapsViewUrl}
            target="_blank"
            rel="noopener noreferrer"
            aria-label={`Open ${restaurantLocation.name} location in Google Maps`}
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true, margin: "-60px" }}
            transition={{ duration: 0.6 }}
            whileHover={{
              scale: 1.02,
              boxShadow: "0 0 60px -6px rgba(245,183,0,0.5)",
              transition: { duration: 0.4, ease: "easeOut" },
            }}
            style={{ boxShadow: "0 0 0px 0px rgba(245,183,0,0)" }}
            className="group relative block aspect-square cursor-pointer overflow-hidden rounded-3xl border border-ink-600 bg-ink-950 sm:aspect-[4/3]"
          >
            {/* ambient atmosphere */}
            <div
              className="pointer-events-none absolute inset-0"
              style={{
                background:
                  "radial-gradient(circle at 50% 50%, rgba(245,183,0,0.12), transparent 60%), radial-gradient(circle at 50% 50%, transparent 45%, rgba(0,0,0,0.65) 100%)",
              }}
            />

            {/* subtle street grid */}
            <div
              className="pointer-events-none absolute inset-0 opacity-[0.16]"
              style={{
                backgroundImage:
                  "linear-gradient(rgba(245,183,0,0.6) 1px, transparent 1px), linear-gradient(90deg, rgba(245,183,0,0.6) 1px, transparent 1px)",
                backgroundSize: "34px 34px",
              }}
            />

            {/* wide "avenues" for a hand-drawn map feel */}
            <div className="pointer-events-none absolute inset-0 opacity-[0.16]">
              <div className="absolute left-[-10%] top-[28%] h-px w-[120%] -rotate-6 bg-gradient-to-r from-transparent via-gold-400 to-transparent" />
              <div className="absolute left-[-10%] top-[70%] h-px w-[120%] rotate-3 bg-gradient-to-r from-transparent via-gold-400 to-transparent" />
              <div className="absolute left-[24%] top-[-10%] h-[120%] w-px rotate-6 bg-gradient-to-b from-transparent via-gold-400 to-transparent" />
              <div className="absolute left-[76%] top-[-10%] h-[120%] w-px -rotate-3 bg-gradient-to-b from-transparent via-gold-400 to-transparent" />
            </div>

            <div className="grain pointer-events-none absolute inset-0 opacity-60" />

            {/* pin + label cluster */}
            <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
              <div className="relative flex flex-col items-center">
                <span className="absolute -top-11 whitespace-nowrap rounded-full border border-gold-400/40 bg-ink-950/90 px-3 py-1 font-sans text-[10px] font-bold uppercase tracking-widest text-gold-400 shadow-lg">
                  {restaurantLocation.name}
                </span>

                <span className="absolute h-16 w-16 animate-pulse-glow rounded-full bg-gold-400/25 blur-xl" />
                <motion.span
                  className="absolute h-10 w-10 rounded-full border-2 border-gold-400/60"
                  animate={{ scale: [1, 2.4], opacity: [0.6, 0] }}
                  transition={{ duration: 2.2, repeat: Infinity, ease: "easeOut" }}
                />
                <LocationPin />
              </div>
            </div>

            {/* open-in-maps overlay */}
            <div className="absolute bottom-4 right-4 flex items-center gap-1.5 rounded-full border border-gold-400/30 bg-ink-950/70 px-3 py-1.5 font-sans text-[10px] font-bold uppercase tracking-wider text-gold-400/90 backdrop-blur-sm transition-all duration-300 group-hover:border-gold-400/60 group-hover:bg-ink-950/90 group-hover:text-gold-300">
              <span aria-hidden="true">📍</span> Open in Google Maps
            </div>
          </motion.a>
        </div>
      </Container>
    </section>
  );
}
