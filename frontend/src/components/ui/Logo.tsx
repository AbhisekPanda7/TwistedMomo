type LogoProps = {
  className?: string;
  variant?: "badge" | "wordmark";
};

/** Circular stamp-badge logo, recreated in code from the brand mark. */
export default function Logo({ className = "h-16 w-16", variant = "badge" }: LogoProps) {
  if (variant === "wordmark") {
    return (
      <span className={className + " font-display tracking-wide"}>
        <span className="text-gold-400">TWISTED</span>{" "}
        <span className="text-paper-50">MOMOS</span>
      </span>
    );
  }

  return (
    <svg viewBox="0 0 200 200" className={className} role="img" aria-label="Twisted Momos">
      <defs>
        <path id="logoTopArc" d="M 28,108 A 76,76 0 0 1 172,108" fill="none" />
        <path id="logoBottomArc" d="M 38,140 A 76,76 0 0 0 162,140" fill="none" />
      </defs>

      <circle cx="100" cy="100" r="98" fill="#07070a" />
      <circle cx="100" cy="100" r="90" fill="none" stroke="#F5B700" strokeWidth="2" />
      <circle cx="100" cy="100" r="84" fill="none" stroke="#F5B700" strokeWidth="1" strokeDasharray="2 4" opacity="0.6" />

      <text fill="#F5B700" fontSize="10" fontFamily="Inter, sans-serif" fontWeight="700" letterSpacing="3">
        <textPath href="#logoTopArc" startOffset="50%" textAnchor="middle">
          ★ EST. 2022 ★
        </textPath>
      </text>

      <g transform="translate(100,96)" textAnchor="middle">
        <text
          y="-6"
          fill="#F5B700"
          fontFamily="Anton, sans-serif"
          fontSize="34"
          letterSpacing="1"
        >
          TWISTED
        </text>
        <text
          y="26"
          fill="#FAF4E3"
          fontFamily="Anton, sans-serif"
          fontSize="26"
          letterSpacing="4"
        >
          MOMOS
        </text>
      </g>

      <text fill="#F5B700" fontSize="9" fontFamily="Inter, sans-serif" fontWeight="600" letterSpacing="2.5">
        <textPath href="#logoBottomArc" startOffset="50%" textAnchor="middle">
          TWISTING FLAVOURS
        </textPath>
      </text>
    </svg>
  );
}
