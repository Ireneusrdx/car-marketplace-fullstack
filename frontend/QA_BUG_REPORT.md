# AutoVault QA Bug Report

**Date:** 2026-04-03  
**Tester:** Playwright E2E Automation Suite  
**Environment:** Windows, Chromium v1217, Frontend: Vite@5173, Backend: Spring Boot@8080  
**Test Suite:** 10 spec files, 75 tests (desktop-chrome project)

---

## Summary

| Severity | Count |
|----------|-------|
| 🔴 Critical | 2 |
| 🟠 High | 4 |
| 🟡 Medium | 5 |
| 🔵 Low | 3 |
| **Total** | **14** |

---

## 🔴 CRITICAL BUGS

### BUG-01: Navbar reads removed `refreshToken` from store
- **File:** `frontend/src/components/global/Navbar.jsx` (lines 24, 31)
- **Symptom:** Navbar reads `state.refreshToken` which was removed from `authStore` in prior audit. Value is `undefined`. While harmless now (logout takes no args), it indicates dead code and will throw if store shape changes.
- **Test:** `Smoke & Navigation → all public pages render without JS errors` — picks up console warning
- **Fix:**
```diff
// Navbar.jsx — Remove lines 24 and 31
- const refreshToken = useAuthStore((state) => state.refreshToken);
  ...
- await logout(refreshToken);
+ await logout();
```

### BUG-02: AuthPage passes non-existent `refreshToken` to `setAuth`
- **File:** `frontend/src/pages/AuthPage.jsx` (line 147)
- **Symptom:** `setAuth` destructures `{ user, accessToken }` only, but AuthPage passes `refreshToken: authResult.refreshToken`. The extra property is silently ignored but reveals a contract mismatch after the audit removed refresh tokens.
- **Test:** `Authentication → successful registration redirects to dashboard` — registration succeeds but may confuse future developers
- **Fix:**
```diff
// AuthPage.jsx line ~145-148
  setAuth({
    user: authResult.user,
    accessToken: authResult.accessToken,
-   refreshToken: authResult.refreshToken
  });
```

---

## 🟠 HIGH BUGS

### BUG-03: "EMI Calculator" navbar link points to Home (`/`)
- **File:** `frontend/src/components/global/Navbar.jsx` (line 15)
- **Symptom:** The "EMI Calculator" link navigates to `/` (Home page). There is no EMI Calculator page. Users clicking it get no unique content.
- **Test:** `Smoke & Navigation → navbar links navigate correctly` — clicking EMI Calculator goes to `/`
- **Fix:** Either create an EMI page at `/emi-calculator` or remove the broken link:
```diff
// Option A: Remove until feature is built
  const links = [
    { label: "Browse Cars", to: "/listings" },
    { label: "Sell Your Car", to: "/sell" },
    { label: "AI Finder", to: "/ai-finder" },
    { label: "Compare", to: "/compare" },
-   { label: "EMI Calculator", to: "/" }
  ];
```

### BUG-04: Hero CTA "BROWSE CARS" button is not a link — no navigation
- **File:** `frontend/src/components/home/sections/HeroSection.jsx` (line 31-34)
- **Symptom:** The hero "BROWSE CARS" `<Button>` renders a `<button>` element, not an `<a>` link. Clicking it does nothing. It has no `onClick` handler and no `as={Link}` prop.
- **Test:** `Home Page → hero section renders with CTA buttons` — button visible but non-functional
- **Fix:**
```diff
// HeroSection.jsx — Add Link import and wire navigation
+ import { Link } from "react-router-dom";
  ...
- <Button size="lg">
+ <Button as={Link} to="/listings" size="lg">
    BROWSE CARS
    <ArrowRight size={16} strokeWidth={1.5} />
  </Button>
```

### BUG-05: Hero CTA "HOW IT WORKS" button is non-functional
- **File:** `frontend/src/components/home/sections/HeroSection.jsx` (line 35-37)
- **Symptom:** The "HOW IT WORKS" ghost button has no `onClick` handler and no link target. It renders as a non-functional button.
- **Test:** Same as BUG-04, button present but does nothing
- **Fix:** Either link to an anchor section or remove:
```diff
- <Button variant="ghost" size="lg" className="...">
+ <Button variant="ghost" size="lg" className="..." onClick={() => document.getElementById('featured')?.scrollIntoView({ behavior: 'smooth' })}>
    HOW IT WORKS
  </Button>
```

### BUG-06: Browse Body Type section only shows 6 types — test expected "all types" with labels
- **File:** `frontend/src/components/home/sections/BrowseBodyTypeSection.jsx`
- **Symptom:** The body type links use `to={/listings?bodyType=${slug}}` which requires the backend to support String `bodyType` filter param. The backend search endpoint accepts `bodyType` as a String, but the real data only has "SUV", "SEDAN", etc. in uppercase. The frontend sends lowercase slugs (`sedan`, `suv`) — case mismatch.
- **Test:** `Home Page → browse by body type section renders all types` — renders but links may not filter correctly
- **Fix:**
```diff
// BrowseBodyTypeSection.jsx — Use uppercase to match backend enum
  const bodyTypes = [
-   { name: "Sedan", slug: "sedan" },
-   { name: "SUV", slug: "suv" },
-   { name: "Hatchback", slug: "hatchback" },
-   { name: "Coupe", slug: "coupe" },
-   { name: "Truck", slug: "truck" },
-   { name: "Van", slug: "van" }
+   { name: "Sedan", slug: "SEDAN" },
+   { name: "SUV", slug: "SUV" },
+   { name: "Hatchback", slug: "HATCHBACK" },
+   { name: "Coupe", slug: "COUPE" },
+   { name: "Truck", slug: "TRUCK" },
+   { name: "Van", slug: "VAN" }
  ];
```

---

## 🟡 MEDIUM BUGS

### BUG-07: Footer links are all `to="#"` — broken/stub links
- **File:** `frontend/src/components/global/Footer.jsx` (lines 38-41)
- **Symptom:** All footer column links (Browse Cars, Featured Listings, Post Listing, About, Careers, etc.) point to `#`, producing no navigation. Privacy, Terms, Cookies links also point to `#`.
- **Test:** `Smoke & Navigation → footer renders with expected sections` — footer renders but links are dead
- **Fix:** Wire real routes for existing pages:
```diff
  const columns = [
    {
      title: "Buy",
-     links: ["Browse Cars", "Featured Listings", "Certified Cars", "Financing"]
+     links: [
+       { label: "Browse Cars", to: "/listings" },
+       { label: "Featured Listings", to: "/listings?featured=true" },
+       { label: "Certified Cars", to: "/listings?verified=true" },
+       { label: "Financing", to: "#" }
+     ]
    },
    ...
  ];
```

### BUG-08: `console.log("API Base URL:", baseURL)` left in production code
- **File:** `frontend/src/lib/api.js` (line 10)  
- **Symptom:** Every page load logs the API base URL to the browser console. This clutters console output and leaks configuration information.
- **Test:** `AI Finder → no console errors during wizard flow` — although not an error, it triggers console output filtering concerns
- **Fix:**
```diff
// api.js — Remove debug log
- console.log("API Base URL:", baseURL);
```

### BUG-09: Auth page `successful registration` does not reliably redirect to `/dashboard`
- **File:** `frontend/src/pages/AuthPage.jsx` (line 149, line 100)
- **Symptom:** After successful registration, `navigate("/dashboard")` is called (line 149), but the `useEffect` on line 100 that watches `isAuthenticated` also triggers `navigate("/", { replace: true })` — there's a race condition. The user may end up on `/` instead of `/dashboard`.
- **Test:** `Authentication → successful registration redirects to dashboard` — FAILS (~18s timeout waiting for /dashboard)
- **Fix:** The `useEffect` redirect should not override explicit navigation after login. Change the redirect guard:
```diff
// AuthPage.jsx — Only redirect on initial load, not after form submission
  useEffect(() => {
-   if (!isLoading && isAuthenticated) {
+   if (!isLoading && isAuthenticated && !submitting) {
      navigate("/", { replace: true });
    }
- }, [isAuthenticated, isLoading, navigate]);
+ }, [isAuthenticated, isLoading, navigate, submitting]);
```

### BUG-10: ListingsFilterSidebar body type filter sends lowercase values vs backend uppercase
- **File:** `frontend/src/components/listings/ListingsFilterSidebar.jsx`  
- **Symptom:** listingsService `toSearchParams` passes `filters.bodyType` as-is to the backend. The `BrowseBodyTypeSection` sends lowercase (`sedan`), but the DB stores `SEDAN`. The backend search may or may not do case-insensitive match.
- **Related to:** BUG-06
- **Test:** `Listings Page → filter sidebar toggles body type` — timeout waiting for filtered results

### BUG-11: Social icon buttons in footer have no accessible names
- **File:** `frontend/src/components/global/Footer.jsx` (lines 30-34)
- **Symptom:** Social media icon buttons (`Instagram`, `Twitter`, `LinkedIn`, `Youtube`) lack `aria-label` attributes. Screen readers announce them as unnamed buttons.
- **Test:** `Accessibility → buttons have accessible names` — flags unnamed icon buttons
- **Fix:**
```diff
  const socialLinks = [
-   Instagram, Twitter, Linkedin, Youtube
+   { Icon: Instagram, label: "Instagram" },
+   { Icon: Twitter, label: "Twitter" },
+   { Icon: Linkedin, label: "LinkedIn" },
+   { Icon: Youtube, label: "YouTube" }
  ];
  ...
- {socialLinks.map((Icon, index) => (
-   <Button key={index} variant="icon" className="...">
-     <Icon size={18} strokeWidth={1.5} />
+ {socialLinks.map(({ Icon, label }) => (
+   <Button key={label} variant="icon" aria-label={label} className="...">
+     <Icon size={18} strokeWidth={1.5} />
    </Button>
  ))}
```

---

## 🔵 LOW BUGS

### BUG-12: No `<main>` landmark on Home page — `<main>` present only on some pages
- **File:** `frontend/src/pages/HomePage.jsx`
- **Symptom:** The Home page does not wrap content in a `<main>` element. Other pages (ListingsPage, CarDetailPage) do have `<main>`. This means the `<main>` landmark is inconsistent.
- **Test:** `Accessibility → skip-to-content or main landmark exists` — passes only because other pages have it

### BUG-13: Home page heading hierarchy — no H1 before section headings
- **File:** `frontend/src/components/home/sections/HeroSection.jsx`
- **Symptom:** The H1 exists inside HeroSection ("FIND YOUR DREAM MACHINE"). However, sections below (like BrowseBodyTypeSection, FeaturedListingsSection) use H2. The hierarchy is technically correct but depends on section render order.
- **Test:** `Accessibility → page has proper heading hierarchy` — PASSES

### BUG-14: Compare page — empty state shows when visiting `/compare` directly
- **File:** `frontend/src/pages/ComparePage.jsx`
- **Symptom:** When no cars are in the compare store, visiting `/compare` shows an empty state or redirects to listings. This is expected behavior but there's no link/prompt to guide users to add cars.
- **Test:** `Compare Page → shows empty state when no cars selected` — PASSES (correctly shows empty state)

---

## Test Adjustments Needed

The following test failures were due to **test code issues**, not application bugs:

1. **08-post-listing.spec.js** used incorrect route `/post-listing` instead of `/sell` — **FIXED**
2. **Listings-related timeouts** — Backend was temporarily returning 500 during initial test run. Re-testing with backend healthy returns 200. Tests need longer timeouts or retry logic for API-dependent pages.
