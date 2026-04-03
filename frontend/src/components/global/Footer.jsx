import { Instagram, Linkedin, Twitter, Youtube } from "lucide-react";
import { Link } from "react-router-dom";
import Button from "@/components/global/Button";
import Input from "@/components/global/Input";

const columns = [
  {
    title: "Buy",
    links: [
      { label: "Browse Cars", to: "/listings" },
      { label: "Featured Listings", to: "/listings" },
      { label: "Certified Cars", to: "/listings" },
      { label: "AI Finder", to: "/ai-finder" }
    ]
  },
  {
    title: "Sell",
    links: [
      { label: "Post Listing", to: "/sell" },
      { label: "Dealer Program", to: "/sell" },
      { label: "Pricing", to: "/sell" },
      { label: "Success Stories", to: "/sell" }
    ]
  },
  {
    title: "Company",
    links: [
      { label: "About", to: "/" },
      { label: "Careers", to: "/" },
      { label: "Press", to: "/" },
      { label: "Support", to: "/" }
    ]
  }
];

const socialLinks = [
  { Icon: Instagram, label: "Instagram" },
  { Icon: Twitter, label: "Twitter" },
  { Icon: Linkedin, label: "LinkedIn" },
  { Icon: Youtube, label: "YouTube" }
];

export default function Footer() {
  return (
    <footer className="border-t border-white/10 bg-black text-white">
      <div className="mx-auto grid w-full max-w-content gap-10 px-6 py-16 lg:grid-cols-12">
        <div className="lg:col-span-4">
          <div className="flex items-center text-xl font-black tracking-tight">
            <span className="font-montserrat">AUTO</span>
            <span className="mx-1 inline-block h-1.5 w-1.5 rounded-full bg-blue" />
            <span className="font-montserrat text-blue">VAULT</span>
          </div>
          <p className="mt-4 max-w-xs font-playfair text-base italic text-gray-400">
            Crafted for discerning drivers who value style, performance, and trust.
          </p>
          <div className="mt-6 flex items-center gap-3">
            {socialLinks.map(({ Icon, label }) => (
              <Button key={label} variant="icon" aria-label={label} className="border-white/10 bg-black-700 text-white hover:bg-black-800">
                <Icon size={18} strokeWidth={1.5} />
              </Button>
            ))}
          </div>
        </div>

        <div className="grid gap-8 sm:grid-cols-3 lg:col-span-5">
          {columns.map((column) => (
            <div key={column.title}>
              <h4 className="text-sm font-semibold uppercase tracking-[0.14em] text-white">{column.title}</h4>
              <ul className="mt-4 space-y-2.5">
                {column.links.map((item) => (
                  <li key={item.label}>
                    <Link to={item.to} className="text-sm text-gray-400 transition-colors hover:text-gray-200">
                      {item.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="lg:col-span-3">
          <h4 className="text-sm font-semibold uppercase tracking-[0.14em] text-white">Stay Updated</h4>
          <p className="mt-3 text-sm text-gray-400">Get weekly listing drops and market insights.</p>
          <div className="mt-4 space-y-3">
            <Input dark type="email" placeholder="Enter your email" />
            <Button className="w-full">Subscribe</Button>
          </div>
        </div>
      </div>

      <div className="border-t border-white/10">
        <div className="mx-auto flex w-full max-w-content flex-col gap-2 px-6 py-6 text-sm text-gray-500 md:flex-row md:items-center md:justify-between">
          <p>© 2026 AutoVault. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <Link to="#" className="hover:text-gray-300">
              Privacy
            </Link>
            <Link to="#" className="hover:text-gray-300">
              Terms
            </Link>
            <Link to="#" className="hover:text-gray-300">
              Cookies
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
}

