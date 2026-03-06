package TestCases.ui;

import baseTest.BaseTest;
import org.config.ConfigReader;
import org.testng.annotations.Test;
import pages.LoginPage;

public class TestCases extends BaseTest {

    private final ConfigReader config = ConfigReader.getInstance();
    String user = "user"+System.currentTimeMillis();


    @Test()
    public void test(){
      LoginPage loginPage = new LoginPage(getDriver());

      loginPage.waitPage()
              .loginWithValidCreditional(config.getUserName(),config.getPassword())
              .AssertLoginAndWaitForDashboard()
              .clickOnAdmin()
              .getRecordCount()
              .clickAddButton()
              .fillUserForm("Admin" , "sww  test","Enabled" , user,"admin123")
              .clickSaveButton()
              .verifyRecordCountIncreasedByOne()
              .searchByUserName(user)
              .deleteUser(user)
              .resetUserRecord()
              .verifyRecordCountDecreasedByOne();
    }

}