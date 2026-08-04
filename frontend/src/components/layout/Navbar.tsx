import { useEffect, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { AnimatePresence, motion, useScroll, useSpring } from "framer-motion";
import Logo from "../ui/Logo";
import { ButtonLink } from "../ui/Button";
import { BagIcon } from "../ui/Icons";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import { hasRole } from "../../lib/tokenStorage";

const links = [
  { to: "/", label: "Home" },
  { to: "/menu", label: "Menu" },
  { to: "/about", label: "About" },
  { to: "/gallery", label: "Gallery" },
  { to: "/contact", label: "Contact" },
];

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);
  const { scrollYProgress } = useScroll();
  const progress = useSpring(scrollYProgress, { stiffness: 200, damping: 30, restDelta: 0.001 });
  const { user, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();
  const cartCount = cart?.totalItems ?? 0;

  function handleLogout() {
    logout();
    setOpen(false);
    navigate("/");
  }

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [open]);

  return (
    <>
      <header
        className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ${
          scrolled ? "bg-ink-950/90 backdrop-blur-md border-b border-ink-600 py-2" : "bg-transparent py-4"
        }`}
      >
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 sm:px-8 lg:px-12">
          <Link to="/" onClick={() => setOpen(false)} className="flex items-center gap-3 group">
            <Logo className="h-12 w-12 sm:h-14 sm:w-14 transition-transform duration-500 group-hover:rotate-12" />
            <span className="hidden font-display text-xl tracking-wide text-paper-50 sm:block">
              TWISTED <span className="text-gold-400">MOMOS</span>
            </span>
          </Link>

          <nav className="hidden items-center gap-1 lg:flex">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                className={({ isActive }) =>
                  `relative px-4 py-2 font-sans text-sm font-semibold uppercase tracking-wider transition-colors ${
                    isActive ? "text-gold-400" : "text-paper-100 hover:text-gold-400"
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {l.label}
                    {isActive && (
                      <motion.span
                        layoutId="nav-underline"
                        className="absolute -bottom-0.5 left-4 right-4 h-[2px] bg-gold-400"
                      />
                    )}
                  </>
                )}
              </NavLink>
            ))}
          </nav>

          <div className="hidden items-center gap-5 lg:flex">
            <Link to="/cart" data-cursor-hover aria-label="Cart" className="relative text-paper-100 transition-colors hover:text-gold-400">
              <BagIcon className="h-6 w-6" />
              {cartCount > 0 && (
                <span className="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-gold-400 px-1 font-sans text-[10px] font-bold text-ink-950">
                  {cartCount}
                </span>
              )}
            </Link>
            {user ? (
              <div className="flex items-center gap-4">
                <span className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/70">
                  Hi, {user.name.split(" ")[0]}
                </span>
                <Link
                  to="/orders"
                  data-cursor-hover
                  className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60 transition-colors hover:text-gold-400"
                >
                  Orders
                </Link>
                {hasRole(user, "ADMIN") && (
                  <Link
                    to="/admin"
                    data-cursor-hover
                    className="font-sans text-xs font-bold uppercase tracking-wider text-gold-400/80 transition-colors hover:text-gold-400"
                  >
                    Admin
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  data-cursor-hover
                  className="font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60 transition-colors hover:text-gold-400"
                >
                  Log Out
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                data-cursor-hover
                className="font-sans text-xs font-bold uppercase tracking-wider text-paper-100 transition-colors hover:text-gold-400"
              >
                Log In
              </Link>
            )}
            <ButtonLink to="/menu" className="!py-2.5 !px-6 !text-xs">
              Order Now
            </ButtonLink>
          </div>

          <button
            aria-label="Toggle menu"
            onClick={() => setOpen((o) => !o)}
            className="flex h-11 w-11 flex-col items-center justify-center gap-1.5 rounded-full border border-gold-400/40 lg:hidden"
          >
            <motion.span
              animate={open ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }}
              className="h-[2px] w-5 bg-gold-400"
            />
            <motion.span animate={open ? { opacity: 0 } : { opacity: 1 }} className="h-[2px] w-5 bg-gold-400" />
            <motion.span
              animate={open ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }}
              className="h-[2px] w-5 bg-gold-400"
            />
          </button>
        </div>

        <motion.div
          aria-hidden="true"
          style={{ scaleX: progress }}
          className="h-[2px] w-full origin-left bg-gradient-to-r from-gold-500 via-gold-300 to-gold-500"
        />
      </header>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ clipPath: "circle(0% at 92% 4%)" }}
            animate={{ clipPath: "circle(150% at 92% 4%)" }}
            exit={{ clipPath: "circle(0% at 92% 4%)" }}
            transition={{ duration: 0.6, ease: [0.65, 0, 0.35, 1] }}
            className="fixed inset-0 z-40 flex flex-col justify-center bg-ink-950 lg:hidden"
          >
            <div className="halftone absolute inset-0 opacity-[0.04]" />
            <nav className="relative flex flex-col gap-2 px-10">
              {links.map((l, i) => (
                <motion.div
                  key={l.to}
                  initial={{ opacity: 0, x: -40 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.15 + i * 0.07 }}
                >
                  <NavLink
                    to={l.to}
                    onClick={() => setOpen(false)}
                    className={({ isActive }) =>
                      `block py-3 font-display text-5xl uppercase tracking-wide ${
                        isActive ? "text-gold-400" : "text-paper-50"
                      }`
                    }
                  >
                    {l.label}
                  </NavLink>
                </motion.div>
              ))}
              <motion.div
                initial={{ opacity: 0, x: -40 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 + links.length * 0.07 }}
                className="mt-2"
              >
                <NavLink
                  to="/cart"
                  onClick={() => setOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-3 font-display text-3xl uppercase tracking-wide ${
                      isActive ? "text-gold-400" : "text-paper-50"
                    }`
                  }
                >
                  Cart
                  {cartCount > 0 && (
                    <span className="flex h-6 min-w-6 items-center justify-center rounded-full bg-gold-400 px-1.5 font-sans text-xs font-bold text-ink-950">
                      {cartCount}
                    </span>
                  )}
                </NavLink>
              </motion.div>

              {user && (
                <motion.div
                  initial={{ opacity: 0, x: -40 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.12 + links.length * 0.07 }}
                  className="mt-2"
                >
                  <NavLink
                    to="/orders"
                    onClick={() => setOpen(false)}
                    className={({ isActive }) =>
                      `block font-display text-3xl uppercase tracking-wide ${
                        isActive ? "text-gold-400" : "text-paper-50"
                      }`
                    }
                  >
                    Orders
                  </NavLink>
                </motion.div>
              )}

              {hasRole(user, "ADMIN") && (
                <motion.div
                  initial={{ opacity: 0, x: -40 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.13 + links.length * 0.07 }}
                  className="mt-2"
                >
                  <NavLink
                    to="/admin"
                    onClick={() => setOpen(false)}
                    className={({ isActive }) =>
                      `block font-display text-3xl uppercase tracking-wide ${
                        isActive ? "text-gold-400" : "text-paper-50"
                      }`
                    }
                  >
                    Admin
                  </NavLink>
                </motion.div>
              )}

              <motion.div
                initial={{ opacity: 0, x: -40 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.15 + links.length * 0.07 }}
                className="mt-4"
              >
                {user ? (
                  <button
                    onClick={handleLogout}
                    data-cursor-hover
                    className="font-display text-3xl uppercase tracking-wide text-paper-50"
                  >
                    Log Out
                  </button>
                ) : (
                  <NavLink
                    to="/login"
                    onClick={() => setOpen(false)}
                    className={({ isActive }) =>
                      `block font-display text-3xl uppercase tracking-wide ${
                        isActive ? "text-gold-400" : "text-paper-50"
                      }`
                    }
                  >
                    Log In
                  </NavLink>
                )}
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -40 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 + links.length * 0.07 }}
                className="mt-6"
              >
                <ButtonLink to="/menu" onClick={() => setOpen(false)}>
                  Order Now
                </ButtonLink>
              </motion.div>
            </nav>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
