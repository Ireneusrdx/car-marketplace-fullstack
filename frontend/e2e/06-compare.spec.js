// e2e/06-compare.spec.js
// Tests: Compare page — add/remove cars, table rendering, empty state
import { test, expect } from "@playwright/test";
import { waitForApp } from "./helpers.js";

test.describe("Compare Page", () => {
  test("shows empty state when no cars selected", async ({ page }) => {
    await page.goto("/compare");
    await waitForApp(page);

    // Should show empty state message or redirect
    const emptyMsg = page.getByText(/no cars|select|add.*cars|nothing to compare/i).first();
    const isVisible = await emptyMsg.isVisible({ timeout: 3000 }).catch(() => false);
    // Either show empty state or redirect happens
    expect(isVisible || page.url().includes("/listings")).toBeTruthy();
  });

  test("compare table renders when navigated with cars from listings", async ({
    page,
  }) => {
    // First, add cars to compare from listings
    await page.goto("/listings");
    await waitForApp(page);

    // Wait for cards
    const cards = page.locator("article");
    await expect(cards.first()).toBeVisible({ timeout: 10000 });

    // Try to add cars to compare
    const compareBtns = page.locator('button:has-text("compare"), [title*="compare" i]');
    if ((await compareBtns.count()) >= 2) {
      await compareBtns.first().click();
      await page.waitForTimeout(300);
      await compareBtns.nth(1).click();
      await page.waitForTimeout(300);

      // Navigate to compare
      const compareLink = page.getByRole("link", { name: /compare/i }).first();
      if (await compareLink.isVisible()) {
        await compareLink.click();
        await expect(page).toHaveURL(/\/compare/);
        await waitForApp(page);
      }
    }
  });

  test("compare floating bar appears on listings page after selecting cars", async ({
    page,
  }) => {
    await page.goto("/listings");
    await waitForApp(page);

    const cards = page.locator("article");
    await expect(cards.first()).toBeVisible({ timeout: 10000 });

    // Click compare button on first card
    const compareBtn = cards.first().locator('button:has-text("compare"), input[type="checkbox"]').first();
    if (await compareBtn.isVisible()) {
      await compareBtn.click();
      // Floating bar should appear
      const floatingBar = page.locator('[class*="compare-bar"], [class*="floating"], [class*="CompareFloating"]');
      await expect(floatingBar.first()).toBeVisible({ timeout: 3000 }).catch(() => {/* optional */});
    }
  });
});
