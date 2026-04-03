import {
  Calendar,
  CheckCircle,
  Clock,
  Eye,
  Flag,
  Fuel,
  Gauge,
  Heart,
  MapPin,
  Phone,
  Share2,
  Shield,
  Star,
  User
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link, useParams } from "react-router-dom";
import Button from "@/components/global/Button";
import PageTransition from "@/components/global/PageTransition";
import CarCard from "@/components/home/CarCard";
import { notify } from "@/components/global/ToastProvider";
import { initiateBooking } from "@/lib/bookingService";
import { sendInquiry } from "@/lib/inquiryService";
import { aiSimilar, fetchListingBySlug } from "@/lib/listingsService";
import { checkSaved, saveListing, unsaveListing } from "@/lib/savedService";
import { stripePromise } from "@/lib/stripe";
import { useAuthStore } from "@/store/authStore";

const featureList = [
  "Adaptive Cruise Control",
  "Apple CarPlay",
  "Panoramic Sunroof",
  "Blind Spot Assist",
  "Premium Audio",
  "360 Camera"
];

const specs = [
  ["Car Year", (car) => car.year],
  ["Body Type", (car) => car.bodyType.toUpperCase()],
  ["Fuel Type", (car) => car.fuel],
  ["Transmission", (car) => car.transmission],
  ["Mileage", (car) => car.mileage],
  ["Condition", (car) => (car.certified ? "Certified" : "Used")],
  ["Ownership", () => "First Owner"],
  ["Insurance", () => "Comprehensive"]
];

export default function CarDetailPage() {
  const { slug } = useParams();
  const { isAuthenticated } = useAuthStore();
  const [car, setCar] = useState(null);
  const [loading, setLoading] = useState(true);
  const [similarCars, setSimilarCars] = useState([]);
  const [activeImage, setActiveImage] = useState(0);
  const [isSaved, setIsSaved] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [inquiryText, setInquiryText] = useState("");
  const [showInquiryBox, setShowInquiryBox] = useState(false);
  const [inquiryLoading, setInquiryLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    setActiveImage(0);

    async function loadDetail() {
      setLoading(true);
      try {
        const detail = await fetchListingBySlug(slug);
        if (!isMounted) return;
        setCar(detail);

        // Load similar via AI
        if (detail?.id) {
          try {
            const sim = await aiSimilar(detail.id);
            if (isMounted) setSimilarCars(sim.items || []);
          } catch {
            if (isMounted) setSimilarCars([]);
          }
        }

        // Check saved status
        if (isAuthenticated && detail?.id) {
          try {
            const saved = await checkSaved(detail.id);
            if (isMounted) setIsSaved(saved);
          } catch { /* ignore */ }
        }
      } catch {
        if (isMounted) setCar(null);
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadDetail();
    return () => { isMounted = false; };
  }, [slug, isAuthenticated]);

  const handleSaveToggle = async () => {
    if (!isAuthenticated) { notify.error("Sign in to save listings."); return; }
    try {
      if (isSaved) {
        await unsaveListing(car.id);
        setIsSaved(false);
        notify.success("Removed from saved.");
      } else {
        await saveListing(car.id);
        setIsSaved(true);
        notify.success("Saved to your wishlist!");
      }
    } catch {
      notify.error("Could not update saved status.");
    }
  };

  const handleBookTestDrive = async () => {
    if (!isAuthenticated) { notify.error("Sign in to book a test drive."); return; }
    setBookingLoading(true);
    try {
      const booking = await initiateBooking({ listingId: car.id, type: "TEST_DRIVE" });
      if (booking.stripeClientSecret && stripePromise) {
        const stripe = await stripePromise;
        const { error } = await stripe.confirmCardPayment(booking.stripeClientSecret);
        if (error) {
          notify.error(error.message || "Payment failed. Please try again.");
          return;
        }
        notify.success("Payment confirmed! Check your Dashboard for details.");
      } else {
        notify.success("Test drive booked! Check your Dashboard for details.");
      }
    } catch {
      notify.error("Booking failed. Please try again.");
    } finally {
      setBookingLoading(false);
    }
  };

  const handleSendInquiry = async () => {
    if (!isAuthenticated) { notify.error("Sign in to send an inquiry."); return; }
    if (!inquiryText.trim()) { notify.error("Please enter a message."); return; }
    setInquiryLoading(true);
    try {
      await sendInquiry({ listingId: car.id, message: inquiryText.trim() });
      notify.success("Inquiry sent successfully!");
      setInquiryText("");
      setShowInquiryBox(false);
    } catch {
      notify.error("Failed to send inquiry. Please try again.");
    } finally {
      setInquiryLoading(false);
    }
  };

  if (loading) {
    return (
      <PageTransition>
        <Helmet>
          <title>Loading Car Details | AutoVault</title>
          <meta name="description" content="Loading selected vehicle details from AutoVault." />
        </Helmet>
        <main className="min-h-screen bg-[var(--white-soft)] px-6 py-24">
          <div className="mx-auto max-w-content rounded-3xl border border-gray-100 bg-white p-10 text-center shadow-blue-sm">
            <h1 className="text-3xl font-bold text-gray-900">Loading vehicle...</h1>
            <p className="mt-2 text-gray-600">Fetching listing details.</p>
          </div>
        </main>
      </PageTransition>
    );
  }

  if (!car) {
    return (
      <PageTransition>
        <Helmet>
          <title>Car Not Found | AutoVault</title>
          <meta name="description" content="The requested listing is unavailable." />
        </Helmet>
        <main className="min-h-screen bg-[var(--white-soft)] px-6 py-24">
          <div className="mx-auto max-w-content rounded-3xl border border-gray-100 bg-white p-10 text-center shadow-blue-sm">
            <h1 className="text-3xl font-bold text-gray-900">Car not found</h1>
            <p className="mt-2 text-gray-600">The requested listing is unavailable or has been removed.</p>
            <Button as={Link} to="/listings" className="mt-6">
              Back to Listings
            </Button>
          </div>
        </main>
      </PageTransition>
    );
  }

  // Use real images array from API, fall back to primary image duplicated for gallery UI
  const gallery = Array.isArray(car.images) && car.images.length > 0
    ? car.images
    : [car.image, car.image, car.image];

  return (
    <PageTransition>
      <Helmet>
        <title>{`${car.title} | AutoVault`}</title>
        <meta name="description" content={`View details, specs, and pricing for ${car.title} on AutoVault.`} />
      </Helmet>
      <main className="bg-[var(--white-soft)] pb-20">
        <section className="relative h-[62vh] min-h-[420px] w-full overflow-hidden bg-black">
          <img src={gallery[activeImage]} alt={car.title} className="h-full w-full object-cover" />
          <div className="absolute inset-0 bg-gradient-to-t from-black/55 to-transparent" />

          <div className="absolute bottom-6 left-6 flex max-w-[calc(100%-3rem)] gap-2 overflow-x-auto">
            {gallery.map((image, index) => (
              <button
                key={index}
                onClick={() => setActiveImage(index)}
                className={
                  activeImage === index
                    ? "h-16 w-24 shrink-0 overflow-hidden rounded-xl border-2 border-blue"
                    : "h-16 w-24 shrink-0 overflow-hidden rounded-xl border-2 border-transparent"
                }
              >
                <img src={image} alt={`${car.title} ${index + 1}`} className="h-full w-full object-cover" />
              </button>
            ))}
          </div>

          <div className="absolute right-6 top-6 rounded-full bg-black/60 px-3 py-1 text-sm text-white">
            {activeImage + 1}/{gallery.length}
          </div>
        </section>

        <section className="border-b border-gray-100 bg-white px-6 py-3">
          <div className="mx-auto max-w-content text-sm text-gray-500">
            <Link to="/" className="hover:text-blue">
              Home
            </Link>{" "}
            /{" "}
            <Link to="/listings" className="hover:text-blue">
              Cars
            </Link>{" "}
            / <span className="text-gray-900">{car.title}</span>
          </div>
        </section>

        <section className="mx-auto grid w-full max-w-content gap-6 px-6 py-8 lg:grid-cols-12 lg:items-start">
          <div className="space-y-6 lg:col-span-8">
            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h1 className="font-montserrat text-4xl font-extrabold text-gray-900">{car.title}</h1>
              <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-gray-400">
                <span className="inline-flex items-center gap-1">
                  <MapPin size={14} strokeWidth={1.5} /> {car.location}
                </span>
                <span className="inline-flex items-center gap-1">
                  <Clock size={14} strokeWidth={1.5} /> {car.listedAgo}
                </span>
                <span className="inline-flex items-center gap-1">
                  <Eye size={14} strokeWidth={1.5} /> {car.viewCount ? `${car.viewCount.toLocaleString()} views` : ""}
                </span>
              </div>
              <div className="mt-5 flex gap-2">
                <Button variant="icon" onClick={handleSaveToggle} title={isSaved ? "Unsave" : "Save"}>
                  <Heart size={16} strokeWidth={1.5} fill={isSaved ? "currentColor" : "none"} />
                </Button>
                <Button variant="icon" title="Share" onClick={() => navigator.clipboard?.writeText(window.location.href)}>
                  <Share2 size={16} strokeWidth={1.5} />
                </Button>
                <Button variant="icon" title="Report">
                  <Flag size={16} strokeWidth={1.5} />
                </Button>
              </div>
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h2 className="text-2xl font-bold text-gray-900">Specifications</h2>
              <div className="mt-5 grid gap-3 sm:grid-cols-2">
                {specs.map(([label, getValue]) => (
                  <div key={label} className="rounded-xl bg-gray-50 p-4">
                    <p className="text-xs uppercase tracking-[0.12em] text-gray-400">{label}</p>
                    <p className="mt-1 font-semibold text-gray-900">{getValue(car)}</p>
                  </div>
                ))}
              </div>
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h2 className="text-2xl font-bold text-gray-900">Features & Options</h2>
              <div className="mt-5 flex flex-wrap gap-2">
                {featureList.map((feature) => (
                  <span
                    key={feature}
                    className="inline-flex items-center gap-1 rounded-full border border-blue/20 bg-blue/5 px-4 py-2 text-sm font-medium text-blue"
                  >
                    <CheckCircle size={12} strokeWidth={1.5} />
                    {feature}
                  </span>
                ))}
              </div>
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h2 className="text-2xl font-bold text-gray-900">About This Car</h2>
              <p className="mt-4 leading-relaxed text-gray-600">
                This {car.title} is maintained in excellent condition with complete service history and verified documents.
                The cabin quality, responsive drivetrain, and advanced driver assistance package make it a strong pick
                for both everyday comfort and spirited performance.
              </p>
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h2 className="text-2xl font-bold text-gray-900">Location</h2>
              <p className="mt-3 text-gray-600">{car.location} • 24 km from your location</p>
              <div className="mt-4 h-52 rounded-2xl bg-gray-100" />
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm">
              <h2 className="text-2xl font-bold text-gray-900">Seller Reviews</h2>
              <div className="mt-4 flex items-center gap-4">
                <p className="font-montserrat text-5xl font-black text-gray-900">4.8</p>
                <div>
                  <div className="flex items-center gap-1 text-blue">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Star key={index} size={16} strokeWidth={1.5} fill="currentColor" />
                    ))}
                  </div>
                  <p className="text-sm text-gray-400">Based on 48 reviews</p>
                </div>
              </div>
            </article>
          </div>

          <div className="space-y-6 lg:sticky lg:top-24 lg:col-span-4">
            <article className="rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-xl">
              <p className="font-mono text-5xl font-extrabold text-gray-900">{car.price}</p>
              <p className="mt-1 text-gray-400 line-through">{car.originalPrice}</p>

              <div className="mt-4 rounded-2xl bg-blue/5 p-4">
                <p className="text-sm text-gray-500">EMI from</p>
                <p className="font-mono text-2xl font-bold text-blue">$485/month</p>
                <p className="text-xs text-gray-400">at 8% for 60 months</p>
              </div>

              <div className="mt-5 space-y-3">
                <Button className="w-full" onClick={handleBookTestDrive} disabled={bookingLoading}>
                  <Calendar size={16} strokeWidth={1.5} />
                  {bookingLoading ? "Booking..." : "Book Test Drive"}
                </Button>
                <Button variant="secondary" className="w-full" onClick={() => setShowInquiryBox((v) => !v)}>
                  Make an Offer / Send Inquiry
                </Button>
              </div>
              {showInquiryBox && (
                <div className="mt-3 space-y-2">
                  <textarea
                    value={inquiryText}
                    onChange={(e) => setInquiryText(e.target.value)}
                    rows={3}
                    placeholder="Write your message or offer..."
                    className="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue"
                  />
                  <Button className="w-full" size="sm" onClick={handleSendInquiry} disabled={inquiryLoading}>
                    {inquiryLoading ? "Sending..." : "Send Message"}
                  </Button>
                </div>
              )}

              <div className="my-4 border-t border-gray-100" />

              <Button variant="secondary" className="w-full">
                <Phone size={16} strokeWidth={1.5} />
                Show Number
              </Button>
            </article>

            <article className="rounded-3xl border border-gray-100 bg-white p-6 shadow-blue-sm">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue/10 text-blue">
                  <User size={20} strokeWidth={1.5} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">{car.seller}</p>
                  <p className="text-sm text-gray-500">Verified Dealer</p>
                </div>
              </div>
              <div className="mt-4 grid grid-cols-3 gap-2 text-center text-xs text-gray-500">
                <div className="rounded-xl bg-gray-50 p-2">142 listings</div>
                <div className="rounded-xl bg-gray-50 p-2">98% response</div>
                <div className="rounded-xl bg-gray-50 p-2">&lt;1h reply</div>
              </div>
            </article>

            <article className="rounded-3xl border border-amber-100 bg-amber-50 p-6">
              <p className="inline-flex items-center gap-2 text-sm font-semibold text-amber-700">
                <Shield size={16} strokeWidth={1.5} /> Buy Safely
              </p>
              <ul className="mt-3 space-y-2 text-sm text-amber-800">
                <li>Meet seller in public places.</li>
                <li>Verify documents before payment.</li>
                <li>Avoid sharing OTP or personal PIN.</li>
              </ul>
            </article>
          </div>
        </section>

        {similarCars.length > 0 && (
          <section className="mx-auto w-full max-w-content px-6">
            <h2 className="mb-4 text-2xl font-bold text-gray-900">Similar Vehicles</h2>
            <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
              {similarCars.map((item) => (
                <CarCard
                  key={item.id || item.slug}
                  car={{
                    id: item.id,
                    slug: item.slug,
                    title: item.title,
                    price: new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(item.price || 0),
                    priceValue: item.price || 0,
                    image: item.primaryImageUrl || "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80",
                    bodyType: item.bodyType || "sedan",
                    fuel: item.fuelType || "Unknown",
                    year: item.year,
                    mileage: "",
                    mileageValue: 0,
                    location: "",
                    transmission: "",
                    listedAgo: ""
                  }}
                />
              ))}
            </div>
          </section>
        )}
      </main>
    </PageTransition>
  );
}



