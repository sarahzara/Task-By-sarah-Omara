
package org.Base;

import org.config.ConfigReader;
import org.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.utilities.logs.LogManager;
import org.utilities.screenshot.Screenshot;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;


public class BaseManager {

    // =================== CONSTANTS ===================

    private static final ConfigReader config = ConfigReader.getInstance();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final String ALLURE_RESULTS_DIR = resolveResultsDir();
    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    private static String resolveResultsDir() {
        String dir = System.getProperty("allure.results.directory");
        if (dir == null || dir.trim().isEmpty()) {
            dir = "target/allure-results";
            System.setProperty("allure.results.directory", dir);
        }
        return dir;
    }

    // =================== INSTANCE VARIABLES ===================

    private LocalDateTime testStartTime;
    private static LocalDateTime suiteStartTime;

    // =================== DRIVER LIFECYCLE ===================

    /** Initialises a WebDriver for the current test thread. */
    public void initializeDriver() {
        String browser   = config.getBrowser();
        boolean headless = config.isHeadless();
        LogManager.debug("Initializing {} driver (headless: {})", browser, headless);
        DriverManager.initializeDriver();
        LogManager.debug("Driver initialized successfully");
    }

    /** Quits WebDriver and removes it from ThreadLocal after each test. */
    public void cleanupDriver() {
        try {
            if (DriverManager.hasDriver()) {
                LogManager.debug("Cleaning up WebDriver for thread: {}", Thread.currentThread().getName());
                DriverManager.quitDriver();
            }
        } catch (Exception e) {
            LogManager.error("Error cleaning up driver: {}", e.getMessage());
        }
    }

    /** Returns the WebDriver instance for the current thread. */
    public WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    // =================== SUITE LIFECYCLE ===================


    public void suiteSetup() {
        suiteStartTime = LocalDateTime.now();

        // Clean results ONCE here — never inside AllureListener.onStart()
        cleanAllureResults();

        LogManager.info("=================================================================");
        LogManager.info("                    TEST SUITE STARTING                         ");
        LogManager.info("=================================================================");
        LogManager.info("Suite Start Time : {}", suiteStartTime.format(TIME_FORMATTER));
        LogManager.info("Environment      : {}", config.getCurrentEnvironment());
        LogManager.info("Browser          : {}", config.getBrowser());
        LogManager.info("Headless Mode    : {}", config.isHeadless());
        LogManager.info("Base URL         : {}", config.getBaseUrl());
        LogManager.info("=================================================================");

        cleanupOldScreenshots();
    }


    public void suiteTeardown() {
        LocalDateTime suiteEndTime  = LocalDateTime.now();
        Duration      suiteDuration = Duration.between(suiteStartTime, suiteEndTime);

        LogManager.info("=================================================================");
        LogManager.info("                    TEST SUITE COMPLETED                        ");
        LogManager.info("=================================================================");
        LogManager.info("Suite End Time   : {}", suiteEndTime.format(TIME_FORMATTER));
        LogManager.info("Total Duration   : {} min {} sec",
                suiteDuration.toMinutes(), suiteDuration.getSeconds() % 60);
        LogManager.info("=================================================================");

        // Quit all drivers before generating the report
        DriverManager.quitAllDrivers();

        // Auto-generate and open the Allure report
        serveAllureReport();
    }

    // =================== TEST LIFECYCLE ===================

    /** Called before each @Test method. */
    public void testSetup(Method method) {
        testStartTime = LocalDateTime.now();
        String testName = method.getName();

        LogManager.info("┌──────────────────────────────────────────────────────────────────");
        LogManager.info("│ STARTING TEST : {}", testName);
        LogManager.info("│ Start Time    : {}", testStartTime.format(TIME_FORMATTER));
        LogManager.info("│ Thread        : {}", Thread.currentThread().getName());
        LogManager.info("└──────────────────────────────────────────────────────────────────");

        try {
            initializeDriver();
        } catch (Exception e) {
            LogManager.error("Test setup failed for {}: {}", testName, e.getMessage());
            throw e;
        }
    }

    /** Called after each @Test method. */
    public void testTeardown(org.testng.ITestResult result) {
        LocalDateTime testEndTime  = LocalDateTime.now();
        Duration      testDuration = Duration.between(testStartTime, testEndTime);
        String        testName     = result.getMethod().getMethodName();

        try {
            handleTestResult(result, testName, testDuration);
        } catch (Exception e) {
            LogManager.error("Error in test teardown: {}", e.getMessage());
        } finally {
            cleanupDriver();

            LogManager.info("┌──────────────────────────────────────────────────────────────────");
            LogManager.info("│ COMPLETED TEST : {}", testName);
            LogManager.info("│ End Time       : {}", testEndTime.format(TIME_FORMATTER));
            LogManager.info("│ Duration       : {}.{} s",
                    testDuration.getSeconds(), testDuration.getNano() / 1_000_000);
            LogManager.info("│ Status         : {}", getResultStatus(result));
            LogManager.info("└──────────────────────────────────────────────────────────────────");
        }
    }

    // =================== RESULT HANDLING ===================

    private void handleTestResult(org.testng.ITestResult result, String testName, Duration duration) {
        switch (result.getStatus()) {
            case org.testng.ITestResult.SUCCESS -> handleTestSuccess(testName, duration);
            case org.testng.ITestResult.FAILURE -> handleTestFailure(result, testName, duration);
            case org.testng.ITestResult.SKIP    -> LogManager.info("⚠️  TEST SKIPPED: {}", testName);
            default -> { /* no-op */ }
        }
    }

    private void handleTestSuccess(String testName, Duration duration) {
        LogManager.info("✅ TEST PASSED: {} ({}.{}s)",
                testName, duration.getSeconds(), duration.getNano() / 1_000_000);
        if (config.getPropertyAsBoolean("screenshot.on.success", false)) {
            Screenshot.captureScreenshot("success_" + testName);
        }
    }

    private void handleTestFailure(org.testng.ITestResult result, String testName, Duration duration) {
        Throwable exception = result.getThrowable();
        LogManager.error("❌ TEST FAILED: {} ({}.{}s)",
                testName, duration.getSeconds(), duration.getNano() / 1_000_000);
        if (exception != null) {
            LogManager.error("Failure Reason : {}", exception.getMessage());
            LogManager.error("Stack Trace    :", exception);
        }
        if (config.getPropertyAsBoolean("screenshot.on.failure", true)) {
            Screenshot.captureFailureScreenshot(testName);
        }
    }

    private String getResultStatus(org.testng.ITestResult result) {
        return switch (result.getStatus()) {
            case org.testng.ITestResult.SUCCESS -> "PASSED ✅";
            case org.testng.ITestResult.FAILURE -> "FAILED ❌";
            case org.testng.ITestResult.SKIP    -> "SKIPPED ⚠️";
            default -> "UNKNOWN";
        };
    }

    // =================== ALLURE REPORT ===================

    /**
     * Generates the Allure HTML report and opens it in the default browser
     * by running `mvn allure:serve` via ProcessBuilder.
     *
     * Why ProcessBuilder + mvn instead of allure CLI?
     *   - allure-maven plugin is already declared in pom.xml — no extra install.
     *   - Maven downloads the correct Allure version automatically.
     *   - Works on every machine that has Maven (CI, local, team members).
     *   - `allure:serve` starts a lightweight local web server and opens browser.
     *
     * This method is non-blocking — the server stays alive in a background
     * process so the JVM can exit normally after the suite finishes.
     */
    private void serveAllureReport() {
        // Verify there are actual result files to report on
        File resultsDir  = new File(ALLURE_RESULTS_DIR);
        File[] results   = resultsDir.listFiles(f -> f.getName().endsWith("-result.json"));

        if (results == null || results.length == 0) {
            LogManager.error("❌ No Allure result files found in: {}", resultsDir.getAbsolutePath());
            LogManager.error("   This usually means AspectJ weaver is not attached.");
            LogManager.error("   Verify pom.xml argLine contains: -javaagent:...aspectjweaver-{version}.jar");
            LogManager.info("   Run manually after fixing: mvn allure:serve");
            return;
        }

        LogManager.info("📊 Found {} test result file(s) — generating Allure report...", results.length);

        try {
            String projectRoot = System.getProperty("user.dir");

            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(projectRoot));
            pb.redirectErrorStream(true);

            if (IS_WINDOWS) {
                pb.command("cmd", "/c", "mvn", "allure:serve");
            } else {
                pb.command("sh", "-c", "mvn allure:serve");
            }

            // Start in background — keeps the server alive after JVM exits
            pb.start();

            // Give the server time to start before JVM shuts down
            Thread.sleep(4000);

            LogManager.info("✅ Allure report server started — opening at http://localhost:8080");
            LogManager.info("   Press Ctrl+C in the terminal to stop the report server.");

        } catch (Exception e) {
            LogManager.error("❌ Could not start Allure report server: {}", e.getMessage());
            LogManager.info("   Run manually: mvn allure:serve");
        }
    }

    // =================== ALLURE RESULTS CLEANUP ===================


    public void cleanAllureResults() {
        try {
            Path resultsPath = Paths.get(ALLURE_RESULTS_DIR);
            if (Files.exists(resultsPath)) {
                Files.walk(resultsPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try { Files.delete(file); } catch (Exception ignored) { }
                        });
                LogManager.info("🧹 Cleaned Allure results directory: {}", ALLURE_RESULTS_DIR);
            } else {
                Files.createDirectories(resultsPath);
                LogManager.debug("Created Allure results directory: {}", ALLURE_RESULTS_DIR);
            }
        } catch (Exception e) {
            LogManager.error("Failed to clean Allure results: {}", e.getMessage());
        }
    }

    // =================== NAVIGATION ===================

    public void navigateTo(String url) {
        LogManager.info("Navigating to URL: {}", url);
        getDriver().get(url);
    }

    public void navigateToBaseUrl()  { navigateTo(config.getBaseUrl()); }
    public void refreshPage()        { LogManager.debug("Refreshing page"); getDriver().navigate().refresh(); }
    public String getPageTitle()     { return getDriver().getTitle(); }
    public String getCurrentUrl()    { return getDriver().getCurrentUrl(); }
    public String getBaseUrl()       { return config.getBaseUrl(); }

    // =================== UTILITY ===================

    private void cleanupOldScreenshots() {
        if (config.getPropertyAsBoolean("screenshot.cleanup.enabled", false)) {
            LogManager.info("Cleaning up old screenshots...");
            Screenshot.clearScreenshotsDirectory();
        }
    }

    public void waitForSeconds(int seconds) {
        try { TimeUnit.SECONDS.sleep(seconds); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void waitForMilliseconds(int ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}