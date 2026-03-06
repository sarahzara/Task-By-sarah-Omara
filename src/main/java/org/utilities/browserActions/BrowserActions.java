package org.utilities.browserActions;



import org.config.ConfigReader;
import org.driver.DriverManager;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.utilities.logs.LogManager;
import org.utilities.screenshot.Screenshot;

import java.time.Duration;
import java.util.List;
import java.util.Set;


public final class BrowserActions {

    private static final ConfigReader config = ConfigReader.getInstance();

    private static final int PAGE_LOAD_TIMEOUT = 30;
    private static final int WINDOW_TIMEOUT    = 10;

    private BrowserActions() {
        throw new UnsupportedOperationException("BrowserActions is a utility class and cannot be instantiated");
    }

    // =================== DRIVER ACCESS ===================

    public static WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    public static String getBaseUrl() {
        return config.getBaseUrl();
    }

    // =================== NAVIGATION ===================

    public static void navigateTo(String url) {
        LogManager.info("Navigating to: {}", url);
        getDriver().get(url);
        LogManager.info("Navigation completed to: {}", url);
    }

    public static void navigateToBaseUrl() {
        navigateTo(getBaseUrl());
    }

    public static void navigateBack() {
        LogManager.debug("Navigating back to previous page");
        getDriver().navigate().back();
    }

    public static void navigateForward() {
        LogManager.debug("Navigating forward to next page");
        getDriver().navigate().forward();
    }

    public static void refreshPage() {
        LogManager.debug("Refreshing current page");
        getDriver().navigate().refresh();
    }

    // =================== PAGE INFO ===================

    public static String getCurrentUrl() {
        String url = getDriver().getCurrentUrl();
        LogManager.debug("Current URL: {}", url);
        return url;
    }

    public static String getPageTitle() {
        String title = getDriver().getTitle();
        LogManager.debug("Page title: {}", title);
        return title;
    }

    public static String getPageSource() {
        return getDriver().getPageSource();
    }

    // =================== WINDOW MANAGEMENT ===================

    public static void maximizeWindow() {
        LogManager.debug("Maximizing browser window");
        getDriver().manage().window().maximize();
    }

    public static void minimizeWindow() {
        LogManager.debug("Minimizing browser window");
        getDriver().manage().window().minimize();
    }

    public static void fullscreenWindow() {
        LogManager.debug("Setting window to fullscreen mode");
        getDriver().manage().window().fullscreen();
    }

    public static void setWindowSize(int width, int height) {
        LogManager.debug("Setting window size to {}x{}", width, height);
        getDriver().manage().window().setSize(new Dimension(width, height));
    }

    public static Dimension getWindowSize() {
        return getDriver().manage().window().getSize();
    }

    public static Point getWindowPosition() {
        return getDriver().manage().window().getPosition();
    }

    public static String getWindowHandle() {
        return getDriver().getWindowHandle();
    }

    public static List<String> getWindowHandles() {
        return getDriver().getWindowHandles().stream().toList();
    }

    public static void switchToWindow(String windowHandle) {
        LogManager.debug("Switching to window: {}", windowHandle);
        getDriver().switchTo().window(windowHandle);
    }

    public static void switchToFirstWindow() {
        LogManager.debug("Switching to first window");
        List<String> handles = getWindowHandles();
        if (!handles.isEmpty()) {
            getDriver().switchTo().window(handles.get(0));
        }
    }

    public static void switchToLastWindow() {
        LogManager.debug("Switching to last (newest) window");
        List<String> handles = getWindowHandles();
        if (!handles.isEmpty()) {
            getDriver().switchTo().window(handles.get(handles.size() - 1));
        }
    }

    /**
     * Switches to the parent frame in the DOM hierarchy.
     *
     * FIX 2: Previously this method was closing all other windows
     *         instead of switching to the parent frame. Corrected to use
     *         driver.switchTo().parentFrame() as the name implies.
     */
    public static void switchToParentFrame() {
        LogManager.debug("Switching to parent frame");
        getDriver().switchTo().parentFrame();
    }


    public static void closeChildWindowsAndReturnToMain(String mainWindowHandle) {
        LogManager.debug("Closing child windows, returning to: {}", mainWindowHandle);
        for (String handle : getWindowHandles()) {
            if (!handle.equals(mainWindowHandle)) {
                getDriver().switchTo().window(handle);
                getDriver().close();
            }
        }
        getDriver().switchTo().window(mainWindowHandle);
    }

    public static void closeWindow() {
        LogManager.debug("Closing current window");
        getDriver().close();
    }

    public static void closeAllWindowsExceptMain() {
        LogManager.debug("Closing all windows except main window");
        String mainWindow = getDriver().getWindowHandle();
        closeChildWindowsAndReturnToMain(mainWindow);
    }

    public static boolean waitForNewWindow(int currentHandleCount) {
        LogManager.debug("Waiting for new window to open");
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WINDOW_TIMEOUT));
        return wait.until(ExpectedConditions.numberOfWindowsToBe(currentHandleCount + 1));
    }

    // =================== ALERTS ===================

    public static Alert switchToAlert() {
        LogManager.debug("Switching to alert");
        return getDriver().switchTo().alert();
    }

    public static void acceptAlert() {
        LogManager.debug("Accepting alert");
        getDriver().switchTo().alert().accept();
    }

    public static void dismissAlert() {
        LogManager.debug("Dismissing alert");
        getDriver().switchTo().alert().dismiss();
    }

    public static String getAlertText() {
        String text = getDriver().switchTo().alert().getText();
        LogManager.debug("Alert text: {}", text);
        return text;
    }

    public static void sendKeysToAlert(String text) {
        LogManager.debug("Sending text to alert: {}", text);
        getDriver().switchTo().alert().sendKeys(text);
    }

    // =================== FRAMES ===================

    public static void switchToFrame(int index) {
        LogManager.debug("Switching to frame by index: {}", index);
        getDriver().switchTo().frame(index);
    }

    public static void switchToFrame(String nameOrId) {
        LogManager.debug("Switching to frame by name/ID: {}", nameOrId);
        getDriver().switchTo().frame(nameOrId);
    }

    public static void switchToFrame(WebElement frameElement) {
        LogManager.debug("Switching to frame by WebElement");
        getDriver().switchTo().frame(frameElement);
    }

    public static void switchToDefaultContent() {
        LogManager.debug("Switching to default content");
        getDriver().switchTo().defaultContent();
    }

    // =================== JAVASCRIPT ===================

    public static Object executeJavaScript(String script, Object... args) {
        LogManager.debug("Executing JavaScript: {}", script);
        return ((JavascriptExecutor) getDriver()).executeScript(script, args);
    }

    public static void scrollToTop() {
        LogManager.debug("Scrolling to top of page");
        executeJavaScript("window.scrollTo(0, 0);");
    }

    public static void scrollToBottom() {
        LogManager.debug("Scrolling to bottom of page");
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public static void scrollBy(int xPixels, int yPixels) {
        LogManager.debug("Scrolling by {} horizontal and {} vertical pixels", xPixels, yPixels);
        executeJavaScript("window.scrollBy(" + xPixels + "," + yPixels + ");");
    }

    // =================== PAGE LOAD ===================


    public static void waitForPageLoad() {
        String strategy = config.get("page.load.strategy", "eager").toLowerCase();
        int timeoutSec  = config.getPageLoadTimeout();

        LogManager.debug("Waiting for page load (strategy={}, timeout={}s)", strategy, timeoutSec);

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSec));

        if (strategy.equals("normal")) {
            // Normal: wait for every resource to finish loading
            wait.until(driver -> executeJavaScript("return document.readyState").equals("complete"));
            LogManager.debug("Page fully loaded (readyState=complete)");
        } else {
            // Eager / none: DOM is usable once interactive or complete
            wait.until(driver -> {
                String state = (String) executeJavaScript("return document.readyState");
                return "interactive".equals(state) || "complete".equals(state);
            });
            LogManager.debug("Page DOM ready (readyState=interactive or complete)");
        }
    }

    // =================== SCREENSHOTS ===================


    public static String takeScreenshot(String screenshotName) {
        LogManager.debug("Taking screenshot: {}", screenshotName);
        return Screenshot.captureScreenshot(screenshotName);
    }

    public static String windowScreenShot(){
        return Screenshot.captureScreenshot();
    }

    // =================== COOKIES ===================

    public static void deleteAllCookies() {
        LogManager.debug("Deleting all browser cookies");
        getDriver().manage().deleteAllCookies();
    }

    public static Cookie getCookie(String name) {
        return getDriver().manage().getCookieNamed(name);
    }

    public static Set<Cookie> getAllCookies() {
        return getDriver().manage().getCookies();
    }

    public static void addCookie(Cookie cookie) {
        LogManager.debug("Adding cookie: {}", cookie.getName());
        getDriver().manage().addCookie(cookie);
    }

    public static void deleteCookie(String name) {
        LogManager.debug("Deleting cookie: {}", name);
        getDriver().manage().deleteCookieNamed(name);
    }
}