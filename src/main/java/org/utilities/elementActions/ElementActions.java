package org.utilities.elementActions;

import io.qameta.allure.Step;
import org.driver.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.utilities.logs.LogManager;
import org.utilities.screenshot.Screenshot;
import org.utilities.wait.WaitManager;

import java.util.List;


public final class ElementActions {


    private ElementActions() {
        throw new UnsupportedOperationException("ElementActions is a utility class and cannot be instantiated");
    }

    // =================== CLICK ACTIONS ===================

    @Step("Click on element: {description}")
    public static void click(By locator, String description) {
        LogManager.debug("Clicking on element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementClickable(locator);
            element.click();
            LogManager.info("Successfully clicked on element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to click on element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "click_failed");
            throw e;
        }
    }

    /**
     * Clicks a WebElement that has already been located.
     *
     * @param element     WebElement to click
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Click on element: {description}")
    public static void click(WebElement element, String description) {
        LogManager.debug("Clicking on WebElement: {}", description);
        try {
            WebElement clickableElement = WaitManager.waitForElementClickable(element, WaitManager.getDefaultTimeout());
            clickableElement.click();
            LogManager.info("Successfully clicked on WebElement: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to click on WebElement '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "click_failed");
            throw e;
        }
    }

    /**
     * Performs a JavaScript click — useful for elements that are covered or not truly clickable.
     *
     * @param locator     By locator of the target element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("JavaScript click on element: {description}")
    public static void javascriptClick(By locator, String description) {
        LogManager.debug("Performing JavaScript click on element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementPresent(locator);
            WebDriver driver = DriverManager.getDriver();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            LogManager.info("Successfully performed JavaScript click on element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed JavaScript click on element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "javascript_click_failed");
            throw e;
        }
    }

    // =================== INPUT ACTIONS ===================

    @Step("Send text '{text}' to element: {description}")
    public static void sendKeys(By locator, String text, String description) {
        // FIX 2: was: LogManager.debug("Sending text '{}' to element: {}", text);  ← missing description arg
        LogManager.debug("Sending text '{}' to element: {}", text, description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            element.clear();
            element.sendKeys(text);
            LogManager.info("Successfully sent text '{}' to element: {}", text, description);
        } catch (Exception e) {
            // FIX 2: was: LogManager.error("Failed to send text to element '{}': {}", e.getMessage());  ← wrong args
            LogManager.error("Failed to send text to element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "send_keys_failed");
            throw e;
        }
    }



    /**
     * Clears the text content of an element.
     *
     * @param locator     By locator of the element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Clear text from element: {description}")
    public static void clear(By locator, String description) {
        LogManager.debug("Clearing text from element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            element.clear();
            LogManager.info("Successfully cleared text from element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to clear text from element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "clear_failed");
            throw e;
        }
    }

    /**
     * Submits a form by calling submit() on the given element.
     *
     * @param locator     By locator of the form element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Submit form element: {description}")
    public static void submit(By locator, String description) {
        LogManager.debug("Submitting form element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            element.submit();
            LogManager.info("Successfully submitted form element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to submit form element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "submit_failed");
            throw e;
        }
    }

    // =================== GET ACTIONS ===================

    /**
     * Returns the visible text of an element.
     *
     * @param locator     By locator of the element
     * @param description Human-readable label for logging and Allure step
     * @return Visible text content of the element
     */
    @Step("Get text from element: {description}")
    public static String getText(By locator, String description) {
        LogManager.debug("Getting text from element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            String text = element.getText();
            LogManager.debug("Retrieved text '{}' from element: {}", text, description);
            return text;
        } catch (Exception e) {
            LogManager.error("Failed to get text from element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "get_text_failed");
            throw e;
        }
    }



    @Step("Get text (with JS fallback) from element: {description}")
    public static String getTextWithJsFallback(By locator, String description) {
        LogManager.debug("Getting text from element with JS fallback: {}", description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            String text = element.getText();

            if (text == null || text.trim().isEmpty()) {
                WebDriver driver = DriverManager.getDriver();
                text = (String) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].innerText || arguments[0].textContent;", element);
                LogManager.debug("Retrieved text using JavaScript fallback: '{}'", text);
            } else {
                LogManager.debug("Retrieved text normally: '{}'", text);
            }
            return text;
        } catch (Exception e) {
            LogManager.error("Failed to get text from element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "get_text_js_failed");
            throw e;
        }
    }

    /**
     * Returns the value of a specific HTML attribute from an element.
     *
     * @param locator        By locator of the element
     * @param attributeName  Name of the HTML attribute
     * @param description    Human-readable label for logging and Allure step
     * @return Value of the attribute, or null if not present
     */
    @Step("Get attribute '{attributeName}' from element: {description}")
    public static String getAttribute(By locator, String attributeName, String description) {
        LogManager.debug("Getting attribute '{}' from element: {}", attributeName, description);
        try {
            WebElement element = WaitManager.waitForElementPresent(locator);
            String value = element.getAttribute(attributeName);
            LogManager.debug("Retrieved attribute '{}' = '{}' from element: {}", attributeName, value, description);
            return value;
        } catch (Exception e) {
            LogManager.error("Failed to get attribute from element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "get_attribute_failed");
            throw e;
        }
    }

    // =================== VISIBILITY / PRESENCE CHECKS ===================

    /**
     * Returns true if the element is currently visible on the page.
     *
     * @param locator     By locator of the element
     * @param description Human-readable label for logging and Allure step
     * @return true if element is displayed, false otherwise
     */
    @Step("Check if element is displayed: {description}")
    public static boolean isDisplayed(By locator, String description) {
        LogManager.debug("Checking if element is displayed: {}", description);
        try {
            WebElement element = WaitManager.waitForElementPresent(locator, 5);
            boolean displayed = element.isDisplayed();
            LogManager.debug("Element '{}' is displayed: {}", description, displayed);
            return displayed;
        } catch (Exception e) {
            LogManager.debug("Element '{}' is not displayed: {}", description, e.getMessage());
            return false;
        }
    }

    /**
     * Returns true if the element exists in the DOM (does not need to be visible).
     *
     * @param locator     By locator of the element
     * @param description Human-readable label for logging and Allure step
     * @return true if element is present in DOM, false otherwise
     */
    @Step("Check if element exists: {description}")
    public static boolean isPresent(By locator, String description) {
        LogManager.debug("Checking if element exists: {}", description);
        try {
            WaitManager.waitForElementPresent(locator, 2);
            LogManager.debug("Element '{}' is present in DOM", description);
            return true;
        } catch (Exception e) {
            LogManager.debug("Element '{}' is not present in DOM: {}", description, e.getMessage());
            return false;
        }
    }

    // =================== COLLECTION ACTIONS ===================

    /**
     * Returns all elements matching the given locator.
     *
     * @param locator     By locator shared by all target elements
     * @param description Human-readable label for logging and Allure step
     * @return List of matching WebElements
     */
    @Step("Find all elements: {description}")
    public static List<WebElement> findElements(By locator, String description) {
        LogManager.debug("Finding all elements: {}", description);
        try {
            List<WebElement> elements = WaitManager.waitForElementsPresent(locator, WaitManager.getDefaultTimeout());
            LogManager.debug("Found {} elements for: {}", elements.size(), description);
            return elements;
        } catch (Exception e) {
            LogManager.error("Failed to find elements '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "find_elements_failed");
            throw e;
        }
    }

    // =================== ADVANCED ACTIONS ===================

    /**
     * Scrolls the page so the element is within the visible viewport.
     *
     * @param locator     By locator of the target element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Scroll to element: {description}")
    public static void scrollToElement(By locator, String description) {
        LogManager.debug("Scrolling to element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementPresent(locator);
            WebDriver driver = DriverManager.getDriver();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            WaitManager.sleep(300);
            LogManager.info("Successfully scrolled to element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to scroll to element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "scroll_failed");
            throw e;
        }
    }

    /**
     * Moves the mouse cursor over an element (hover / mouse-over).
     *
     * @param locator     By locator of the target element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Hover over element: {description}")
    public static void hover(By locator, String description) {
        LogManager.debug("Hovering over element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            WebDriver driver = DriverManager.getDriver();
            new Actions(driver).moveToElement(element).perform();
            LogManager.info("Successfully hovered over element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to hover over element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "hover_failed");
            throw e;
        }
    }

    /**
     * Double-clicks an element.
     *
     * @param locator     By locator of the target element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Double click on element: {description}")
    public static void doubleClick(By locator, String description) {
        LogManager.debug("Double clicking on element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementClickable(locator);
            WebDriver driver = DriverManager.getDriver();
            new Actions(driver).doubleClick(element).perform();
            LogManager.info("Successfully double clicked on element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to double click on element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "double_click_failed");
            throw e;
        }
    }

    /**
     * Right-clicks (context-clicks) an element.
     *
     * @param locator     By locator of the target element
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Right click on element: {description}")
    public static void rightClick(By locator, String description) {
        LogManager.debug("Right clicking on element: {}", description);
        try {
            WebElement element = WaitManager.waitForElementClickable(locator);
            WebDriver driver = DriverManager.getDriver();
            new Actions(driver).contextClick(element).perform();
            LogManager.info("Successfully right clicked on element: {}", description);
        } catch (Exception e) {
            LogManager.error("Failed to right click on element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "right_click_failed");
            throw e;
        }
    }

    // =================== DROPDOWN ACTIONS ===================

    /**
     * Selects a dropdown option by its visible text label.
     *
     * @param locator     By locator of the {@code <select>} element
     * @param optionText  Exact visible text of the option to select
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Select option '{optionText}' from dropdown: {description}")
    public static void selectByText(By locator, String optionText, String description) {
        LogManager.debug("Selecting option '{}' from dropdown: {}", optionText, description);
        try {
            WebElement dropdown = WaitManager.waitForElementVisible(locator);
            Select select = new Select(dropdown);
            select.selectByVisibleText(optionText);

            String selected = select.getFirstSelectedOption().getText();
            if (!optionText.equals(selected)) {
                throw new RuntimeException(
                        "Selection validation failed. Expected: '" + optionText + "', Selected: '" + selected + "'");
            }
            LogManager.info("Successfully selected option '{}' from dropdown: {}", optionText, description);
        } catch (Exception e) {
            LogManager.error("Failed to select option from dropdown '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "select_by_text_failed");
            throw e;
        }
    }

    /**
     * Selects a dropdown option by its value attribute.
     *
     * @param locator     By locator of the {@code <select>} element
     * @param value       Value attribute of the option to select
     * @param description Human-readable label for logging and Allure step
     */
    @Step("Select option by value '{value}' from dropdown: {description}")
    public static void selectByValue(By locator, String value, String description) {
        LogManager.debug("Selecting option by value '{}' from dropdown: {}", value, description);
        try {
            WebElement dropdown = WaitManager.waitForElementVisible(locator);
            Select select = new Select(dropdown);
            select.selectByValue(value);
            LogManager.info("Successfully selected option by value '{}' from dropdown: {}", value, description);
        } catch (Exception e) {
            LogManager.error("Failed to select option by value from dropdown '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "select_by_value_failed");
            throw e;
        }
    }

    // =================== PRIVATE HELPERS ===================

    /**
     * Silently captures a failure screenshot without interrupting the test flow.
     *
     * @param description Description of the element/action that failed
     * @param failureType Short label appended to the screenshot filename
     */
    private static void captureFailureScreenshot(String description, String failureType) {
        try {
            String name = description.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + failureType;
            Screenshot.captureFailureScreenshot(name);
        } catch (Exception ignored) {
            // Never let screenshot failure propagate and hide the real error
        }
    }

    @Step("Press {key} on element: {description}")
    public static void pressKey(By locator, Keys key, String description) {
        LogManager.debug("Pressing {} on element: {}", key.name(), description);
        try {
            WebElement element = WaitManager.waitForElementVisible(locator);
            element.sendKeys(key);
            LogManager.info("Successfully pressed {} on element: {}", key.name(), description);
        } catch (Exception e) {
            LogManager.error("Failed to press key on element '{}': {}", description, e.getMessage());
            captureFailureScreenshot(description, "press_key_failed");
            throw e;
        }
    }
}
