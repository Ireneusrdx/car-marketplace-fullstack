# AutoVault E2E Test Summary Report

**Date:** 2026-04-03  
**Framework:** Playwright 1.59.1 + Chromium  
**Project:** desktop-chrome (1280×720)  
**Total Specs:** 10 files · **Total Tests:** 75  

---

## Execution Results (Partial Run — 41/75 completed)

| # | Test | Status | Time |
|---|------|--------|------|
| **01-smoke-and-navigation.spec.js** | | | |
| 1 | All public pages render without JS errors | ❌ FAIL | 29.4s |
| 2 | Navbar links navigate correctly | ❌ FAIL | 21.1s |
| 3 | 404 page renders for unknown routes | ✅ PASS | 2.6s |
| 4 | Footer renders with expected sections | ❌ FAIL | 3.8s |
| 5 | Protected routes redirect to /auth when unauthenticated | ✅ PASS | 6.1s |
| **02-home-page.spec.js** | | | |
| 6 | Hero section renders with CTA buttons | ❌ FAIL | 12.4s |
| 7 | Featured listings section loads car cards | ✅ PASS | 11.7s |
| 8 | Browse by body type section renders all types | ❌ FAIL | 12.6s |
| 9 | AI finder section has a CTA link | ✅ PASS | 5.5s |
| 10 | Stats section shows numerical values | ✅ PASS | 7.0s |
| 11 | Page has correct meta title | ✅ PASS | 6.2s |
| **03-auth.spec.js** | | | |
| 12 | Auth page renders sign-in form by default | ✅ PASS | 6.0s |
| 13 | Can switch between sign-in and create-account tabs | ✅ PASS | 5.4s |
| 14 | Sign-in with empty fields shows validation error | ✅ PASS | 5.7s |
| 15 | Sign-in with invalid email format shows error | ✅ PASS | 5.9s |
| 16 | Sign-in with wrong credentials shows error toast | ✅ PASS | 4.8s |
| 17 | Registration with short password shows error | ✅ PASS | 4.8s |
| 18 | Successful registration redirects to dashboard | ❌ FAIL | 18.0s |
| 19 | Password visibility toggle works | ✅ PASS | 9.7s |
| 20 | Already authenticated user is redirected from /auth | ✅ PASS | 14.0s |
| **04-listings.spec.js** | | | |
| 21 | Page loads and shows car cards from API | ❌ FAIL | 20.6s |
| 22 | Search/filter hero section renders inputs | ❌ FAIL | 19.6s |
| 23 | Filter sidebar toggles body type | ❌ FAIL | 22.5s |
| 24 | Sort dropdown changes listing order | ❌ FAIL | 22.6s |
| 25 | View mode toggle between grid and list | ❌ FAIL | 23.7s |
| 26 | Clicking a car card navigates to detail page | ❌ FAIL | 23.6s |
| 27 | Compare checkbox adds car to compare bar | ❌ FAIL | 19.4s |
| **04-listings.spec.js (Car Detail)** | | | |
| 28 | Renders car info after navigating from listings | ❌ FAIL | 19.6s |
| 29 | Book test drive button requires auth | ❌ FAIL | 20.8s |
| 30 | Share button copies URL to clipboard | ❌ FAIL | 19.3s |
| 31 | Inquiry box opens and submits (auth-gated) | ❌ FAIL | 19.8s |
| 32 | Image gallery navigates between images | ❌ FAIL | 19.5s |
| 33 | Breadcrumb links work | ❌ FAIL | 19.5s |
| **05-ai-finder.spec.js** | | | |
| 34 | Wizard step 1 renders budget slider | ✅ PASS | 2.5s |
| 35 | Budget slider updates displayed value | ✅ PASS | 2.9s |
| 36 | Next button advances to step 2 | ✅ PASS | 3.7s |
| 37 | Can complete full wizard flow | ✅ PASS | 8.0s |
| 38 | Back button returns to previous step | ✅ PASS | 4.5s |
| 39 | No console errors during wizard flow | ❌ FAIL | 6.6s |
| **06-compare.spec.js** | | | |
| 40 | Shows empty state when no cars selected | ✅ PASS | 2.3s |
| 41 | Compare table renders w/ cars from listings | ❌ FAIL | 21.0s |
| 42–75 | *(Remaining tests not executed — killed due to timeout)* | ⏭️ SKIP | — |

---

## Results Summary

| Category | Passed | Failed | Skipped | Total |
|----------|--------|--------|---------|-------|
| Smoke & Navigation | 2 | 3 | 0 | 5 |
| Home Page | 4 | 2 | 0 | 6 |
| Authentication | 7 | 1 | 0 | 8 |
| Listings (Page + Detail) | 0 | 13 | 0 | 13 |
| AI Finder | 5 | 1 | 0 | 6 |
| Compare | 1 | 1 | 1 | 3 |
| Dashboard | — | — | 8 | 8 |
| Post Listing | — | — | 6 | 6 |
| Responsiveness & A11y | — | — | 11 | 11 |
| Performance & API | — | — | 9 | 9 |
| **Totals** | **19** | **21** | **35** | **75** |

**Pass Rate (executed):** 19/41 = **46.3%**

---

## Failure Root Cause Analysis

### Category 1: Backend API Transient 500 (13 failures)
**Tests affected:** All 13 Listings + Car Detail tests (#21–33), Compare #41  
**Root cause:** During the test run, the backend `GET /api/listings` endpoint was intermittently returning HTTP 500. Post-test verification confirmed the endpoint works correctly (200 with 50 listings). These are **environment failures**, not app bugs.  
**Resolution:** Re-run when backend is stable. Consider adding a backend health-check wait in `beforeAll`.

### Category 2: Application Bugs (7 failures)
| Test # | Bug ID | Description |
|--------|--------|-------------|
| 1 | BUG-01 | Console errors from reading undefined `refreshToken` |
| 2 | BUG-03,04,05 | Navbar link issues (EMI calc → /, non-functional CTA buttons) |
| 4 | BUG-07,11 | Footer stub links `#`, missing aria-labels |
| 6 | BUG-04 | Hero CTA not wired to navigation |
| 8 | BUG-06 | Body type case mismatch (lowercase vs uppercase) |
| 18 | BUG-09 | Registration redirect race condition (useEffect vs navigate) |
| 39 | BUG-08 | Console.log in api.js triggers "no console errors" assertion |

### Category 3: Test Timeouts (1 failure)
**Test #41:** Compare page tries to select cars from listings, but listings cards timed out (same as Category 1).

---

## Bugs Found — Summary

| ID | Severity | Component | Description |
|----|----------|-----------|-------------|
| BUG-01 | 🔴 Critical | Navbar.jsx | Reads removed `refreshToken` from auth store |
| BUG-02 | 🔴 Critical | AuthPage.jsx | Passes `refreshToken` to `setAuth` (contract mismatch) |
| BUG-03 | 🟠 High | Navbar.jsx | "EMI Calculator" link → Home `/` (dead feature) |
| BUG-04 | 🟠 High | HeroSection.jsx | "BROWSE CARS" button not linked (no navigation) |
| BUG-05 | 🟠 High | HeroSection.jsx | "HOW IT WORKS" button non-functional |
| BUG-06 | 🟠 High | BrowseBodyTypeSection.jsx | Body type slug case mismatch vs backend |
| BUG-07 | 🟡 Medium | Footer.jsx | All column links point to `#` (dead links) |
| BUG-08 | 🟡 Medium | api.js | `console.log` left in production code |
| BUG-09 | 🟡 Medium | AuthPage.jsx | Registration redirect race (useEffect vs navigate) |
| BUG-10 | 🟡 Medium | ListingsFilterSidebar.jsx | Body type filter case mismatch |
| BUG-11 | 🟡 Medium | Footer.jsx | Social icon buttons missing aria-labels |
| BUG-12 | 🔵 Low | HomePage.jsx | No `<main>` landmark wrapper |
| BUG-13 | 🔵 Low | HeroSection.jsx | Heading hierarchy depends on section order |
| BUG-14 | 🔵 Low | ComparePage.jsx | Empty state lacks guidance to add cars |

---

## Test File Inventory

| File | Tests | Focus Area |
|------|-------|------------|
| `e2e/01-smoke-and-navigation.spec.js` | 5 | Public routes, navbar, 404, footer, auth redirect |
| `e2e/02-home-page.spec.js` | 6 | Hero, featured, body types, AI section, stats, meta |
| `e2e/03-auth.spec.js` | 8 | Form render, tabs, validation, login, register, toggle |
| `e2e/04-listings.spec.js` | 13 | Listings grid, filters, sort, compare; car detail page |
| `e2e/05-ai-finder.spec.js` | 6 | Wizard steps, budget slider, back button, console errors |
| `e2e/06-compare.spec.js` | 3 | Empty state, floating bar, compare table |
| `e2e/07-dashboard.spec.js` | 8 | Auth redirect, 6 dashboard tabs |
| `e2e/08-post-listing.spec.js` | 6 | Auth redirect, wizard steps, validation |
| `e2e/09-responsiveness-a11y.spec.js` | 11 | Mobile/tablet viewports, ARIA, keyboard nav, headings |
| `e2e/10-performance-api.spec.js` | 9 | Load times, CLS, API errors, offline handling, 401 |

---

## Recommendations

1. **Fix all 🔴 Critical and 🟠 High bugs** before next release — see `QA_BUG_REPORT.md` for exact code diffs
2. **Re-run full suite** once backend is stable with all fixes applied
3. **Add backend health check** in test `beforeAll` to avoid false negatives from transient 500s
4. **Increase test timeout** for listings-dependent tests to 30s (API + rendering)
5. **Enable multi-project runs** (mobile + tablet) after desktop tests reach >90% pass rate
6. **CI integration:** Add `npx playwright test --project=desktop-chrome` to CI pipeline

---

*Generated by Playwright E2E automation suite — AutoVault QA*
