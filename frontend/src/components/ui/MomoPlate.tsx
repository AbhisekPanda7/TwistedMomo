/** Stylised flat-illustration plate of momos — the site's signature graphic motif. */
export default function MomoPlate({ className = "w-full h-full" }: { className?: string }) {
  const count = 8;
  const items = Array.from({ length: count });

  return (
    <svg viewBox="0 0 400 400" className={className}>
      <defs>
        <radialGradient id="plateShadow" cx="50%" cy="55%" r="55%">
          <stop offset="0%" stopColor="#F5B700" stopOpacity="0.16" />
          <stop offset="100%" stopColor="#F5B700" stopOpacity="0" />
        </radialGradient>
        <linearGradient id="momoFill" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#F8DD85" />
          <stop offset="100%" stopColor="#F5B700" />
        </linearGradient>
      </defs>

      <circle cx="200" cy="200" r="196" fill="url(#plateShadow)" />
      <circle cx="200" cy="200" r="168" fill="none" stroke="#F5B700" strokeWidth="2" opacity="0.5" />
      <circle cx="200" cy="200" r="150" fill="#131318" stroke="#2A2A32" strokeWidth="1" />

      {items.map((_, i) => {
        const angle = (i / count) * Math.PI * 2 - Math.PI / 2;
        const r = 96;
        const cx = 200 + Math.cos(angle) * r;
        const cy = 200 + Math.sin(angle) * r;
        return (
          <g key={i} transform={`translate(${cx} ${cy}) rotate(${(angle * 180) / Math.PI + 90})`}>
            <path
              d="M -22 10 A 22 20 0 1 1 22 10 Z"
              fill="url(#momoFill)"
              stroke="#7A5200"
              strokeWidth="1.2"
            />
            {[-14, -7, 0, 7, 14].map((x) => (
              <path key={x} d={`M ${x} -8 Q ${x} 2 ${x} 10`} stroke="#B98600" strokeWidth="1.4" fill="none" strokeLinecap="round" />
            ))}
            <circle cx="0" cy="-9" r="3" fill="#B98600" />
          </g>
        );
      })}

      <circle cx="200" cy="200" r="48" fill="#E2321F" stroke="#7A3208" strokeWidth="2" />
      <circle cx="200" cy="200" r="48" fill="none" stroke="#F5B700" strokeWidth="1" opacity="0.4" />
      {Array.from({ length: 6 }).map((_, i) => (
        <circle
          key={i}
          cx={200 + Math.cos((i / 6) * Math.PI * 2) * 20}
          cy={200 + Math.sin((i / 6) * Math.PI * 2) * 20}
          r="2"
          fill="#F5B700"
          opacity="0.5"
        />
      ))}
    </svg>
  );
}
