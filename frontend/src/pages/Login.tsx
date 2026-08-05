import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Container from "../components/ui/Container";
import PageHero from "../components/ui/PageHero";
import Reveal from "../components/ui/Reveal";
import ErrorNote from "../components/ui/ErrorNote";
import { Button } from "../components/ui/Button";
import { useAuth } from "../context/AuthContext";
import GoogleSignInButton from "../components/auth/GoogleSignInButton";
import { toApiError, type ApiError } from "../lib/apiError";
import { landingPathFor } from "../lib/landing";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-900 px-4 py-3 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";

export default function Login() {
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<ApiError | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [googleSubmitting, setGoogleSubmitting] = useState(false);

  // Only used when there is no page to return to. `from` is set by ProtectedRoute
  // when it bounces an unauthenticated visitor.
  const returnTo = (location.state as { from?: string } | null)?.from ?? null;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    // Enter still submits a disabled-button form, so the guard belongs here too.
    if (googleSubmitting) return;
    setError(null);
    setSubmitting(true);
    try {
      const signedIn = await login({ email, password });
      navigate(returnTo ?? landingPathFor(signedIn), { replace: true });
    } catch (err) {
      setError(toApiError(err, "Invalid email or password."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHero
        kicker="Welcome Back"
        title={
          <>
            Log <span className="text-gold-400">In</span>
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
                  Password
                </label>
                <input
                  required
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={inputClass}
                  placeholder="••••••••"
                />
              </div>

              <Button type="submit" className="w-full" disabled={submitting || googleSubmitting}>
                {submitting ? "Logging In…" : "Log In"}
              </Button>

              <div className="flex items-center gap-3 pt-2">
                <div className="h-px flex-1 bg-ink-600" />
                <span className="font-sans text-xs uppercase tracking-wider text-paper-200/40">or</span>
                <div className="h-px flex-1 bg-ink-600" />
              </div>

              <div className="relative">
                <GoogleSignInButton
                  onCredential={async (idToken) => {
                    setError(null);
                    setGoogleSubmitting(true);
                    try {
                      const signedIn = await loginWithGoogle(idToken);
                      navigate(returnTo ?? landingPathFor(signedIn), { replace: true });
                    } catch (err) {
                      setError(toApiError(err, "Could not sign in with Google."));
                      // Only on failure: a success unmounts this page, and clearing
                      // state on the way out warns about setting state after unmount.
                      setGoogleSubmitting(false);
                    }
                  }}
                  onError={(message) => setError({ message, traceId: null, status: null })}
                />

                {googleSubmitting && (
                  <div className="absolute inset-0 z-10 flex items-center justify-center gap-3 rounded-2xl bg-ink-900">
                    <span
                      className="h-4 w-4 animate-spin rounded-full border-2 border-gold-400/30 border-t-gold-400"
                      aria-hidden="true"
                    />
                    <span className="font-sans text-sm text-paper-200/80" role="status">
                      Signing you in…
                    </span>
                  </div>
                )}
              </div>

              <p className="text-center font-sans text-sm text-paper-200/60">
                New here?{" "}
                <Link to="/register" data-cursor-hover className="font-bold text-gold-400 hover:underline">
                  Create an account
                </Link>
              </p>
            </form>
          </Reveal>
        </div>
      </Container>
    </>
  );
}
