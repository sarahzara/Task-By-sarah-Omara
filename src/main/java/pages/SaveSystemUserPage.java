package pages;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.utilities.browserActions.BrowserActions;
import org.utilities.elementActions.ElementActions;
import org.utilities.logs.LogManager;
import org.utilities.wait.WaitManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SaveSystemUserPage {

    private final WebDriver driver;
    public SaveSystemUserPage(WebDriver driver) {
        this.driver = driver;
    }
    private static final ConfigReader config = ConfigReader.getInstance();
    private int initialRecordCount;
    private int countAfterAddition;
    private int countBeforeDeletion;


    // =================== LOCATORS ===================
    private final By recordsCountSpan = By.cssSelector(
            "div.orangehrm-horizontal-padding.orangehrm-vertical-padding " +
                    "span.oxd-text.oxd-text--span"
    );

    private final By addButton = By.xpath("//button[normalize-space()='Add']");
    private final By userRoleTrigger = By.xpath("(//div[contains(@class,'oxd-select-text-input')])[1]");
    private final By statusTrigger = By.xpath("(//div[contains(@class,'oxd-select-text-input')])[2]");
    private final By employeeNameInput = By.xpath("//input[@placeholder='Type for hints...']");
    private final By autocompleteSuggestions = By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option span");
    private final By usernameInput = By.xpath("//label[normalize-space()='Username']" + "/ancestor::div[contains(@class,'oxd-input-group')]" + "//input[contains(@class,'oxd-input')]");
    private final By passwordInput = By.xpath("(//input[@type='password'])[1]");
    private final By confirmPasswordInput = By.xpath("(//input[@type='password'])[2]");
    private final By saveButton   = By.xpath("//button[normalize-space()='Save']");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");
    private final By successToast = By.cssSelector(".oxd-toast--success");
    private final By userNameSearch = By.xpath("(//input[@class='oxd-input oxd-input--active'])[2]");
    private final By searchButton = By.xpath("//button[normalize-space()='Search']");
    private final By yesButton_Delete = By.xpath("//button[normalize-space()='Yes, Delete']");
    private final By resetButton = By.xpath("//button[normalize-space()='Reset']");

    private static By deleteButtonForUsername(String username) {
        return By.xpath("//div[contains(@class, 'oxd-table-card')][.//div[text()='" + username + "']]//button[i[contains(@class, 'bi-trash')]]");
    }







    // =================== Methods ACTIONS ===================


    @Step("Step 6: Get the number of records found - Current record count")
    public SaveSystemUserPage getRecordCount() {
        initialRecordCount = extractRecordCount();
        LogManager.info("Captured initial record count: {}", initialRecordCount);
        Allure.parameter("Initial Record Count", String.valueOf(initialRecordCount));
        return this;
    }

    @Step("Step 7: Click Add button to open Add User form")
    public SaveSystemUserPage clickAddButton() {
        ElementActions.click(addButton, "Add button");
        WaitManager.waitForElementVisible(userRoleTrigger, config.getExplicitWaitTimeout());
        return this;
    }


    @Step("Step 8: Fill Add User form with data - User Role: {userRole}, Employee: {employeeName}, Status: {status}, Username: {username}")
    public SaveSystemUserPage fillUserForm(
            String userRole,
            String employeeName,
            String status,
            String username,
            String password) {
        selectDropdown(userRoleTrigger, userRole, "User Role");
        selectAutocomplete(employeeNameInput, employeeName, "Employee Name");
        selectDropdown(statusTrigger, status, "Status");
        ElementActions.sendKeys(usernameInput, username, "Username field - Value: " + username);
        ElementActions.sendKeys(passwordInput, password, "Password field");
        ElementActions.sendKeys(confirmPasswordInput, password, "Confirm Password field");

        return this;
    }


    @Step("Step 9: Click Save button and confirm success")
    public SaveSystemUserPage clickSaveButton() {
        ElementActions.click(saveButton, "Save button");
        WaitManager.waitForElementVisible(successToast, config.getExplicitWaitTimeout());
        BrowserActions.waitForPageLoad();
        LogManager.info("Save confirmed — success toast is visible");
        return this;
    }


    @Step("Step 10: Verify that the number of records increased by 1")
    public SaveSystemUserPage verifyRecordCountIncreasedByOne() {
        countAfterAddition = extractRecordCount();
        int expectedCount = initialRecordCount + 1;
        
        Allure.parameter("Record Count - Before Adding User", String.valueOf(initialRecordCount));
        Allure.parameter("Record Count - After Adding User", String.valueOf(countAfterAddition));
        Allure.parameter("Record Count - Expected After Adding", String.valueOf(expectedCount));
        
        Assert.assertEquals(countAfterAddition, expectedCount,
                String.format("❌ Record count did not increase by 1 after adding user. Before=%d, After=%d, Expected=%d",
                        initialRecordCount, countAfterAddition, expectedCount));
        
        LogManager.info("✅ Record count increased correctly: {} → {}", initialRecordCount, countAfterAddition);
        return this;
    }

    @Step("Step 11: Search with the username for the new user - Username: {searchUsername}")
    public SaveSystemUserPage searchByUserName(String searchUsername){
        Allure.parameter("Username Searched", searchUsername);
        ElementActions.sendKeys(userNameSearch, searchUsername, "Username search field");
        ElementActions.click(searchButton, "Search button");
        return this;
    }

    @Step("Step 12: Capture record count before deletion")
    public SaveSystemUserPage captureCountBeforeDeletion() {
        countBeforeDeletion = extractRecordCount();
        LogManager.info("Captured record count before deletion: {}", countBeforeDeletion);
        Allure.parameter("Record Count - Before Deletion", String.valueOf(countBeforeDeletion));
        return this;
    }

    @Step("Step 12: Delete the new user - Username: {userToDelete}")
    public SaveSystemUserPage deleteUser(String userToDelete){
        countBeforeDeletion = extractRecordCount();
        LogManager.info("Captured record count before deletion: {}", countBeforeDeletion);
        Allure.parameter("Record Count - Before Deletion", String.valueOf(countBeforeDeletion));
        Allure.parameter("User Deleted", userToDelete);

        ElementActions.click(deleteButtonForUsername(userToDelete), "Delete button for user: " + userToDelete);
        ElementActions.click(yesButton_Delete, "Confirm Delete - Yes, Delete button");
        WaitManager.waitForElementVisible(successToast, config.getExplicitWaitTimeout());
        return this;
    }



    public SaveSystemUserPage resetUserRecord(){
        ElementActions.click(resetButton, "Reset button");
        BrowserActions.waitForPageLoad();
        return this;
    }

    @Step("Step 13: Verify that the number of records decreased by 1")
    public SaveSystemUserPage verifyRecordCountDecreasedByOne() {
        int countAfterDeletion = extractRecordCount();
        int expectedCount = countAfterAddition - 1;

        Allure.parameter("Record Count - Before Adding User",   String.valueOf(initialRecordCount));
        Allure.parameter("Record Count - After Adding User",    String.valueOf(countAfterAddition));
        Allure.parameter("Record Count - After Deletion",       String.valueOf(countAfterDeletion));
        Allure.parameter("Record Count - Expected After Deletion", String.valueOf(expectedCount));

        Assert.assertEquals(countAfterDeletion, expectedCount,
                String.format("❌ Record count did not decrease by 1 after deleting user. " +
                                "AfterAdd=%d, AfterDelete=%d, Expected=%d",
                        countAfterAddition, countAfterDeletion, expectedCount));

        LogManager.info("✅ Record count decreased correctly: {} → {}", countAfterAddition, countAfterDeletion);
        return this;
    }





    private int extractRecordCount() {
        String text = driver.findElement(recordsCountSpan).getText();
        Matcher matcher = Pattern.compile("\\d+").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        throw new RuntimeException("Could not extract record count from text: " + text);
    }









    // =================== PRIVATE HELPERS ===================

    private void selectDropdown(By triggerLocator, String optionText, String fieldName) {
        LogManager.debug("Custom dropdown '{}' — opening → selecting '{}'", fieldName, optionText);
        ElementActions.click(triggerLocator, fieldName + " trigger");
        By optionLocator = By.xpath(
                "//div[contains(@class,'oxd-select-dropdown')]" +
                        "//span[normalize-space()='" + optionText + "']"
        );
        WaitManager.waitForElementVisible(optionLocator, config.getExplicitWaitTimeout());
        ElementActions.click(optionLocator, fieldName + " option: " + optionText);
    }

    private void selectAutocomplete(By inputLocator, String searchText, String fieldName) {
        LogManager.debug("Autocomplete '{}' — typing '{}'", fieldName, searchText);
        ElementActions.sendKeys(inputLocator, searchText, fieldName);
        WaitManager.waitForElementVisible(autocompleteSuggestions, config.getExplicitWaitTimeout());
        By matchingOption = By.xpath(
                "//div[contains(@class,'oxd-autocomplete-dropdown')]" +
                        "//span[contains(normalize-space(),'" + searchText + "')]"
        );
        By firstOption = By.cssSelector(
                ".oxd-autocomplete-dropdown .oxd-autocomplete-option:first-child"
        );

        try {
            WaitManager.waitForElementVisible(matchingOption, 3);
            ElementActions.click(matchingOption, fieldName + " → " + searchText);
        } catch (Exception e) {
            LogManager.debug("No exact match found — clicking first suggestion for '{}'", fieldName);
            ElementActions.click(firstOption, fieldName + " first suggestion");
        }

        LogManager.debug("Autocomplete '{}' — selection complete", fieldName);
    }

}
