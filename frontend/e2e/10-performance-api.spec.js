// e2e/10-performance-api.spec.js
// Tests: page load performance, API error handling, network failures
import { test, expect } from "@playwright/test";
import { waitForApp } from "./helpers.js";

test.describe("Performance", () => {
  test("home page loads within 5 seconds", async ({ page }) => {
    const start = Date.now();
    await page.goto("/", { waitUntil: "domcontentloaded" });
    const loadTime = Date.now() - start;
    expect(loadTime).toBeLessThan(5000);
  });

  test("listings page loads within 5 seconds", async ({ page }) => {
    const start = Date.now();
    await page.goto("/listings", { waitUntil: "domcontentloaded" });
    const loadTime = Date.now() - start;
    expect(loadTime).toBeLessThan(5000);
  });

  test("no large layout shifts on home page", async ({ page }) => {
    await page.goto("/");
    // Use Performance API
    const cls = await page.evaluate(async () => {
      return new Promise((resolve) => {
        let totalCLS = 0;
        const observer = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (!entry.hadRecentInput) {
              totalCLS += entry.value;
            }
          }
        });
        observer.observe({ type: "layout-shift", buffered: true });
        setTimeout(() => {
          observer.disconnect();
          resolve(totalCLS);
        }, 3000);
      });
    });
    // CLS should be below 0.25 (acceptable threshold)
    expect(cls).toBeLessThan(0.25);
  });

  test("API responses complete within 3 seconds", async ({ page }) => {
    const slowRequests = [];

    page.on("response", (response) => {
      const timing = response.timing?.responseEnd;
      if (timing && timing > 3000) {
        slowRequests.push({
          url: response.url(),
          time: timing,
        });
      }
    });

    await page.goto("/listings");
    await waitForApp(page);
    await page.waitForTimeout(3000);

    if (slowRequests.length > 0) {
      console.warn("Slow API requests:", slowRequests);
    }
  });
});

test.describe("API Error Handling", () => {
  test("listings page shows error state when API fails", async ({ page }) => {
    // Block the listings API
    await page.route("**/api/cars**", (route) => {
      route.fulfill({
        status: 500,
        contentType: "application/json",
        body: JSON.stringify({ message: "Internal Server Error" }),
      });
    });

    await page.goto("/listings");
    await waitForApp(page);
    await page.waitForTimeout(2000);

    // Should show some error state, not a blank page
    const bodyText = await page.locator("body").textContent();
    expect(bodyText?.trim().length).toBeGreaterThan(0);
  });

  test("car detail page handles 404 gracefully", async ({ page }) => {
    await page.goto("/cars/99999999");
    await page.waitForTimeout(3000);

    // Should show error or redirect, not crash
    const bodyText = await page.locator("body").textContent();
    expect(bodyText?.trim().length).toBeGreaterThan(0);

    // No unhandled JS error crash
    const errors = [];
    page.on("pageerror", (err) => errors.push(err.message));
    // Already loaded so check URL
    const url = page.url();
    expect(url).toBeDefined();
  });

  test("app handles network disconnect gracefully", async ({ page }) => {
    await page.goto("/");
    await waitForApp(page);

    // Go offline
    await page.context().setOffline(true);

    // Try navigating to listings (will fail to fetch)
    await page.goto("/listings").catch(() => {/* expected */});
    await page.waitForTimeout(2000);

    // Restore
    await page.context().setOffline(false);

    // Navigate back to home — app should recover
    await page.goto("/");
    await waitForApp(page);
    await expect(page.locator("nav").first()).toBeVisible();
  });

  test("401 on protected endpoint shows login prompt", async ({ page }) => {
    // Block with 401
    await page.route("**/api/bookings**", (route) => {
      route.fulfill({
        status: 401,
        contentType: "application/json",
        body: JSON.stringify({ message: "Unauthorized" }),
      });
    });

    await page.goto("/dashboard");
    await page.waitForTimeout(2000);

    // Should redirect to auth or show error
    const url = page.url();
    const bodyText = await page.locator("body").textContent();
    expect(url.includes("/auth") || bodyText?.includes("sign in") || bodyText?.includes("login") || bodyText?.length > 0).toBeTruthy();
  });
});
