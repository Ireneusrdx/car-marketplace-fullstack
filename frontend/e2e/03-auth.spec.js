// e2e/03-auth.spec.js
// Tests: Sign-in form validation, registration, auth flows, edge cases
import { test, expect } from "@playwright/test";
import { waitForApp, attachErrorListener } from "./helpers.js";

test.describe("Authentication", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/auth");
    await waitForApp(page);
  });

  test("auth page renders sign-in form by default", async ({ page }) => {
    await expect(page.getByText(/welcome back/i).or(page.getByText(/sign in/i)).first()).toBeVisible();
    await expect(page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first()).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
  });

  test("can switch between sign-in and create-account tabs", async ({ page }) => {
    // Switch to create account
    await page.getByRole("button", { name: /create account/i }).first().click();
    await expect(page.getByLabel(/full name/i).or(page.getByPlaceholder(/name/i)).first()).toBeVisible();

    // Switch back to sign in
    await page.getByRole("button", { name: /sign in/i }).first().click();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
  });

  test("sign-in with empty fields shows validation error", async ({ page }) => {
    // Click sign-in without filling anything
    await page.getByRole("button", { name: /sign in/i }).last().click();

    // Should show some form of validation (toast, inline error, or HTML5 validation)
    const emailInput = page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first();
    const isInvalid = await emailInput.evaluate((el) => !el.checkValidity());
    expect(isInvalid).toBe(true);
  });

  test("sign-in with invalid email format shows error", async ({ page }) => {
    await page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first().fill("not-an-email");
    await page.locator('input[type="password"]').first().fill("password123");
    await page.getByRole("button", { name: /sign in/i }).last().click();

    // Either HTML5 validation or toast
    const toast = page.locator('[class*="toast"], [role="status"], [role="alert"]');
    const emailInput = page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first();
    const isInvalid = await emailInput.evaluate((el) => !el.checkValidity());
    if (!isInvalid) {
      await expect(toast.first()).toBeVisible({ timeout: 5000 });
    }
  });

  test("sign-in with wrong credentials shows error toast", async ({ page }) => {
    await page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first().fill("nonexistent@test.com");
    await page.locator('input[type="password"]').first().fill("WrongPass123!");
    await page.getByRole("button", { name: /sign in/i }).last().click();

    // Expect an error toast or message
    const errorIndicator = page.locator('[class*="toast"], [role="alert"]').or(
      page.getByText(/unable|invalid|failed|error/i)
    );
    await expect(errorIndicator.first()).toBeVisible({ timeout: 8000 });
  });

  test("registration with short password shows error", async ({ page }) => {
    await page.getByRole("button", { name: /create account/i }).first().click();

    await page.getByLabel(/full name/i).or(page.getByPlaceholder(/name/i)).first().fill("Test User");
    await page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first().fill("short@test.com");
    await page.locator('input[type="password"]').first().fill("123"); // Too short
    await page.getByRole("button", { name: /create account/i }).last().click();

    // Expect validation message about password length
    const errorIndicator = page.locator('[class*="toast"], [role="alert"]').or(
      page.getByText(/min|short|password|8 char/i)
    );
    await expect(errorIndicator.first()).toBeVisible({ timeout: 8000 });
  });

  test("successful registration redirects to dashboard", async ({ page }) => {
    const ts = Date.now();
    await page.getByRole("button", { name: /create account/i }).first().click();

    await page.getByLabel(/full name/i).or(page.getByPlaceholder(/name/i)).first().fill(`E2E User ${ts}`);
    await page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first().fill(`e2e_${ts}@test.local`);
    await page.locator('input[type="password"]').first().fill("Test1234!@");
    await page.getByRole("button", { name: /create account/i }).last().click();

    // Should redirect to dashboard
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });

  test("password visibility toggle works", async ({ page }) => {
    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.fill("secret123");

    // Find and click the toggle (eye icon button)
    const toggle = page.locator('button:near(input[type="password"])').or(
      page.locator('[class*="password"] button, [class*="eye"]')
    ).first();

    if (await toggle.isVisible()) {
      await toggle.click();
      // After toggle, input type should be "text"
      await expect(page.locator('input[value="secret123"]').first()).toHaveAttribute("type", "text");
    }
  });

  test("already authenticated user is redirected from /auth", async ({ page }) => {
    // Register first
    const ts = Date.now();
    await page.getByRole("button", { name: /create account/i }).first().click();
    await page.getByLabel(/full name/i).or(page.getByPlaceholder(/name/i)).first().fill(`Redir User ${ts}`);
    await page.getByLabel(/email/i).or(page.locator('input[type="email"]')).first().fill(`redir_${ts}@test.local`);
    await page.locator('input[type="password"]').first().fill("Test1234!@");
    await page.getByRole("button", { name: /create account/i }).last().click();
    await page.waitForURL("**/dashboard", { timeout: 10000 });

    // Now visit /auth again — should redirect away
    await page.goto("/auth");
    await page.waitForTimeout(2000);
    const url = page.url();
    // Should NOT still be on /auth (redirected to / or /dashboard)
    expect(url).not.toMatch(/\/auth$/);
  });
});
