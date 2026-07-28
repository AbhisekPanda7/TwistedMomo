import Container from "../components/ui/Container";
import { ButtonLink } from "../components/ui/Button";

export default function NotFound() {
  return (
    <section className="flex min-h-[80vh] items-center justify-center bg-ink-950 pt-24">
      <Container className="text-center">
        <span className="font-display text-[10rem] leading-none text-gold-400/20 sm:text-[14rem]">404</span>
        <h1 className="-mt-8 font-display text-3xl uppercase text-paper-50 sm:text-5xl">
          This Momo Wandered Off
        </h1>
        <p className="mx-auto mt-4 max-w-sm font-sans text-sm text-paper-200/60">
          The page you're looking for got twisted right out of existence. Let's get you back
          to the good stuff.
        </p>
        <div className="mt-8">
          <ButtonLink to="/">Back To Home</ButtonLink>
        </div>
      </Container>
    </section>
  );
}
