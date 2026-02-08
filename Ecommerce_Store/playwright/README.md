# Playwright E2E Tests

These Playwright tests cover the following flows:

- login
- add-to-cart
- checkout

## Prerequisites

1. Start the Spring Boot app locally (default port 8080) with the `e2e` profile so the reset endpoint and seed data are available:

   ```bash
   SPRING_PROFILES_ACTIVE=e2e ./mvnw spring-boot:run
   ```
2. The `e2e` profile seeds a `White Shirt` product with high stock and exposes `/e2e/reset` for test resets.
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