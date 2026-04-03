// e2e/helpers.js — shared utilities for all tests
import { expect } from "@playwright/test";

/** Wait for the React app shell to be fully mounted */
export async function waitForApp(page) {
  await page.waitForSelector("nav", { timeout: 15000 });
}

/** Collect and return all browser console errors during a callback */
export async function collectConsoleErrors(page, fn) {
  const errors = [];
  const handler = (msg) => {
    if (msg.type() === "error") errors.push(msg.text());
  };
  page.on("console", handler);
  await fn();
  page.off("console", handler);
  return errors;
}

/** Register a new test user via the UI and return credentials */
export async function registerTestUser(page) {
  const ts = Date.now();
  const email = `e2e_${ts}@test.local`;
  const password = "Test1234!@";
  const name = `E2E User ${ts}`;

  await page.goto("/auth");
  await page.getByRole("button", { name: /create account/i }).first().click();
  await page.getByLabel(/full name/i).fill(name);
  await page.getByLabel(/email/i).fill(email);
  await page.locator('input[type="password"]').fill(password);
  await page.getByRole("button", { name: /create account/i }).last().click();

  // Wait for redirect to dashboard
  await page.waitForURL("**/dashboard", { timeout: 10000 }).catch(() => {});

  return { email, password, name };
}

/** Sign in with email/password via the UI */
export async function signIn(page, email, password) {
  await page.goto("/auth");
  await page.getByLabel(/email/i).fill(email);
  await page.locator('input[type="password"]').fill(password);
  await page.getByRole("button", { name: /sign in/i }).last().click();
  await page.waitForURL("**/dashboard", { timeout: 10000 }).catch(() => {});
}

/** Ensure no unhandled JS errors on a page */
export function attachErrorListener(page) {
  const errors = [];
  page.on("pageerror", (err) => errors.push(err.message));
  return errors;
}
