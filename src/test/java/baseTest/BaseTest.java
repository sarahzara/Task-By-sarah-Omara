
package baseTest;
import org.Base.BaseManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.utilities.logs.LogManager;

import java.lang.reflect.Method;

public abstract class BaseTest {


    private final BaseManager baseManager = new BaseManager();

    // =================== SUITE LIFECYCLE ===================

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        LogManager.info("Starting test suite setup...");
        baseManager.suiteSetup();
        customSuiteSetup();
        LogManager.info("Test suite setup completed");
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        LogManager.info("Starting test suite teardown...");
        customSuiteTeardown();
        baseManager.suiteTeardown();
        LogManager.info("Test suite teardown completed");
    }

    // =================== TEST LIFECYCLE ===================

    @BeforeMethod(alwaysRun = true)
    public void testSetup(Method method) {
        baseManager.testSetup(method);
        customTestSetup(method);
        navigateToBaseUrl();
    }

    @AfterMethod(alwaysRun = true)
    public void testTeardown(ITestResult result) {
        customTestTeardown(result);
        baseManager.testTeardown(result);
    }

    // =================== DRIVER ACCESS ===================

    protected WebDriver getDriver() {
        return baseManager.getDriver();
    }

    protected void initializeDriver() {
        baseManager.initializeDriver();
    }

    protected void cleanupDriver() {
        baseManager.cleanupDriver();
    }

    // =================== NAVIGATION ===================

    protected void navigateTo(String url) {
        baseManager.navigateTo(url);
    }

    protected void navigateToBaseUrl() {
        baseManager.navigateToBaseUrl();
    }

    protected void refreshPage() {
        baseManager.refreshPage();
    }

    // =================== PAGE INFO ===================

    protected String getPageTitle() {
        return baseManager.getPageTitle();
    }

    protected String getCurrentUrl() {
        return baseManager.getCurrentUrl();
    }

    protected String getBaseUrl() {
        return baseManager.getBaseUrl();
    }

    // =================== WAIT HELPERS ===================

    protected void waitForSeconds(int seconds) {
        baseManager.waitForSeconds(seconds);
    }

    protected void waitForMilliseconds(int milliseconds) {
        baseManager.waitForMilliseconds(milliseconds);
    }


    // =================== RESULT HELPERS ===================

    protected boolean isTestPassed(ITestResult result) {
        return result.getStatus() == ITestResult.SUCCESS;
    }

    protected boolean isTestFailed(ITestResult result) {
        return result.getStatus() == ITestResult.FAILURE;
    }

    protected boolean isTestSkipped(ITestResult result) {
        return result.getStatus() == ITestResult.SKIP;
    }


    // =================== CUSTOM HOOKS (override in child classes) ===================

    /** Called after base suite setup. Override for suite-level custom setup. */
    protected void customSuiteSetup() { }

    /** Called before base suite teardown. Override for suite-level custom teardown. */
    protected void customSuiteTeardown() { }

    /** Called after driver initialization. Override for test-level custom setup. */
    protected void customTestSetup(Method method) { }

    /** Called before driver cleanup. Override for test-level custom teardown. */
    protected void customTestTeardown(ITestResult result) { }
}