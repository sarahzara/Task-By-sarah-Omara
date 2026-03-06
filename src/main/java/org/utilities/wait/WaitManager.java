package org.utilities.wait;

import org.config.ConfigReader;
import org.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.utilities.logs.LogManager;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public final class WaitManager {

    private static final int DEFAULT_POLLING_MS = 500;

    private static final String ELEMENT_FOUND_LOG   = "Element found: {}";
    private static final String ELEMENT_TIMEOUT_LOG = "Timeout waiting for element: {}";

    private static final ConfigReader config = ConfigReader.getInstance();

    private WaitManager() {
        throw new UnsupportedOperationException("WaitManager is a utility class and cannot be instantiated");
    }

    // =================== WAIT FACTORY METHODS ===================

    /**
     * Creates a standard WebDriverWait with the given timeout.
     *
     * @param timeoutInSeconds Maximum seconds to wait
     * @return Configured WebDriverWait instance
     */
    public static WebDriverWait createWebDriverWait(int timeoutInSeconds) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutInSeconds));
    }

    /**
     * Creates a FluentWait that ignores {@link NoSuchElementException} and
     * {@link StaleElementReferenceException} by default.
     *
     * @param timeoutInSeconds         Maximum seconds to wait
     * @param pollingIntervalInSeconds How often to poll (in seconds)
     * @return Configured FluentWait instance
     */
    public static Wait<WebDriver> createFluentWait(int timeoutInSeconds, int pollingIntervalInSeconds) {
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofSeconds(pollingIntervalInSeconds))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Creates a FluentWait with a custom set of exceptions to ignore.
     *
     * @param timeoutInSeconds         Maximum seconds to wait
     * @param pollingIntervalInSeconds How often to poll (in seconds)
     * @param exceptionsToIgnore       Exception classes to suppress while polling
     * @return Configured FluentWait instance
     */
    @SafeVarargs
    public static Wait<WebDriver> createFluentWait(int timeoutInSeconds, int pollingIntervalInSeconds,
                                                   Class<? extends Throwable>... exceptionsToIgnore) {
        FluentWait<WebDriver> wait = new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofSeconds(pollingIntervalInSeconds));

        for (Class<? extends Throwable> ex : exceptionsToIgnore) {
            wait = wait.ignoring(ex);
        }
        return wait;
    }

    // =================== VISIBILITY WAITS ===================

    /** Waits for an element to be visible using the configured default timeout. */
    public static WebElement waitForElementVisible(By locator) {
        return waitForElementVisible(locator, config.getExplicitWaitTimeout());
    }

    /**
     * Waits up to {@code timeoutInSeconds} for the element to be visible.
     *
     * @param locator          By locator of the element
     * @param timeoutInSeconds Maximum seconds to wait
     * @return The visible WebElement
     * @throws TimeoutException if element is not visible within the timeout
     */
    public static WebElement waitForElementVisible(By locator, int timeoutInSeconds) {
        LogManager.debug("Waiting for element to be visible: {}", locator);
        try {
            WebElement element = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            LogManager.debug(ELEMENT_FOUND_LOG, locator.toString());
            return element;
        } catch (TimeoutException e) {
            LogManager.error(ELEMENT_TIMEOUT_LOG, locator.toString());
            throw new TimeoutException(
                    "Element not visible within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    /**
     * Waits for an element to be visible using FluentWait with custom polling.
     *
     * @param locator                  By locator of the element
     * @param timeoutInSeconds         Maximum seconds to wait
     * @param pollingIntervalInSeconds Poll frequency in seconds
     * @return The visible WebElement
     */
    public static WebElement waitForElementVisibleFluent(By locator, int timeoutInSeconds,
                                                         int pollingIntervalInSeconds) {
        LogManager.debug("Waiting for element to be visible (FluentWait): {}", locator);
        try {
            WebElement element = createFluentWait(timeoutInSeconds, pollingIntervalInSeconds)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            LogManager.debug(ELEMENT_FOUND_LOG, locator.toString());
            return element;
        } catch (TimeoutException e) {
            LogManager.error(ELEMENT_TIMEOUT_LOG, locator.toString());
            throw new TimeoutException(
                    "Element not visible within " + timeoutInSeconds + " seconds (FluentWait): " + locator, e);
        }
    }

    // =================== CLICKABILITY WAITS ===================

    /** Waits for an element to be clickable using the configured default timeout. */
    public static WebElement waitForElementClickable(By locator) {
        return waitForElementClickable(locator, config.getExplicitWaitTimeout());
    }

    /**
     * Waits up to {@code timeoutInSeconds} for the element to be clickable.
     *
     * @param locator          By locator of the element
     * @param timeoutInSeconds Maximum seconds to wait
     * @return The clickable WebElement
     */
    public static WebElement waitForElementClickable(By locator, int timeoutInSeconds) {
        LogManager.debug("Waiting for element to be clickable: {}", locator);
        try {
            WebElement element = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            LogManager.debug(ELEMENT_FOUND_LOG, locator.toString());
            return element;
        } catch (TimeoutException e) {
            LogManager.error(ELEMENT_TIMEOUT_LOG, locator.toString());
            throw new TimeoutException(
                    "Element not clickable within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    /**
     * Waits for an already-located WebElement to become clickable.
     *
     * @param element          WebElement to wait for
     * @param timeoutInSeconds Maximum seconds to wait
     * @return The clickable WebElement
     */
    public static WebElement waitForElementClickable(WebElement element, int timeoutInSeconds) {
        LogManager.debug("Waiting for WebElement to be clickable");
        try {
            WebElement clickable = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.elementToBeClickable(element));
            LogManager.debug("WebElement is now clickable");
            return clickable;
        } catch (TimeoutException e) {
            LogManager.error("WebElement not clickable within {} seconds", timeoutInSeconds);
            throw new TimeoutException("WebElement not clickable within " + timeoutInSeconds + " seconds", e);
        }
    }

    // =================== PRESENCE WAITS ===================

    /** Waits for an element to be present in the DOM using the configured default timeout. */
    public static WebElement waitForElementPresent(By locator) {
        return waitForElementPresent(locator, config.getExplicitWaitTimeout());
    }

    /**
     * Waits up to {@code timeoutInSeconds} for the element to be present in the DOM.
     *
     * @param locator          By locator of the element
     * @param timeoutInSeconds Maximum seconds to wait
     * @return The present WebElement
     */
    public static WebElement waitForElementPresent(By locator, int timeoutInSeconds) {
        LogManager.debug("Waiting for element to be present: {}", locator);
        try {
            WebElement element = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            LogManager.debug(ELEMENT_FOUND_LOG, locator.toString());
            return element;
        } catch (TimeoutException e) {
            LogManager.error(ELEMENT_TIMEOUT_LOG, locator.toString());
            throw new TimeoutException(
                    "Element not present within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    /**
     * Waits for all elements matching the locator to be present in the DOM.
     *
     * @param locator          Shared By locator for the elements
     * @param timeoutInSeconds Maximum seconds to wait
     * @return List of all matching present WebElements
     */
    public static List<WebElement> waitForElementsPresent(By locator, int timeoutInSeconds) {
        LogManager.debug("Waiting for elements to be present: {}", locator);
        try {
            List<WebElement> elements = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            LogManager.debug("Found {} elements matching locator: {}", elements.size(), locator);
            return elements;
        } catch (TimeoutException e) {
            LogManager.error(ELEMENT_TIMEOUT_LOG, locator.toString());
            throw new TimeoutException(
                    "Elements not present within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    // =================== INVISIBILITY WAITS ===================

    /**
     * Waits for an element to become invisible or leave the DOM.
     *
     * @param locator          By locator of the element
     * @param timeoutInSeconds Maximum seconds to wait
     * @return true when element is invisible
     */
    public static boolean waitForElementInvisible(By locator, int timeoutInSeconds) {
        LogManager.debug("Waiting for element to become invisible: {}", locator);
        try {
            boolean result = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            LogManager.debug("Element became invisible: {}", locator);
            return result;
        } catch (TimeoutException e) {
            LogManager.error("Element did not become invisible within {} seconds: {}", timeoutInSeconds, locator);
            throw new TimeoutException(
                    "Element did not become invisible within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    // =================== TEXT / ATTRIBUTE WAITS ===================

    /**
     * Waits until the element's text contains the specified string.
     *
     * @param locator          By locator of the element
     * @param text             Expected text fragment
     * @param timeoutInSeconds Maximum seconds to wait
     * @return true when text is present
     */
    public static boolean waitForTextToBePresent(By locator, String text, int timeoutInSeconds) {
        LogManager.debug("Waiting for text '{}' to be present in element: {}", text, locator);
        try {
            boolean result = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            LogManager.debug("Text '{}' found in element: {}", text, locator);
            return result;
        } catch (TimeoutException e) {
            LogManager.error("Text '{}' not found in element {} within {} seconds", text, locator, timeoutInSeconds);
            throw new TimeoutException(
                    "Text '" + text + "' not found in element within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    /**
     * Waits until the element's given attribute equals the expected value.
     *
     * @param locator          By locator of the element
     * @param attribute        HTML attribute name
     * @param value            Expected attribute value
     * @param timeoutInSeconds Maximum seconds to wait
     * @return true when the attribute has the expected value
     */
    public static boolean waitForAttributeValue(By locator, String attribute, String value, int timeoutInSeconds) {
        LogManager.debug("Waiting for attribute '{}' to have value '{}' in element: {}", attribute, value, locator);
        try {
            boolean result = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.attributeToBe(locator, attribute, value));
            LogManager.debug("Attribute '{}' has expected value '{}' in element: {}", attribute, value, locator);
            return result;
        } catch (TimeoutException e) {
            LogManager.error("Attribute '{}' does not have expected value '{}' in element {} within {} seconds",
                    attribute, value, locator, timeoutInSeconds);
            throw new TimeoutException("Attribute '" + attribute + "' does not have expected value '" + value
                    + "' within " + timeoutInSeconds + " seconds: " + locator, e);
        }
    }

    // =================== URL / TITLE WAITS ===================

    /**
     * Waits until the current URL contains the given fragment.
     *
     * @param urlFragment      Text the URL must contain
     * @param timeoutInSeconds Maximum seconds to wait
     * @return true when the URL contains the fragment
     */
    public static boolean waitForUrlContains(String urlFragment, int timeoutInSeconds) {
        LogManager.debug("Waiting for URL to contain: {}", urlFragment);
        try {
            boolean result = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.urlContains(urlFragment));
            LogManager.debug("URL now contains: {}", urlFragment);
            return result;
        } catch (TimeoutException e) {
            LogManager.error("URL does not contain '{}' within {} seconds", urlFragment, timeoutInSeconds);
            throw new TimeoutException(
                    "URL does not contain '" + urlFragment + "' within " + timeoutInSeconds + " seconds", e);
        }
    }

    /**
     * Waits until the page title contains the given fragment.
     *
     * @param titleFragment    Text the title must contain
     * @param timeoutInSeconds Maximum seconds to wait
     * @return true when the title contains the fragment
     */
    public static boolean waitForTitleContains(String titleFragment, int timeoutInSeconds) {
        LogManager.debug("Waiting for title to contain: {}", titleFragment);
        try {
            boolean result = createWebDriverWait(timeoutInSeconds)
                    .until(ExpectedConditions.titleContains(titleFragment));
            LogManager.debug("Title now contains: {}", titleFragment);
            return result;
        } catch (TimeoutException e) {
            LogManager.error("Title does not contain '{}' within {} seconds", titleFragment, timeoutInSeconds);
            throw new TimeoutException(
                    "Title does not contain '" + titleFragment + "' within " + timeoutInSeconds + " seconds", e);
        }
    }

    // =================== CUSTOM CONDITION WAITS ===================

    /**
     * Waits for a custom condition expressed as a Java {@link Function}.
     *
     * @param condition                Condition to evaluate
     * @param timeoutInSeconds         Maximum seconds to wait
     * @param pollingIntervalInSeconds Poll frequency in seconds
     * @param <T>                      Return type of the condition
     * @return Result of the condition
     */
    public static <T> T waitForCondition(Function<WebDriver, T> condition,
                                         int timeoutInSeconds, int pollingIntervalInSeconds) {
        LogManager.debug("Waiting for custom condition — timeout: {}s, polling: {}s",
                timeoutInSeconds, pollingIntervalInSeconds);
        try {
            T result = createFluentWait(timeoutInSeconds, pollingIntervalInSeconds).until(condition);
            LogManager.debug("Custom condition satisfied");
            return result;
        } catch (TimeoutException e) {
            LogManager.error("Custom condition not satisfied within {} seconds", timeoutInSeconds);
            throw new TimeoutException("Custom condition not satisfied within " + timeoutInSeconds + " seconds", e);
        }
    }

    /**
     * Waits for a standard Selenium {@link ExpectedCondition}.
     *
     * @param condition        The condition to evaluate
     * @param timeoutInSeconds Maximum seconds to wait
     * @param <T>              Return type of the condition
     * @return Result of the condition
     */
    public static <T> T waitForExpectedCondition(ExpectedCondition<T> condition, int timeoutInSeconds) {
        LogManager.debug("Waiting for ExpectedCondition — timeout: {}s", timeoutInSeconds);
        try {
            T result = createWebDriverWait(timeoutInSeconds).until(condition);
            LogManager.debug("ExpectedCondition satisfied");
            return result;
        } catch (TimeoutException e) {
            LogManager.error("ExpectedCondition not satisfied within {} seconds", timeoutInSeconds);
            throw new TimeoutException("ExpectedCondition not satisfied within " + timeoutInSeconds + " seconds", e);
        }
    }

    // =================== UTILITY ===================

    /**
     * Pauses execution for the given number of milliseconds.
     * Use sparingly — prefer explicit waits wherever possible.
     *
     * @param milliseconds Duration to sleep
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogManager.error("Sleep interrupted: {}", e.getMessage());
        }
    }

    /** @return Default explicit wait timeout from configuration (seconds). */
    public static int getDefaultTimeout() {
        return config.getExplicitWaitTimeout();
    }

    /** @return Default polling interval in milliseconds. */
    public static int getDefaultPollingIntervalMs() {
        return DEFAULT_POLLING_MS;
    }
}
