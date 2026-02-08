// @ts-check
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
    testDir: './tests',
    timeout: 30_000,
    expect: {
        timeout: 10_000,
    },
    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure',
    },
});