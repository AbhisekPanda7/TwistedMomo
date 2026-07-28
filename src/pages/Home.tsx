import Hero from "../components/sections/Hero";
import FeaturedSpecials from "../components/sections/FeaturedSpecials";
import AboutTeaser from "../components/sections/AboutTeaser";
import MenuCategoriesPreview from "../components/sections/MenuCategoriesPreview";
import DidYouKnow from "../components/sections/DidYouKnow";
import Testimonials from "../components/sections/Testimonials";
import GalleryTeaser from "../components/sections/GalleryTeaser";
import LocationCTA from "../components/sections/LocationCTA";
import FinalCTA from "../components/sections/FinalCTA";

export default function Home() {
  return (
    <>
      <Hero />
      <FeaturedSpecials />
      <AboutTeaser />
      <MenuCategoriesPreview />
      <DidYouKnow />
      <Testimonials />
      <GalleryTeaser />
      <LocationCTA />
      <FinalCTA />
    </>
  );
}
