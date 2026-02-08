# Playwright E2E Tests

These Playwright tests cover the following flows:

- login
- add-to-cart
- checkout

## Prerequisites

1. Start the Spring Boot app locally (default port 8080).
2. Ensure the database has at least one active product so the test can open a product detail page.

## Install dependencies

```bash
cd /workspace/ECommerceStore/Ecommerce_Store/playwright
npm install
npx playwright install
```

## Run tests

```bash
npm test
```

## Configuration

- `PLAYWRIGHT_BASE_URL`: override the app URL (default: `http://localhost:8080`).

Example:

```bash
PLAYWRIGHT_BASE_URL=http://localhost:8080 npm test
```