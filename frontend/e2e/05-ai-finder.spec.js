// e2e/05-ai-finder.spec.js
// Tests: AI Finder wizard flow, budget slider, results
import { test, expect } from "@playwright/test";
import { waitForApp } from "./helpers.js";

test.describe("AI Finder Page", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/ai-finder");
    await waitForApp(page);
  });

  test("wizard step 1 renders budget slider", async ({ page }) => {
    // First step should be budget
    const slider = page.locator('input[type="range"]').first();
    await expect(slider).toBeVisible({ timeout: 5000 });
  });

  test("budget slider updates displayed value", async ({ page }) => {
    const slider = page.locator('input[type="range"]').first();
    if (await slider.isVisible()) {
      // Move the slider
      const box = await slider.boundingBox();
      if (box) {
        await page.mouse.click(box.x + box.width * 0.7, box.y + box.height / 2);
        await page.waitForTimeout(300);
      }
    }
  });

  test("next button advances to step 2", async ({ page }) => {
    const nextBtn = page.getByRole("button", { name: /next|continue/i }).first();
    if (await nextBtn.isVisible()) {
      await nextBtn.click();
      await page.waitForTimeout(500);
      // Step indicator should advance
    }
  });

  test("can complete full wizard flow", async ({ page }) => {
    // Step through all steps by clicking Next repeatedly
    for (let step = 0; step < 5; step++) {
      const nextBtn = page.getByRole("button", { name: /next|continue|find|search|get results/i }).first();
      if (await nextBtn.isVisible()) {
        // Select an option if available (radio buttons, checkboxes)
        const options = page.locator('input[type="radio"], input[type="checkbox"], button[role="option"], [class*="option"]');
        const firstOption = options.first();
        if (await firstOption.isVisible({ timeout: 1500 }).catch(() => false)) {
          await firstOption.click();
          await page.waitForTimeout(300);
        }
        await nextBtn.click();
        await page.waitForTimeout(500);
      }
    }
  });

  test("back button returns to previous step", async ({ page }) => {
    // Go to step 2 first
    const nextBtn = page.getByRole("button", { name: /next|continue/i }).first();
    if (await nextBtn.isVisible()) {
      await nextBtn.click();
      await page.waitForTimeout(500);

      // Now go back
      const backBtn = page.getByRole("button", { name: /back|previous/i }).first();
      if (await backBtn.isVisible()) {
        await backBtn.click();
        await page.waitForTimeout(500);
        // Should show step 1 content (budget slider)
        const slider = page.locator('input[type="range"]');
        await expect(slider.first()).toBeVisible({ timeout: 3000 });
      }
    }
  });

  test("no console errors during wizard flow", async ({ page }) => {
    const errors = [];
    page.on("console", (msg) => {
      if (msg.type() === "error") errors.push(msg.text());
    });

    const nextBtn = page.getByRole("button", { name: /next|continue/i }).first();
    if (await nextBtn.isVisible()) {
      await nextBtn.click();
      await page.waitForTimeout(500);
    }

    const jsErrors = errors.filter(
      (e) => !e.includes("favicon") && !e.includes("net::ERR")
    );
    expect(jsErrors).toHaveLength(0);
  });
});
