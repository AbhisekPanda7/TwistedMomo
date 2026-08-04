import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Container from "../components/ui/Container";
import { ButtonLink } from "../components/ui/Button";
import { api } from "../lib/api";
import { toApiError } from "../lib/apiError";

type State =
  | { status: "verifying" }
  | { status: "awaiting" }
  | { status: "done" }
  | { status: "failed"; message: string };

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const token = params.get("token");
  // Reached without a token straight after signing up — nothing to verify yet,
  // so say what is about to arrive rather than showing an error.
  const [state, setState] = useState<State>(() =>
    token ? { status: "verifying" } : { status: "awaiting" },
  );
  // React 18 mounts effects twice in dev; without this the second run posts an
  // already-consumed token and turns a success into an error.
  const attempted = useRef(false);

  useEffect(() => {
    if (attempted.current) return;
    attempted.current = true;

    if (!token) return;

    api
      .post("/auth/verify-email", { token })
      .then(() => setState({ status: "done" }))
      .catch((err: unknown) =>
        setState({
          status: "failed",
          message: toApiError(err, "We could not verify this link. Try requesting a new one.").message,
        }),
      );
  }, [token]);

  return (
    <section className="flex min-h-[80vh] items-center justify-center bg-ink-950 pt-24">
      <Container className="text-center">
        {state.status === "verifying" && (
          <>
            <h1 className="font-display text-3xl uppercase text-paper-50 sm:text-5xl">
              Confirming Your Email
            </h1>
            <p className="mx-auto mt-4 max-w-sm font-sans text-sm text-paper-200/60">
              One moment while we check your link.
            </p>
          </>
        )}

        {state.status === "awaiting" && (
          <>
            <h1 className="font-display text-3xl uppercase text-paper-50 sm:text-5xl">
              Check Your Inbox
            </h1>
            <p className="mx-auto mt-4 max-w-sm font-sans text-sm text-paper-200/60">
              We sent you a link to confirm your email. It works once and expires in 24 hours.
            </p>
            <p className="mx-auto mt-2 max-w-sm font-sans text-xs text-paper-200/40">
              You can order in the meantime — confirming just secures your account.
            </p>
            <div className="mt-8">
              <ButtonLink to="/menu">Browse The Menu</ButtonLink>
            </div>
          </>
        )}

        {state.status === "done" && (
          <>
            <span className="font-display text-8xl leading-none text-gold-400/30">✓</span>
            <h1 className="-mt-4 font-display text-3xl uppercase text-paper-50 sm:text-5xl">
              Email Confirmed
            </h1>
            <p className="mx-auto mt-4 max-w-sm font-sans text-sm text-paper-200/60">
              You're all set. Time to get some momos.
            </p>
            <div className="mt-8">
              <ButtonLink to="/menu">See The Menu</ButtonLink>
            </div>
          </>
        )}

        {state.status === "failed" && (
          <>
            <h1 className="font-display text-3xl uppercase text-paper-50 sm:text-5xl">
              Link Didn't Work
            </h1>
            <p className="mx-auto mt-4 max-w-sm font-sans text-sm text-paper-200/60">{state.message}</p>
            <p className="mx-auto mt-2 max-w-sm font-sans text-xs text-paper-200/40">
              Verification links work once and expire after 24 hours.
            </p>
            <div className="mt-8">
              <ButtonLink to="/">Back To Home</ButtonLink>
            </div>
          </>
        )}
      </Container>
    </section>
  );
}
