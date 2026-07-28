/** Flat-vector portrait of Boozi, the brand's biker-dog mascot. */
export default function BooziMascot({ className = "w-full h-full" }: { className?: string }) {
  return (
    <svg viewBox="0 0 220 220" className={className}>
      <defs>
        <linearGradient id="furGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#DDA860" />
          <stop offset="100%" stopColor="#9C6A32" />
        </linearGradient>
      </defs>

      <circle cx="110" cy="110" r="108" fill="#0A0A0E" />
      <circle cx="110" cy="110" r="100" fill="none" stroke="#F5B700" strokeWidth="2" />

      {/* ears */}
      <path d="M62 70 L40 20 L88 58 Z" fill="url(#furGrad)" stroke="#241705" strokeWidth="2" />
      <path d="M158 70 L180 20 L132 58 Z" fill="url(#furGrad)" stroke="#241705" strokeWidth="2" />

      {/* head */}
      <path
        d="M110 55c34 0 55 24 55 56 0 30-22 55-55 55s-55-25-55-55c0-32 21-56 55-56Z"
        fill="url(#furGrad)"
        stroke="#241705"
        strokeWidth="2"
      />

      {/* face mask */}
      <path d="M110 92c20 0 32 12 32 34 0 20-14 34-32 34s-32-14-32-34c0-22 12-34 32-34Z" fill="#F3E2C4" opacity="0.9" />

      {/* sunglasses */}
      <rect x="66" y="104" width="34" height="20" rx="8" fill="#0A0A0E" />
      <rect x="120" y="104" width="34" height="20" rx="8" fill="#0A0A0E" />
      <rect x="100" y="110" width="20" height="4" fill="#0A0A0E" />
      <rect x="66" y="112" width="90" height="2.5" fill="#0A0A0E" opacity="0.6" />

      {/* nose + mouth */}
      <ellipse cx="110" cy="140" rx="9" ry="6" fill="#0A0A0E" />
      <path d="M110 146v6" stroke="#0A0A0E" strokeWidth="2" strokeLinecap="round" />
      <path d="M98 156q12 10 24 0" stroke="#0A0A0E" strokeWidth="2" fill="none" strokeLinecap="round" />

      {/* tongue */}
      <path d="M108 158c0 8 4 14 6 14s6-6 6-14" fill="#E2321F" />

      {/* jacket collar */}
      <path d="M55 190c15 12 35 18 55 18s40-6 55-18l-14-16c-12 8-27 12-41 12s-29-4-41-12Z" fill="#F5B700" />
      <path d="M55 190c15 12 35 18 55 18v-14c-12 0-27-4-41-12Z" fill="#E0A600" />
    </svg>
  );
}
