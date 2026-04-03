import { Link } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import Button from "@/components/global/Button";
import PageTransition from "@/components/global/PageTransition";

export default function NotFoundPage() {
  return (
    <PageTransition>
      <Helmet>
        <title>Page Not Found | AutoVault</title>
        <meta name="description" content="The page you are looking for does not exist on AutoVault." />
      </Helmet>
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-24">
        <section className="mx-auto w-full max-w-content rounded-3xl border border-gray-100 bg-white p-12 text-center shadow-blue-sm">
          <p className="text-xs font-semibold tracking-[0.14em] text-blue">404 ERROR</p>
          <h1 className="mt-3 font-montserrat text-4xl font-black text-gray-900">Page Not Found</h1>
          <p className="mt-2 text-gray-600">This route does not exist. Return home to continue browsing vehicles.</p>
          <Button as={Link} to="/" className="mt-6">
            Go Home
          </Button>
        </section>
      </main>
    </PageTransition>
  );
}

