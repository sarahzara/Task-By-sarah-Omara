package pages;



import io.qameta.allure.Step;
import org.Base.BaseManager;
import org.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.utilities.browserActions.BrowserActions;
import org.utilities.elementActions.ElementActions;
import org.utilities.screenshot.Screenshot;
import org.utilities.wait.WaitManager;

    public class LoginPage {

        protected WebDriver driver;
        private static final ConfigReader config = ConfigReader.getInstance();


        private final By userNameField = By.xpath("(//input[@placeholder=\'Username\'])[1]");
        private final By passwordField = By.xpath("(//input[@placeholder=\'Password\'])[1]");
        private final By loginButton   = By.xpath("(//button[normalize-space()=\'Login\'])[1]");

        public LoginPage(WebDriver driver) {
            this.driver = driver;
        }

        // =================== PAGE ACTIONS ===================


        @Step("Step 1: Wait for login page to load")
        public LoginPage waitPage() {
            WaitManager.waitForElementVisible(userNameField, config.getExplicitWaitTimeout());
            BrowserActions.waitForPageLoad();
            return this;
        }

        @Step("Step 2-4: Login with username: {username} and password: {password}")
        public LoginPage loginWithValidCreditional(String username, String password) {
            ElementActions.sendKeys(userNameField, username, "Username field");
            ElementActions.sendKeys(passwordField, password, "Password field");
            ElementActions.click(loginButton, "Login button");
            return this;
        }


        @Step("Verify login successful and wait for dashboard")
        public DashboardPage AssertLoginAndWaitForDashboard() {
            BrowserActions.takeScreenshot("Login successful - Navigating to Dashboard");
            BrowserActions.waitForPageLoad();
            return new DashboardPage(driver);
        }
    }