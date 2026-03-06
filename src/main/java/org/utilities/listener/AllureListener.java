package org.utilities.listener;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.config.ConfigReader;
import org.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.utilities.logs.LogManager;


public class AllureListener implements ITestListener {

    private static final ConfigReader config = ConfigReader.getInstance();

    // =================== SUITE LIFECYCLE ===================

    @Override
    public void onStart(ITestContext context) {
        LogManager.info("=== TEST SUITE STARTED: {} ===", context.getName());

    }

    @Override
    public void onFinish(ITestContext context) {
        LogManager.info("=== TEST SUITE COMPLETED: {} ===", context.getName());
        LogManager.info("Allure results saved to: target/allure-results");
        LogManager.info("To view report, run: mvn allure:serve");
    }

    // =================== TEST LIFECYCLE ===================

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        LogManager.info("▶️  STARTING TEST: {}", testName);

        // Attach browser and environment as Allure parameters (visible in report)
        Allure.parameter("Browser",     config.getBrowser());
        Allure.parameter("Environment", config.getCurrentEnvironment());
        Allure.parameter("Base URL",    config.getBaseUrl());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        LogManager.info("✅ TEST PASSED: {}", testName);

        if (config.getPropertyAsBoolean("screenshot.on.success", false)) {
            captureScreenshot("SUCCESS_" + testName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName  = result.getMethod().getMethodName();
        Throwable cause  = result.getThrowable();

        LogManager.error("❌ TEST FAILED: {}", testName);

        if (cause != null) {
            LogManager.error("Failure reason: {}", cause.getMessage());
            // Attach the failure cause as readable text in the Allure report
            attachText("Failure Cause", cause.toString());
        }

        // Always capture screenshot and page source on failure
        captureScreenshot("FAILURE_" + testName);
        capturePageSource();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        LogManager.info("⚠️  TEST SKIPPED: {}", testName);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        LogManager.info("⚠️  TEST FAILED BUT WITHIN SUCCESS PERCENTAGE: {}", testName);
    }

    // =================== ALLURE ATTACHMENT METHODS ===================

    /**
     * Captures a screenshot and attaches it to the current Allure test.
     *
     * @param name Descriptive name shown in the Allure report attachment
     * @return Screenshot bytes, or empty array if capture failed
     */
    @Attachment(value = "Screenshot: {name}", type = "image/png")
    public byte[] captureScreenshot(String name) {
        try {
            if (DriverManager.hasDriver()) {
                WebDriver driver = DriverManager.getDriver();
                if (driver instanceof TakesScreenshot ts) {
                    byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
                    LogManager.info("📸 Screenshot captured for Allure: {}", name);
                    return screenshot;
                }
            }
            LogManager.debug("Could not capture screenshot — driver not available");
        } catch (Exception e) {
            LogManager.error("Failed to capture screenshot: {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * Captures the current page HTML source and attaches it to the Allure test.
     *
     * @return Page HTML source, or fallback message if capture failed
     */
    @Attachment(value = "Page Source", type = "text/html")
    public String capturePageSource() {
        try {
            if (DriverManager.hasDriver()) {
                String pageSource = DriverManager.getDriver().getPageSource();
                LogManager.info("📄 Page source captured for Allure");
                return pageSource;
            }
            LogManager.debug("Could not capture page source — driver not available");
        } catch (Exception e) {
            LogManager.error("Failed to capture page source: {}", e.getMessage());
        }
        return "No page source available";
    }

    /**
     * Attaches a plain-text string to the current Allure test.
     *
     * @param name    Attachment label shown in the Allure report
     * @param content The text content to attach
     * @return The same content (required by @Attachment contract)
     */
    @Attachment(value = "{name}", type = "text/plain")
    public String attachText(String name, String content) {
        return content;
    }
}