import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import Logo from "../ui/Logo";
import Container from "../ui/Container";
import { FacebookIcon, InstagramIcon, WhatsAppIcon } from "../ui/Icons";

const explore = [
  { to: "/menu", label: "Full Menu" },
  { to: "/about", label: "Our Story" },
  { to: "/gallery", label: "Gallery" },
  { to: "/contact", label: "Find Us" },
];

const social = [
  {
    label: "Instagram",
    href: "https://www.instagram.com/twisted.momos?utm_source=qr",
    ariaLabel: "Visit Twisted Momos on Instagram",
    icon: InstagramIcon,
  },
  { label: "Facebook", href: "https://facebook.com", icon: FacebookIcon },
  { label: "WhatsApp", href: "https://wa.me/910000000000", icon: WhatsAppIcon },
];

export default function Footer() {
  return (
    <footer className="relative overflow-hidden border-t border-ink-600 bg-ink-950 pt-20">
      <div className="pointer-events-none absolute -top-24 right-[-6rem] h-72 w-72 rounded-full bg-gold-400/10 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-32 left-[-4rem] h-72 w-72 rounded-full bg-ember-500/10 blur-3xl" />

      <Container className="relative">
        <div className="grid gap-12 pb-16 lg:grid-cols-[1.4fr_1fr_1fr_1.2fr]">
          <div>
            <div className="flex items-center gap-3">
              <Logo className="h-16 w-16" />
              <div className="font-display text-2xl leading-none tracking-wide text-paper-50">
                TWISTED <span className="text-gold-400">MOMOS</span>
              </div>
            </div>
            <p className="mt-5 max-w-xs font-sans text-sm leading-relaxed text-paper-200/70">
              Twisting flavours since 2022. Bold street-food momos, cooked loud and served proud.
              No boring bites here.
            </p>
            <div className="mt-6 flex items-center gap-3">
              {social.map((s) => (
                <motion.a
                  key={s.label}
                  href={s.href}
                  target="_blank"
                  rel="noopener noreferrer"
                  whileHover={{ y: -3, scale: 1.08 }}
                  whileTap={{ scale: 0.92 }}
                  data-cursor-hover
                  className="flex h-10 w-10 items-center justify-center rounded-full border border-gold-400/40 text-gold-400 transition-colors hover:bg-gold-400 hover:text-ink-950"
                  aria-label={s.ariaLabel ?? s.label}
                >
                  <s.icon className="h-4 w-4" />
                </motion.a>
              ))}
            </div>
          </div>

          <div>
            <h4 className="font-display text-sm uppercase tracking-[0.25em] text-gold-400">Explore</h4>
            <ul className="mt-5 space-y-3">
              {explore.map((e) => (
                <li key={e.to}>
                  <Link to={e.to} className="font-sans text-sm text-paper-100/80 transition-colors hover:text-gold-400">
                    {e.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="font-display text-sm uppercase tracking-[0.25em] text-gold-400">Hours</h4>
            <ul className="mt-5 space-y-2 font-sans text-sm text-paper-100/80">
              <li className="flex justify-between gap-6"><span>Mon – Fri</span><span>12pm – 11pm</span></li>
              <li className="flex justify-between gap-6"><span>Sat – Sun</span><span>12pm – 1am</span></li>
              <li className="mt-3 text-gold-400/90">Kitchen never sleeps twisted.</li>
            </ul>
          </div>

          <div>
            <h4 className="font-display text-sm uppercase tracking-[0.25em] text-gold-400">Visit</h4>
            <address className="mt-5 space-y-2 font-sans text-sm not-italic text-paper-100/80">
              <p>Twisted Momos HQ, MG Road,<br />City Center, 500001</p>
              <p>
                <a href="tel:+910000000000" className="hover:text-gold-400">+91 00000 00000</a>
              </p>
              <p>
                <a href="mailto:hello@twistedmomos.com" className="hover:text-gold-400">
                  hello@twistedmomos.com
                </a>
              </p>
            </address>
          </div>
        </div>

        <div className="flex flex-col items-center justify-between gap-4 border-t border-ink-600 py-6 text-center font-sans text-xs text-paper-200/50 sm:flex-row sm:text-left">
          <p>© {new Date().getFullYear()} Twisted Momos. All flavours twisted, all rights reserved.</p>
          <p>Made with chilli, butter, and a whole lot of attitude.</p>
        </div>
      </Container>

      <div aria-hidden="true" className="overflow-hidden border-t border-gold-400/20 bg-ink-900 py-3">
        <div className="animate-marquee flex w-max gap-8 font-display text-3xl uppercase text-ink-700 sm:text-4xl">
          {Array.from({ length: 10 }).map((_, i) => (
            <span key={i}>Twisted Momos</span>
          ))}
        </div>
      </div>
    </footer>
  );
}
