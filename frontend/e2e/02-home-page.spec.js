// e2e/02-home-page.spec.js
// Tests: Hero section, featured listings, CTA links, section rendering
import { test, expect } from "@playwright/test";
import { waitForApp } from "./helpers.js";

test.describe("Home Page", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await waitForApp(page);
  });

  test("hero section renders with CTA buttons", async ({ page }) => {
    // Should have a primary CTA linking to browse or search
    const heroCTA = page.locator("section").first().getByRole("link").first();
    await expect(heroCTA).toBeVisible();
  });

  test("featured listings section loads car cards", async ({ page }) => {
    // Wait for API data to load (backend seeds 50 listings)
    const cards = page.locator('[class*="CarCard"], article').first();
    await expect(cards).toBeVisible({ timeout: 10000 });
  });

  test("browse by body type section renders all types", async ({ page }) => {
    const bodyTypes = ["sedan", "suv", "hatchback", "coupe", "truck"];
    for (const type of bodyTypes) {
      await expect(
        page.getByText(new RegExp(type, "i")).first()
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test("AI finder section has a CTA link", async ({ page }) => {
    const aiSection = page.getByText(/smart car/i).or(page.getByText(/ai/i)).first();
    await expect(aiSection).toBeVisible({ timeout: 5000 });
  });

  test("stats section shows numerical values", async ({ page }) => {
    // Stats like "10,000+ cars listed" or similar counters
    const statNumbers = page.locator("text=/\\d+[,+]?/").first();
    await expect(statNumbers).toBeVisible({ timeout: 5000 });
  });

  test("page has correct meta title", async ({ page }) => {
    await expect(page).toHaveTitle(/autovault/i);
  });
});
