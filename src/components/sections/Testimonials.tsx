import Container from "../ui/Container";
import SectionHeading from "../ui/SectionHeading";
import { StarIcon } from "../ui/Icons";

const reviews = [
  { name: "Aditi R.", role: "Regular · MG Road", quote: "The Kurkure momos ruined every other momo place for me. That crunch is unreal.", rating: 5 },
  { name: "Kabir S.", role: "First-timer", quote: "Walked in for the newspaper posters, stayed for the Tandoori momos. Smoky perfection.", rating: 5 },
  { name: "Meher K.", role: "Weekly regular", quote: "Chilli Butter Garlic is not for the weak. I love that they don't hold back on flavour.", rating: 5 },
  { name: "Rohan D.", role: "Late-night regular", quote: "Ordered at 11pm on a whim. Best decision of my week. The Afghani momos are criminal.", rating: 4 },
  { name: "Ishaan V.", role: "Food blogger", quote: "Genuinely one of the boldest street-food brands I've covered. The mascot has more personality than most restaurants.", rating: 5 },
  { name: "Priya N.", role: "Regular · Corn Cheese loyalist", quote: "Consistent, fresh, and always packed with flavour. My go-to Friday order.", rating: 5 },
];

function Card({ r }: { r: (typeof reviews)[number] }) {
  return (
    <div className="card-elevate mx-3 flex w-[320px] shrink-0 flex-col rounded-2xl border border-ink-600 bg-ink-900 p-6 transition-transform duration-300 hover:-translate-y-1.5 hover:border-gold-400/30 sm:w-[360px]">
      <div className="flex items-center gap-1 text-gold-400" aria-hidden="true">
        {Array.from({ length: 5 }).map((_, i) => (
          <StarIcon key={i} className={`h-4 w-4 ${i < r.rating ? "opacity-100" : "opacity-20"}`} />
        ))}
      </div>
      <p className="mt-4 flex-1 font-sans text-sm leading-relaxed text-paper-100/85">“{r.quote}”</p>
      <div className="mt-5 flex items-center gap-3 border-t border-ink-600 pt-4">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gold-400/15 font-display text-sm text-gold-400">
          {r.name.charAt(0)}
        </div>
        <div>
          <div className="font-sans text-sm font-semibold text-paper-50">{r.name}</div>
          <div className="font-sans text-xs text-paper-200/50">{r.role}</div>
        </div>
      </div>
    </div>
  );
}

export default function Testimonials() {
  const row1 = [...reviews, ...reviews];
  return (
    <section className="relative overflow-hidden bg-ink-950 py-24 sm:py-32">
      <Container>
        <SectionHeading kicker="Word On The Street" title="Loved By" highlight="Regulars" align="center" />
      </Container>

      <div className="group relative mt-14 mask-fade-x">
        <div
          className="flex w-max animate-marquee py-2 group-hover:[animation-play-state:paused]"
          style={{ animationDuration: "50s" }}
        >
          {row1.map((r, i) => (
            <Card key={i} r={r} />
          ))}
        </div>
      </div>
    </section>
  );
}
