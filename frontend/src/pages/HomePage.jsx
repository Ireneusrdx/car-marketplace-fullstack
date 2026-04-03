import PageTransition from "@/components/global/PageTransition";
import { Helmet } from "react-helmet-async";
import AiFinderSection from "@/components/home/sections/AiFinderSection";
import BrowseBodyTypeSection from "@/components/home/sections/BrowseBodyTypeSection";
import BrowseMakeSection from "@/components/home/sections/BrowseMakeSection";
import EmiTeaserSection from "@/components/home/sections/EmiTeaserSection";
import FeaturedListingsSection from "@/components/home/sections/FeaturedListingsSection";
import HeroSection from "@/components/home/sections/HeroSection";
import MegaSearchSection from "@/components/home/sections/MegaSearchSection";
import ShowcaseSection from "@/components/home/sections/ShowcaseSection";
import StatsSection from "@/components/home/sections/StatsSection";

export default function HomePage() {
  return (
    <PageTransition>
      <Helmet>
        <title>AutoVault | Premium Car Marketplace</title>
        <meta name="description" content="Discover verified cars, compare listings, and find your next vehicle with AutoVault." />
      </Helmet>
      <main>
        <HeroSection />
        <MegaSearchSection />
        <StatsSection />
        <ShowcaseSection />
        <FeaturedListingsSection />
        <BrowseMakeSection />
        <BrowseBodyTypeSection />
        <AiFinderSection />
        <EmiTeaserSection />
      </main>
    </PageTransition>
  );
}


