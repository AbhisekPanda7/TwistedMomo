import Container from "../ui/Container";
import SectionHeading from "../ui/SectionHeading";
import PhotoTile from "../ui/PhotoTile";
import { ButtonLink } from "../ui/Button";

const tiles = [
  "Chilli Butter Garlic",
  "Kurkure Crunch",
  "Tandoori Fire",
  "Steam & Soul",
  "Afghani Creamy",
  "Schezwan Heat",
];

export default function GalleryTeaser() {
  return (
    <section className="relative bg-ink-950 py-24 sm:py-32">
      <Container>
        <div className="flex flex-col items-start justify-between gap-6 sm:flex-row sm:items-end">
          <SectionHeading kicker="Inside The Kitchen" title="A Taste Of" highlight="The Gallery" />
          <ButtonLink to="/gallery" variant="outline">
            View Full Gallery
          </ButtonLink>
        </div>

        <div className="mt-14 grid auto-rows-[140px] grid-cols-2 gap-4 sm:auto-rows-[160px] sm:grid-cols-4">
          {tiles.map((t, i) => (
            <PhotoTile
              key={t}
              label={t}
              variant={i}
              big={i === 0}
              className={i === 0 ? "col-span-2 row-span-2" : ""}
            />
          ))}
        </div>
      </Container>
    </section>
  );
}
