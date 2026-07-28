import { StarIcon } from "./Icons";

export default function MarqueeStrip({
  text = "TWISTING FLAVOURS",
  bg = "bg-gold-400",
  fg = "text-ink-950",
}: {
  text?: string;
  bg?: string;
  fg?: string;
}) {
  const items = Array.from({ length: 10 });
  return (
    <div aria-hidden="true" className={`relative overflow-hidden py-3 ${bg} border-y-2 border-ink-950`}>
      <div className="flex w-max animate-marquee items-center gap-6">
        {[0, 1].map((dup) => (
          <div key={dup} className="flex shrink-0 items-center gap-6">
            {items.map((_, i) => (
              <div key={i} className={`flex items-center gap-6 font-display text-lg sm:text-xl ${fg}`}>
                <span className="tracking-wide">{text}</span>
                <StarIcon className="h-3.5 w-3.5" />
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
