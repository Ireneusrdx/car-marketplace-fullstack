import { Component } from "react";
import Button from "@/components/global/Button";

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error) {
    // Keep console logging for local debugging without crashing the entire app.
    console.error("Dashboard render failed:", error);
  }

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-20">
        <section className="mx-auto w-full max-w-content rounded-3xl border border-gray-100 bg-white p-10 text-center shadow-blue-sm">
          <h1 className="font-montserrat text-3xl font-bold text-gray-900">Something went wrong</h1>
          <p className="mt-2 text-gray-600">We could not load this page. Please try again.</p>
          <Button className="mt-6" onClick={() => this.setState({ hasError: false })}>
            Retry
          </Button>
        </section>
      </main>
    );
  }
}

