package pages;

import io.qameta.allure.Step;
import io.qameta.allure.Allure;
import org.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.utilities.browserActions.BrowserActions;
import org.utilities.elementActions.ElementActions;
import org.utilities.logs.LogManager;
import org.utilities.wait.WaitManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardPage {

    protected WebDriver driver;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }


    private static By adminBar(String menuItemName) {
        return By.xpath("//a[normalize-space()='" + menuItemName.trim() + "']");
    }



    @Step("Step 5 : Click on Admin tab on the left side menu")
    public SaveSystemUserPage clickOnAdmin() {
        ElementActions.click(adminBar("Admin"), "Admin Menu");
        return new SaveSystemUserPage(driver);
    }


}