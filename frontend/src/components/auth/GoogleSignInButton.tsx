import { useEffect, useRef, useState } from "react";

const SCRIPT_SRC = "https://accounts.google.com/gsi/client";
const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

type GoogleCredentialResponse = { credential?: string };

type GoogleAccounts = {
  accounts: {
    id: {
      initialize: (config: {
        client_id: string;
        callback: (response: GoogleCredentialResponse) => void;
      }) => void;
      renderButton: (parent: HTMLElement, options: Record<string, string | number>) => void;
    };
  };
};

declare global {
  interface Window {
    google?: GoogleAccounts;
  }
}

/** Resolves once Google's script is on the page, reusing the tag if something already added it. */
function loadScript(): Promise<void> {
  if (window.google?.accounts?.id) return Promise.resolve();

  return new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(`script[src="${SCRIPT_SRC}"]`);
    if (existing) {
      existing.addEventListener("load", () => resolve());
      existing.addEventListener("error", () => reject(new Error("script failed")));
      return;
    }
    const script = document.createElement("script");
    script.src = SCRIPT_SRC;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("script failed"));
    document.head.appendChild(script);
  });
}

export default function GoogleSignInButton({
  onCredential,
  onError,
}: {
  onCredential: (idToken: string) => void;
  onError: (message: string) => void;
}) {
  const target = useRef<HTMLDivElement>(null);
  const [unavailable, setUnavailable] = useState(false);
  // Keeping the callback in a ref means re-renders never re-run initialize(), which would
  // otherwise render a second button into the same container.
  const latest = useRef({ onCredential, onError });
  latest.current = { onCredential, onError };

  useEffect(() => {
    if (!CLIENT_ID) {
      setUnavailable(true);
      return;
    }

    let cancelled = false;
    loadScript()
      .then(() => {
        if (cancelled || !target.current || !window.google) return;
        window.google.accounts.id.initialize({
          client_id: CLIENT_ID,
          callback: (response) => {
            if (response.credential) {
              latest.current.onCredential(response.credential);
            } else {
              latest.current.onError("Google did not return a sign-in token.");
            }
          },
        });
        window.google.accounts.id.renderButton(target.current, {
          theme: "filled_black",
          size: "large",
          shape: "pill",
          text: "continue_with",
          width: 320,
        });
      })
      .catch(() => {
        if (!cancelled) setUnavailable(true);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Silent when unavailable — an ad blocker or a missing client id should not leave a
  // broken control on the page. Email sign-in still works.
  if (unavailable) return null;

  return <div ref={target} className="flex justify-center" />;
}
