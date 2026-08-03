import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import ErrorNote from "../components/ui/ErrorNote";
import { Button } from "../components/ui/Button";
import { useAuth } from "../context/AuthContext";
import { localError, toApiError, type ApiError } from "../lib/apiError";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-900 px-4 py-3 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<ApiError | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (password.length < 8) {
      setError(localError("Password must be at least 8 characters."));
      return;
    }
    if (password !== confirmPassword) {
      setError(localError("Passwords don't match."));
      return;
    }

    setSubmitting(true);
    try {
      await register({ name, email, password, phone: phone || undefined });
      navigate("/", { replace: true });
    } catch (err) {
      setError(toApiError(err, "Couldn't create your account. Please try again."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHero
        kicker="Join The Twist"
        title={
          <>
            Create <span className="text-gold-400">Account</span>
          </>
        }
      />

      <Container className="pb-24 sm:pb-32">
        <div className="mx-auto max-w-md">
          <Reveal>
            <form
              onSubmit={handleSubmit}
              className="card-elevate space-y-5 rounded-3xl border border-ink-600 bg-ink-900 p-8 sm:p-10"
            >
              <ErrorNote error={error} />

              <div>
                <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                  Name
                </label>
                <input
                  required
                  autoComplete="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className={inputClass}
                  placeholder="Your name"
                />
              </div>

              <div>
                <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                  Email
                </label>
                <input
                  required
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={inputClass}
                  placeholder="you@email.com"
                />
              </div>

              <div>
                <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                  Phone <span className="normal-case text-paper-200/40">(optional)</span>
                </label>
                <input
                  autoComplete="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className={inputClass}
                  placeholder="+91 00000 00000"
                />
              </div>

              <div className="grid gap-5 sm:grid-cols-2">
                <div>
                  <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                    Password
                  </label>
                  <input
                    required
                    type="password"
                    autoComplete="new-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className={inputClass}
                    placeholder="••••••••"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60">
                    Confirm
                  </label>
                  <input
                    required
                    type="password"
                    autoComplete="new-password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className={inputClass}
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting ? "Creating Account…" : "Create Account"}
              </Button>

              <p className="text-center font-sans text-sm text-paper-200/60">
                Already have an account?{" "}
                <Link to="/login" data-cursor-hover className="font-bold text-gold-400 hover:underline">
                  Log in
                </Link>
              </p>
            </form>
          </Reveal>
        </div>
      </Container>
    </>
  );
}
