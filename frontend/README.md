# AutoVault Frontend (Phase 1 + Phase 2 + Phase 3 + Phase 4 + Phase 5 + Phase 6 + Phase 7 + Phase 8 + Phase 9 + Phase 10)

Vite + React + Tailwind scaffold for the AutoVault marketplace UI.

## Included

- Design token setup in `src/styles/globals.css`
- Tailwind theme extensions in `tailwind.config.js`
- Route skeleton in `src/App.jsx`
- Zustand stores in `src/store/*`
- Axios API client in `src/lib/api.js`
- Firebase bootstrap in `src/lib/firebase.js`
- Stripe bootstrap in `src/lib/stripe.js`
- Global components in `src/components/global/*`
  - `Button`, `Input`, `Badge`, `Skeleton`
  - `Navbar`, `Footer`, `PageTransition`
  - `ToastProvider` (custom blue toasts)
- Home page sections in `src/components/home/sections/*`
  - Hero, Mega Search, Stats, 3D Showcase
  - Featured Listings with reusable `CarCard`
  - Browse by Brand, Browse by Type, AI CTA, EMI teaser
- Home route assembly in `src/pages/HomePage.jsx`
- Listings route in `src/pages/ListingsPage.jsx`
- Listings components in `src/components/listings/*`
  - Search hero, filter sidebar, sort/view bar
  - Compare floating bar wired to Zustand `compareStore`
- Car detail route in `src/pages/CarDetailPage.jsx`
  - Hero gallery, breadcrumb, specs/features/description
  - Sticky price card, seller card, safety card, similar cars
- Auth route in `src/pages/AuthPage.jsx`
  - Cinematic split layout with brand panel and auth card
  - Sign In/Create Account tabs, social buttons, OTP UI shell
- AI Finder route in `src/pages/AiFinderPage.jsx`
  - 5-step animated questionnaire flow
  - AI processing screen and ranked match results
- Compare route in `src/pages/ComparePage.jsx`
  - Side-by-side comparison for up to 3 vehicles
  - Add/remove car actions and best-value highlight rows
- Sell route in `src/pages/PostListingPage.jsx`
  - 6-step listing wizard with progress header
  - Image upload preview shell and review/publish step
- Dashboard route in `src/pages/DashboardPage.jsx`
  - Profile header and tabbed sections (Listings, Saved, Bookings, Inquiries)
  - Tab skeleton loading and empty states
- Localized error fallback in `src/components/global/ErrorBoundary.jsx`
- SEO metadata with `react-helmet-async`
  - `HelmetProvider` in `src/main.jsx`
  - Title/description tags across core pages
- Dedicated 404 route in `src/pages/NotFoundPage.jsx`
- Backend listing integration via `src/lib/listingsService.js`
  - `ListingsPage` and `CarDetailPage` fetch from `/api/listings` endpoints
  - Automatic fallback to local seed data when API is unavailable
  - `FeaturedListingsSection` uses `/api/listings/featured` and `/api/listings/recent`
    with safe fallback to local home seed cards
- Home API helpers in `src/lib/homeService.js`
  - `BrowseMakeSection` loads `/api/cars/makes` with static fallback
  - `StatsSection` hydrates live total listings count from `/api/listings`
- Dashboard integration in `src/pages/DashboardPage.jsx`
  - `My Listings` loads `/api/listings/my-listings` when authenticated
  - Falls back to local seed data when unauthenticated or API is unavailable
- Auth integration
  - `src/lib/authService.js` for `/api/auth/email/login`, `/api/auth/email/register`, `/api/auth/me`, `/api/auth/logout`
  - `AuthPage` submits real login/register requests and redirects to dashboard on success
  - `authStore` now keeps `refreshToken` in addition to `accessToken` and `user`
  - Auth state persists in local storage via Zustand `persist`
  - App bootstraps session by calling `/api/auth/me` and clears invalid tokens automatically
  - Navbar is auth-aware and supports backend logout + local session clear

## Setup

1. Copy env file:

```powershell
Set-Location "C:\Users\irene\Downloads\project\frontend"
Copy-Item ".env.example" ".env"
```

2. Install and run:

```powershell
Set-Location "C:\Users\irene\Downloads\project\frontend"
npm install
npm run dev
```

3. Build check:

```powershell
Set-Location "C:\Users\irene\Downloads\project\frontend"
npm run build
```


















