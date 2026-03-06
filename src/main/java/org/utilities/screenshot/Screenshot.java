package org.utilities.screenshot;

import org.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.utilities.logs.LogManager;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;


public final class Screenshot {


    // Constants
    private static final String SCREENSHOTS_DIR = System.getProperty("screenshots.dir", "target/screenshots");
    private static final String FAILURE_SCREENSHOT_PREFIX = "FAILED_";
    private static final String RETRY_SCREENSHOT_PREFIX = "RETRY_";
    private static final String DEFAULT_SCREENSHOT_NAME = "screenshot";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd_HH-mm-ss-SSS";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);

    private Screenshot() {
        throw new UnsupportedOperationException("Screenshot is a utility class and cannot be instantiated");
    }

    /**
     * Capture screenshot with custom name
     *
     * @param screenshotName The base name for the screenshot file
     * @return The absolute path to the saved screenshot, or null if capture failed
     */
    public static String captureScreenshot(String screenshotName) {
        try {
            if (!DriverManager.hasDriver()) {
                LogManager.error("No active driver available for screenshot capture");
                return null;
            }

            WebDriver driver = DriverManager.getDriver();

            if (!(driver instanceof TakesScreenshot)) {
                LogManager.error("Driver does not support screenshot capture");
                return null;
            }

            // Capture screenshot
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Create directory
            Path screenshotsDir = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(screenshotsDir)) {
                Files.createDirectories(screenshotsDir);
                LogManager.debug("Created screenshots directory: {}", screenshotsDir.toAbsolutePath());
            }

            // Generate filename with timestamp
            String timestamp = dateFormat.format(new Date());
            String filename = String.format("%s_%s.png", screenshotName, timestamp);

            // Save file
            Path screenshotPath = screenshotsDir.resolve(filename);
            Files.write(screenshotPath, screenshot);

            String path = screenshotPath.toAbsolutePath().toString();
            LogManager.info("📸 Screenshot saved: {}", path);

            return path;

        } catch (Exception e) {
            LogManager.logScreenshotFailure(e);
            return null;
        }
    }

    /**
     * Capture screenshot with default name
     *
     * @return The absolute path to the saved screenshot, or null if capture failed
     */
    public static String captureScreenshot() {
        return captureScreenshot(DEFAULT_SCREENSHOT_NAME);
    }

    /**
     * Capture screenshot for failed test
     *
     * @param testName The name of the failed test
     * @return The absolute path to the saved screenshot, or null if capture failed
     */
    public static String captureFailureScreenshot(String testName) {
        String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");
        String screenshotName = FAILURE_SCREENSHOT_PREFIX + sanitizedTestName;
        return captureScreenshot(screenshotName);
    }

    /**
     * Capture screenshot for retry attempt
     *
     * @param testName The name of the test being retried
     * @param retryAttempt The retry attempt number
     * @return The absolute path to the saved screenshot, or null if capture failed
     */
    public static String captureRetryScreenshot(String testName, int retryAttempt) {
        String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");
        String screenshotName = String.format("%s%s_attempt_%d",
                RETRY_SCREENSHOT_PREFIX,
                sanitizedTestName,
                retryAttempt
        );
        return captureScreenshot(screenshotName);
    }

    // =================== ALLURE METHODS ===================

    /**
     * Capture screenshot for Allure report
     * Returns screenshot as byte array for direct attachment to Allure
     *
     * @return Screenshot as byte array, or empty array if capture failed
     */
    public static byte[] captureForAllure() {
        try {
            if (!DriverManager.hasDriver()) {
                LogManager.debug("No active driver available for Allure screenshot");
                return new byte[0];
            }

            WebDriver driver = DriverManager.getDriver();

            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                LogManager.debug("Screenshot captured for Allure report");
                return screenshot;
            } else {
                LogManager.debug("Driver does not support screenshot capture for Allure");
            }

        } catch (Exception e) {
            LogManager.error("Failed to capture Allure screenshot: {}", e.getMessage());
        }

        return new byte[0];
    }

    /**
     * Capture failure screenshot for Allure
     * Saves screenshot to file AND returns bytes for Allure attachment
     *
     * @param testName The name of the failed test
     * @return Screenshot as byte array for Allure, or empty array if capture failed
     */
    public static byte[] captureFailureForAllure(String testName) {
        // Capture and save to file first
        captureFailureScreenshot(testName);
        // Return bytes for Allure attachment
        return captureForAllure();
    }

    /**
     * Get the screenshots directory path
     *
     * @return The absolute path to the screenshots directory
     */
    public static String getScreenshotsDirectory() {
        return Paths.get(SCREENSHOTS_DIR).toAbsolutePath().toString();
    }

    /**
     * Clear all screenshots from the screenshots directory
     * Use with caution - this will delete all files in the directory
     *
     * @return true if directory was cleared successfully, false otherwise
     */
    public static boolean clearScreenshotsDirectory() {
        try {
            Path screenshotsDir = Paths.get(SCREENSHOTS_DIR);

            if (!Files.exists(screenshotsDir)) {
                LogManager.debug("Screenshots directory does not exist: {}", screenshotsDir.toAbsolutePath());
                return true;
            }

            Files.walk(screenshotsDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                            LogManager.debug("Deleted screenshot: {}", file.getFileName());
                        } catch (Exception e) {
                            LogManager.error("Failed to delete screenshot {}: {}", file.getFileName(), e.getMessage());
                        }
                    });

            LogManager.info("Screenshots directory cleared: {}", screenshotsDir.toAbsolutePath());
            return true;

        } catch (Exception e) {
            LogManager.error("Failed to clear screenshots directory: {}", e.getMessage());
            return false;
        }
    }
}

