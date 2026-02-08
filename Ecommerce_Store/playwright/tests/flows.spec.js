const { test, expect } = require('@playwright/test');
const path = require('path');

const adminCredentials = {
    email: 'admin2@example.com',
    password: 'Admin2!',
};

const buildTestUser = () => {
    const timestamp = Date.now();
    return {
        name: `Marko Markovski ${timestamp}`,
        mobile: '070123456',
        email: `marko.markovski${timestamp}@example.com`,
        address: 'Ruzvelt 28',
        city: 'Skopje',
        state: 'Karpos',
        pinCode: '1000',
        password: 'P@ssword1234',
    };
};

async function registerUser(page, user) {
    await page.goto('/register');
    await page.locator('input[name="name"]').fill(user.name);
    await page.locator('input[name="mobile"]').fill(user.mobile);
    await page.locator('input[name="email"]').fill(user.email);
    await page.locator('input[name="address"]').fill(user.address);
    await page.locator('input[name="city"]').fill(user.city);
    await page.locator('input[name="state"]').fill(user.state);
    await page.locator('input[name="pinCode"]').fill(user.pinCode);
    await page.locator('input[name="password"]').fill(user.password);
    await page.locator('input[name="confirmPassword"]').fill(user.password);
    await page.getByRole('button', { name: 'Register' }).click();
    await expect(page.locator('body')).toContainText('User Registered Successfully');
}

async function loginUser(page, user) {
    await page.goto('/signin');
    await page.locator('input[name="username"]').fill(user.email);
    await page.locator('input[name="password"]').fill(user.password);
    await page.getByRole('button', { name: 'Submit' }).click();
    await expect(page).not.toHaveURL(/signin\?error/);
}

async function loginWithCredentials(page, credentials) {
    await page.goto('/signin');
    await page.locator('input[name="username"]').fill(credentials.email);
    await page.locator('input[name="password"]').fill(credentials.password);
    await page.getByRole('button', { name: 'Submit' }).click();
    await expect(page).not.toHaveURL(/signin\?error/);
}

async function openFirstProduct(page) {
    await page.goto('/products');
    const detailLinks = page.getByRole('link', { name: 'Details' });
    await expect(detailLinks.first(), 'Expected at least one product to be listed. Seed the DB with products for this test.')
        .toBeVisible();
    await detailLinks.first().click();
}

async function openProductByName(page, productName) {
    await page.goto('/products');
    await page.locator('input[name="search"]').fill(productName);
    await page.getByRole('button', { name: 'Search Product' }).click();
    const productTitle = page.getByText(productName, { exact: false });
    await expect(productTitle, `Expected product titled "${productName}" to be visible in search results.`).toBeVisible();
    await productTitle.locator('xpath=ancestor::div[contains(@class,"card-body")]').getByRole('link', { name: 'Details' }).click();
}


async function addProductToCartFromDetail(page, quantity = null) {
    if (quantity !== null) {
        await page.locator('input[name="quantity"]').fill(String(quantity));
    }
    await Promise.all([
        page.waitForURL(/\/product\//),
        page.getByRole('button', { name: 'Add To Cart' }).click(),
    ]);
}


async function getCartQuantity(page) {
    const quantityCell = page.locator('table tbody tr').first().locator('td').nth(3);
    const rawText = await quantityCell.innerText();
    const match = rawText.match(/(\d+)/);
    return match ? Number(match[1]) : null;
}

async function fetchLastResetUrl(request) {
    const response = await request.get('/test/emails/last');
    expect(response.ok(), 'Expected test email endpoint to return a reset link.').toBeTruthy();
    const payload = await response.json();
    return payload.resetUrl;
}

async function hasCartErrorMessage(page, message) {
    return page.locator('body').filter({ hasText: message }).isVisible();
}

function escapeRegex(value) {
    return value.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&');
}

function parsePrice(value) {
    const normalized = value.replace(/[^0-9.]/g, '');
    return normalized ? Number.parseFloat(normalized) : 0;
}

function getFixturePath(fileName) {
    return path.resolve(__dirname, '..', '..', 'src', 'main', 'resources', 'static', 'img', fileName);
}

test.describe('E2E flows', () => {

    test.beforeEach(async ({ request }) => {
        const response = await request.post('/e2e/reset');
        if (!response.ok()) {
            throw new Error('E2E reset failed. Ensure the app is running with SPRING_PROFILES_ACTIVE=e2e.');
        }
    });

    test('login flow', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);
        await expect(page).toHaveURL(/\/(products|)$/);
    });

    test('add-to-cart flow', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);

        await page.getByRole('button', { name: 'Add To Cart' }).click();
        await page.goto('/user/cart');

        const cartRows = page.locator('table tbody tr');
        await expect(cartRows.first()).toBeVisible();
    });

    test('checkout flow', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);
        await addProductToCartFromDetail(page);

        await page.goto('/user/cart');
        await page.getByRole('link', { name: 'Proceed Payment' }).click();

        await expect(page).toHaveURL(/\/user\/order-success/);
        await expect(page.getByRole('heading', { name: 'Order Placed Successfully!' })).toBeVisible();
    });

    test('logout flow', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await page.getByRole('link', { name: 'Logout' }).click();
        await expect(page).toHaveURL(/\/signin\?logout/);
        await expect(page.locator('body')).toContainText('Logout Successfully');
    });

    test('cart quantity update flow', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);
        await addProductToCartFromDetail(page);
        await page.goto('/user/cart');

        const initialQuantity = await getCartQuantity(page);
        await page.locator('table tbody tr').first().getByRole('link').nth(1).click();
        await page.waitForURL(/\/user\/cart/);
        const increasedQuantity = await getCartQuantity(page);
        if (await hasCartErrorMessage(page, 'Requested quantity is unavailable.')) {
            test.skip(true, 'Stock limit reached; cannot increase quantity for this product.');
            return;
        }
        await expect(increasedQuantity).toBe(initialQuantity + 1);

        await page.locator('table tbody tr').first().getByRole('link').first().click();
        await page.waitForURL(/\/user\/cart/);
        const decreasedQuantity = await getCartQuantity(page);
        await expect(decreasedQuantity).toBe(initialQuantity);
    });


    test('empty cart checkout shows error', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await page.goto('/user/orders');
        await expect(page).toHaveURL(/\/user\/cart/);
        await expect(page.locator('body')).toContainText('Your cart is empty.');
    });

    test('admin page requires authentication', async ({ page }) => {
        await page.goto('/admin/');
        await expect(page).toHaveURL(/\/signin/);
    });

    test('admin can manage categories', async ({ page }) => {
        await loginWithCredentials(page, adminCredentials);

        const timestamp = Date.now();
        const categoryName = `E2E Category ${timestamp}`;
        const updatedCategoryName = `E2E Category ${timestamp} Updated`;

        await page.goto('/admin/add-category');
        await page.locator('input[name="categoryName"]').fill(categoryName);
        await page.setInputFiles('input[name="file"]', getFixturePath('banner1.jpg'));
        await Promise.all([
            page.waitForURL(/\/admin\/category/),
            page.getByRole('button', { name: 'ADD' }).click(),
        ]);

        const categoryRow = page.locator('table tbody tr', { hasText: categoryName });
        await expect(categoryRow).toBeVisible();
        await categoryRow.locator('a').first().click();

        await page.locator('input[name="categoryName"]').fill(updatedCategoryName);
        await page.locator('input[name="isActive"][value="false"]').check();
        await page.setInputFiles('input[name="file"]', getFixturePath('banner2.jpg'));
        await Promise.all([
            page.waitForURL(/\/admin\/category/),
            page.getByRole('button', { name: 'Update' }).click(),
        ]);

        const updatedRow = page.locator('table tbody tr', { hasText: updatedCategoryName });
        await expect(updatedRow).toBeVisible();

        page.once('dialog', (dialog) => dialog.accept());
        await Promise.all([
            page.waitForURL(/\/admin\/category/),
            updatedRow.locator('a').nth(1).click(),
        ]);
        await expect(page.locator('table tbody tr', { hasText: updatedCategoryName })).toHaveCount(0);
    });

    test('admin can manage products', async ({ page }) => {
        await loginWithCredentials(page, adminCredentials);

        const timestamp = Date.now();
        const productTitle = `E2E Product ${timestamp}`;
        const updatedTitle = `E2E Product ${timestamp} Updated`;

        await page.goto('/admin/add-product');
        await page.locator('input[name="productTitle"]').fill(productTitle);
        await page.locator('textarea[name="productDescription"]').fill('Seeded product for admin CRUD test.');
        await page.locator('select[name="productCategory"]').selectOption({ label: 'Shirts' });
        await page.locator('input[name="productPrice"]').fill('25.50');
        await page.locator('input[name="productStock"]').fill('5');
        await page.setInputFiles('input[name="file"]', getFixturePath('banner3.jpg'));
        await Promise.all([
            page.waitForURL(/\/admin\/product-list/),
            page.getByRole('button', { name: 'Submit' }).click(),
        ]);

        const productRow = page.locator('table tbody tr', { hasText: productTitle });
        await expect(productRow).toBeVisible();
        await productRow.locator('a').first().click();

        await page.locator('input[name="productTitle"]').fill(updatedTitle);
        await page.locator('input[name="productStock"]').fill('10');
        await page.setInputFiles('input[name="file"]', getFixturePath('banner2.jpg'));
        await Promise.all([
            page.waitForURL(/\/admin\/product-list/),
            page.getByRole('button', { name: 'Update' }).click(),
        ]);
        const updatedRow = page.locator('table tbody tr', { hasText: updatedTitle });
        await expect(updatedRow).toBeVisible();
        page.once('dialog', (dialog) => dialog.accept());
        await Promise.all([
            page.waitForURL(/\/admin\/product-list/),
            updatedRow.locator('a').nth(1).click(),
        ]);
        await expect(page.locator('table tbody tr', { hasText: updatedTitle })).toHaveCount(0);
    });

    test('forgot password flow', async ({ page, request }) => {
        const user = buildTestUser();
        const newPassword = 'N3wP@ssword1234';
        await registerUser(page, user);

        await page.goto('/forgot-password');
        await page.locator('input[name="email"]').fill(user.email);
        await page.getByRole('button', { name: 'Send' }).click();
        await expect(page.locator('body')).toContainText('Password Reset Link has been sent');

        const resetUrl = await fetchLastResetUrl(request);
        await page.goto(resetUrl);
        await page.locator('input[name="password"]').fill(newPassword);
        await page.locator('input[name="confirmPassword"]').fill(newPassword);
        await page.getByRole('button', { name: 'Reset Password' }).click();
        await expect(page.locator('body')).toContainText('Password Changed Successfully');

        user.password = newPassword;
        await loginUser(page, user);
        await expect(page).not.toHaveURL(/signin\?error/);
    });

    test('adding same product twice increases quantity', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);
        const productDetailUrl = page.url();
        await addProductToCartFromDetail(page);

        await page.goto(productDetailUrl);
        await addProductToCartFromDetail(page);

        await page.goto('/user/cart');
        const quantity = await getCartQuantity(page);
        await expect(quantity).toBe(2);
    });

    test('out-of-stock product blocks add-to-cart', async ({ page }) => {
        await page.goto('/products');
        const detailLinks = page.getByRole('link', { name: 'Details' });
        const totalLinks = await detailLinks.count();

        let foundOutOfStock = false;
        for (let i = 0; i < Math.min(totalLinks, 5); i += 1) {
            await detailLinks.nth(i).click();
            const outOfStockButton = page.getByRole('link', { name: 'Out of Stock' });
            if (await outOfStockButton.isVisible()) {
                foundOutOfStock = true;
                await expect(page.getByRole('button', { name: 'Add To Cart' })).toHaveCount(0);
                break;
            }
            await page.goto('/products');
        }

        if (!foundOutOfStock) {
            test.skip(true, 'No out-of-stock products available to validate add-to-cart blocking.');
            return;
        }
    });

    test('quantity increase beyond stock shows error', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);
        const maxQuantity = await page.locator('input[name="quantity"]').getAttribute('max');
        if (!maxQuantity) {
            test.skip(true, 'Quantity max attribute missing for this product.');
            return;
        }
        await addProductToCartFromDetail(page, maxQuantity);

        await page.goto('/user/cart');
        await page.locator('table tbody tr').first().getByRole('link').nth(1).click();
        await expect(page.locator('body')).toContainText('Requested quantity is unavailable.');
    });

    test('removing item shows empty cart state', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openFirstProduct(page);
        await addProductToCartFromDetail(page);
        await page.goto('/user/cart');

        await page.locator('table tbody tr').first().getByRole('link').first().click();
        const cartRows = page.locator('table tbody tr');
        await expect(cartRows).toHaveCount(0);
    });

    test('cart contents persist after reload and totals stay consistent', async ({ page }) => {
        const user = buildTestUser();
        await registerUser(page, user);
        await loginUser(page, user);

        await openProductByName(page, 'White Shirt');
        await addProductToCartFromDetail(page, 2);

        await page.goto('/user/cart');

        const productRows = page.locator('table tbody tr', { hasText: 'White Shirt' });
        const productRowCount = await productRows.count();
        await expect(productRowCount).toBeGreaterThan(0);

        let totalQuantity = 0;
        let lineTotalSum = 0;
        let unitPrice = null;

        for (let i = 0; i < productRowCount; i += 1) {
            const row = productRows.nth(i);
            const quantityText = await row.locator('td').nth(3).innerText();
            const priceText = await row.locator('td').nth(2).innerText();
            const totalText = await row.locator('td').nth(4).innerText();

            const quantity = Number(quantityText.match(/\\d+/)?.[0] ?? 0);
            const price = parsePrice(priceText);
            const total = parsePrice(totalText);

            totalQuantity += quantity;
            lineTotalSum += total;
            if (unitPrice === null) {
                unitPrice = price;
            }
        }

        await expect(totalQuantity).toBe(2);
        await expect(lineTotalSum).toBeCloseTo((unitPrice ?? 0) * totalQuantity, 2);

        const totalRow = page.locator('table tbody tr', { hasText: 'Total Price' });
        const orderTotalText = await totalRow.locator('td').last().innerText();
        const orderTotal = parsePrice(orderTotalText);
        await expect(orderTotal).toBeCloseTo(lineTotalSum, 2);

        await page.reload();
        const reloadedRows = page.locator('table tbody tr', { hasText: 'White Shirt' });
        const reloadedCount = await reloadedRows.count();
        await expect(reloadedCount).toBeGreaterThan(0);

        let reloadedQuantity = 0;
        let reloadedTotal = 0;

        for (let i = 0; i < reloadedCount; i += 1) {
            const row = reloadedRows.nth(i);
            const quantityText = await row.locator('td').nth(3).innerText();
            const totalText = await row.locator('td').nth(4).innerText();
            reloadedQuantity += Number(quantityText.match(/\\d+/)?.[0] ?? 0);
            reloadedTotal += parsePrice(totalText);
        }

        await expect(reloadedQuantity).toBe(totalQuantity);
        await expect(reloadedTotal).toBeCloseTo(lineTotalSum, 2);
    });

    test('category filter updates product listing URL', async ({ page }) => {
        await page.goto('/products');
        const categoryLinks = page.locator('a.list-group-item-action');
        if (await categoryLinks.count() < 2) {
            test.skip(true, 'No categories available to test filter navigation.');
            return;
        }
        const categoryLink = categoryLinks.nth(1);
        const href = await categoryLink.getAttribute('href');
        await categoryLink.click();
        if (href) {
            await expect(page).toHaveURL((url) => url.href.endsWith(href));
        }
    });

    test('search box updates query parameter', async ({ page }) => {
        await page.goto('/products');
        await page.locator('input[name="search"]').fill('shirt');
        await page.getByRole('button', { name: 'Search Product' }).click();
        await expect(page).toHaveURL(/search=/);
    });

    test('product detail shows price and discount formatting', async ({ page }) => {
        await openFirstProduct(page);
        const priceBlock = page.locator('p.fs-5.fw-bold');
        await expect(priceBlock).toContainText('Price: $');
        const priceText = await priceBlock.innerText();
        await expect(priceText).toMatch(/\d/);

        const discountText = page.locator('span.text-success');
        if (await discountText.count()) {
            await expect(priceBlock.locator('span.text-decoration-line-through')).toBeVisible();
            await expect(discountText.first()).toContainText('% Off');
        }
    });
});