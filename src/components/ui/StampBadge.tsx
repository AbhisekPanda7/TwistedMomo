import { useId } from "react";

export default function StampBadge({
  topText = "EST. 2022 ★ TWISTED MOMOS ★",
  center = "100%\nTWISTED",
  className = "h-28 w-28",
  spin = true,
}: {
  topText?: string;
  center?: string;
  className?: string;
  spin?: boolean;
}) {
  const uid = useId().replace(/:/g, "");
  const topId = `stampTop-${uid}`;

  return (
    <div className={`relative ${className}`}>
      <svg viewBox="0 0 200 200" className={`h-full w-full ${spin ? "animate-spin-slow" : ""}`}>
        <defs>
          <path id={topId} d="M 100,100 m -76,0 a 76,76 0 1,1 152,0 a 76,76 0 1,1 -152,0" fill="none" />
        </defs>
        <circle cx="100" cy="100" r="96" fill="transparent" stroke="#F5B700" strokeWidth="1.5" strokeDasharray="3 5" />
        <text fill="#F5B700" fontSize="10.5" fontFamily="Inter, sans-serif" fontWeight="700" letterSpacing="2">
          <textPath href={`#${topId}`} startOffset="0%">
            {topText}
          </textPath>
        </text>
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <div className="text-center font-display leading-none text-gold-400">
          {center.split("\n").map((line, i) => (
            <div key={i} className={i === 0 ? "text-[13px] tracking-widest" : "text-2xl"}>
              {line}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
