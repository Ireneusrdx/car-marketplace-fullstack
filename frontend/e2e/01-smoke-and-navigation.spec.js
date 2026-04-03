// e2e/01-smoke-and-navigation.spec.js
// Tests: UI rendering, broken links, navigation flow, console errors
import { test, expect } from "@playwright/test";
import { waitForApp, attachErrorListener } from "./helpers.js";

const PUBLIC_ROUTES = [
  { path: "/", title: /AutoVault/i, heading: /find your/i },
  { path: "/listings", title: /listings|browse/i, heading: /explore|browse|find/i },
  { path: "/ai-finder", title: /ai finder/i, heading: /budget|find/i },
  { path: "/compare", title: /compare/i, heading: /compare/i },
  { path: "/auth", title: /sign in|auth/i, heading: /welcome|sign in/i },
];

test.describe("Smoke & Navigation", () => {
  test("all public pages render without JS errors", async ({ page }) => {
    const jsErrors = attachErrorListener(page);

    for (const route of PUBLIC_ROUTES) {
      await page.goto(route.path);
      await waitForApp(page);

      // Verify page loaded (has content beyond blank shell)
      const body = await page.locator("body").innerText();
      expect(body.length).toBeGreaterThan(50);
    }

    // Allow known benign errors (e.g., failed API calls when backend seed data absent)
    const critical = jsErrors.filter(
      (e) => !e.includes("net::ERR") && !e.includes("NetworkError") && !e.includes("AxiosError")
    );
    expect(critical).toEqual([]);
  });

  test("navbar links navigate correctly", async ({ page }) => {
    await page.goto("/");
    await waitForApp(page);

    // Browse Cars
    await page.getByRole("link", { name: /browse cars/i }).first().click();
    await expect(page).toHaveURL(/\/listings/);

    // AI Finder
    await page.getByRole("link", { name: /ai finder/i }).first().click();
    await expect(page).toHaveURL(/\/ai-finder/);

    // Compare
    await page.getByRole("link", { name: /compare/i }).first().click();
    await expect(page).toHaveURL(/\/compare/);

    // Logo navigates home
    await page.locator("nav").getByRole("link").first().click();
    await expect(page).toHaveURL("/");
  });

  test("404 page renders for unknown routes", async ({ page }) => {
    await page.goto("/this-does-not-exist-xyz");
    await waitForApp(page);

    await expect(page.getByText(/404/)).toBeVisible();
    await expect(page.getByText(/page not found/i)).toBeVisible();

    // "Go Home" button works
    await page.getByRole("link", { name: /go home/i }).click();
    await expect(page).toHaveURL("/");
  });

  test("footer renders with expected sections", async ({ page }) => {
    await page.goto("/");
    await waitForApp(page);

    const footer = page.locator("footer");
    await expect(footer).toBeVisible();
    await expect(footer.getByText(/auto/i)).toBeVisible();
    await expect(footer.getByText(/privacy/i)).toBeVisible();
    await expect(footer.getByText(/terms/i)).toBeVisible();
  });

  test("protected routes redirect to /auth when unauthenticated", async ({ page }) => {
    await page.goto("/dashboard");
    await page.waitForURL("**/auth", { timeout: 10000 });
    await expect(page).toHaveURL(/\/auth/);

    await page.goto("/sell");
    await page.waitForURL("**/auth", { timeout: 10000 });
    await expect(page).toHaveURL(/\/auth/);
  });
});
