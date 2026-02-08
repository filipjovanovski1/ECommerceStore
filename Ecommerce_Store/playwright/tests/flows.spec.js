const { test, expect } = require('@playwright/test');

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

async function openFirstProduct(page) {
    await page.goto('/products');
    const detailLinks = page.getByRole('link', { name: 'Details' });
    await expect(detailLinks.first(), 'Expected at least one product to be listed. Seed the DB with products for this test.')
        .toBeVisible();
    await detailLinks.first().click();
}

async function getCartQuantity(page) {
    const quantityCell = page.locator('table tbody tr').first().locator('td').nth(3);
    const rawText = await quantityCell.innerText();
    const match = rawText.match(/(\d+)/);
    return match ? Number(match[1]) : null;
}

test.describe('E2E flows', () => {
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
        await page.getByRole('button', { name: 'Add To Cart' }).click();

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
        await page.getByRole('button', { name: 'Add To Cart' }).click();
        await page.goto('/user/cart');

        const initialQuantity = await getCartQuantity(page);
        await page.locator('table tbody tr').first().getByRole('link').nth(1).click();
        const increasedQuantity = await getCartQuantity(page);
        await expect(increasedQuantity).toBe(initialQuantity + 1);

        await page.locator('table tbody tr').first().getByRole('link').first().click();
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
});