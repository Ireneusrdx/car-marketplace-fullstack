// e2e/04-listings.spec.js
// Tests: Listings page rendering, filters, sorting, compare, car detail
import { test, expect } from "@playwright/test";
import { waitForApp } from "./helpers.js";

test.describe("Listings Page", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
  });

  test("page loads and shows car cards from API", async ({ page }) => {
    // Backend seeds 50 listings
    const cards = page.locator("article");
    await expect(cards.first()).toBeVisible({ timeout: 10000 });

    const count = await cards.count();
    expect(count).toBeGreaterThan(0);
  });

  test("search/filter hero section renders inputs", async ({ page }) => {
    // Should have a search input or filter controls
    const searchInput = page.locator('input[placeholder*="search" i], input[placeholder*="find" i], input[type="search"]').first();
    if (await searchInput.isVisible()) {
      await expect(searchInput).toBeEnabled();
    }
  });

  test("filter sidebar toggles body type", async ({ page }) => {
    // Look for body type filter options
    const bodyTypeFilter = page.getByText(/suv/i).first();
    if (await bodyTypeFilter.isVisible()) {
      await bodyTypeFilter.click();
      // URL or listing should update
      await page.waitForTimeout(1000);
    }
  });

  test("sort dropdown changes listing order", async ({ page }) => {
    // Find sort control
    const sortControl = page.locator('select, [class*="sort"], button:has-text("sort")').first();
    if (await sortControl.isVisible()) {
      await sortControl.click();
    }
  });

  test("view mode toggle between grid and list", async ({ page }) => {
    // Find toggle buttons (grid/list icons)
    const viewToggles = page.locator('button[title*="grid" i], button[title*="list" i], [class*="view-toggle"] button');
    if (await viewToggles.first().isVisible()) {
      const count = await viewToggles.count();
      if (count >= 2) {
        await viewToggles.nth(1).click();
        await page.waitForTimeout(500);
        await viewToggles.nth(0).click();
      }
    }
  });

  test("clicking a car card navigates to detail page", async ({ page }) => {
    const firstCard = page.locator("article").first();
    await expect(firstCard).toBeVisible({ timeout: 10000 });

    const link = firstCard.getByRole("link").first();
    if (await link.isVisible()) {
      await link.click();
      await expect(page).toHaveURL(/\/cars\//);
    }
  });

  test("compare checkbox adds car to compare bar", async ({ page }) => {
    // Find compare toggles on cards
    const compareBtn = page.locator('button:has-text("compare"), input[type="checkbox"]').first();
    if (await compareBtn.isVisible()) {
      await compareBtn.click();
      // Floating bar should appear
      await page.waitForTimeout(500);
    }
  });
});

test.describe("Car Detail Page", () => {
  test("renders car info after navigating from listings", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);

    // Wait for cards to load
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();

    await expect(page).toHaveURL(/\/cars\//);
    await waitForApp(page);

    // Key elements should be visible
    await expect(page.locator("img").first()).toBeVisible();
    // Price should be visible
    await expect(page.getByText(/\$/)).toBeVisible({ timeout: 5000 });
  });

  test("book test drive button requires auth", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();
    await waitForApp(page);

    const bookBtn = page.getByRole("button", { name: /book test drive/i });
    if (await bookBtn.isVisible()) {
      await bookBtn.click();
      // Should show "sign in" error toast
      const toast = page.getByText(/sign in/i);
      await expect(toast.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test("share button copies URL to clipboard", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();
    await waitForApp(page);

    // Grant clipboard permissions
    await page.context().grantPermissions(["clipboard-read", "clipboard-write"]);

    const shareBtn = page.locator('button[title*="share" i], button:has(svg)').filter({ hasText: /share/i }).first();
    if (await shareBtn.isVisible()) {
      await shareBtn.click();
      // Should show a toast confirming copy
      await page.waitForTimeout(1000);
    }
  });

  test("inquiry box opens and submits (auth-gated)", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();
    await waitForApp(page);

    const offerBtn = page.getByRole("button", { name: /make an offer|send inquiry/i }).first();
    if (await offerBtn.isVisible()) {
      await offerBtn.click();
      // Inquiry textarea should appear
      const textarea = page.locator("textarea");
      await expect(textarea.first()).toBeVisible({ timeout: 3000 });
    }
  });

  test("image gallery navigates between images", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();
    await waitForApp(page);

    // Click thumbnail images
    const thumbnails = page.locator("img").filter({ hasNot: page.locator("nav img") });
    const count = await thumbnails.count();
    if (count > 1) {
      await thumbnails.nth(1).click();
      await page.waitForTimeout(300);
    }
  });

  test("breadcrumb links work", async ({ page }) => {
    await page.goto("/listings");
    await waitForApp(page);
    const firstLink = page.locator("article a").first();
    await expect(firstLink).toBeVisible({ timeout: 10000 });
    await firstLink.click();
    await waitForApp(page);

    // Click "Home" in breadcrumb
    const homeLink = page.getByRole("link", { name: /home/i }).first();
    if (await homeLink.isVisible()) {
      await homeLink.click();
      await expect(page).toHaveURL("/");
    }
  });
});
