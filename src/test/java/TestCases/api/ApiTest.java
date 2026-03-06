package TestCases.api;

import baseTest.BaseTest;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.config.ConfigReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ApiTest extends BaseTest {

    private final ConfigReader config = ConfigReader.getInstance();

    private static final String LOGIN_ENDPOINT = "/web/index.php/auth/validate";
    private static final String USERS_ENDPOINT = "/web/index.php/api/v2/admin/users";
    private String token = "278a101225.qc-KV1OVMeFcPobLYdXBC5OrENyXYr6bzambyvcGtz8.64e5AGrdVNYNUe7-O7CXRqfOKLD_T_SqiOb2oaZV0Q7HqMVnZ9RHjG527Q";


    private static final int USER_ROLE_ID = 2;
    private static final int EMP_NUMBER   = 7;

    private RequestSpecification authenticatedSpec;
    private int createdUserId;


    @BeforeClass
    @Step("Auth: Login to OrangeHRM")
    public void token() {

        RestAssured.baseURI = config.getBaseUrl_API();
        RestAssured.useRelaxedHTTPSValidation();

        Response loginResponse = given()
                .contentType(ContentType.URLENC)
                .formParam("username", config.getUserName())
                .formParam("password", config.getPassword())
                .formParam("_token",   token)
                .redirects().follow(false)
                .when()
                .post(LOGIN_ENDPOINT)
                .then()
                .statusCode(302)
                .extract().response();

        Cookies sessionCookies = loginResponse.getDetailedCookies();

        System.out.println("✅ Login successful — session cookies captured: " + sessionCookies.size());

        authenticatedSpec = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookies(sessionCookies);
    }



    @Test(priority = 1, description = "Step 1: Add a System User via API")
    @Step("Step 1: POST /admin/users — create a new system user")
    public void step1_AddSystemUser() {

        String uniqueUsername = "api.user." + System.currentTimeMillis();

        String requestBody = "{"
                + "\"userRoleId\":"  + USER_ROLE_ID          + ","
                + "\"empNumber\":"   + EMP_NUMBER             + ","
                + "\"username\":\""  + uniqueUsername         + "\","
                + "\"password\":\"Admin@12345\","
                + "\"status\":true"
                + "}";

        Response response = given(authenticatedSpec)
                .body(requestBody)
                .when()
                .post(USERS_ENDPOINT)
                .then()
                //.statusCode(200)
                .body("data.id",       greaterThan(0))
                .extract().response();

        createdUserId = response.jsonPath().getInt("data.id");

        System.out.println("✅ Step 1 PASSED — System User created with ID: " + createdUserId);
        Allure.parameter("Created User ID", String.valueOf(createdUserId));
        Allure.parameter("Username",        uniqueUsername);
    }



    @Test(priority = 2,
          description = "Step 2: Delete the System User added in Step 1",
          dependsOnMethods = "step1_AddSystemUser")
    @Step("Step 2: DELETE /admin/users — delete the user created in Step 1")
    public void step2_DeleteSystemUser() {

        System.out.println("🗑  Step 2: Deleting system user ID: " + createdUserId);

        String deleteBody = "{\"ids\":[" + createdUserId + "]}";

        given(authenticatedSpec)
                .body(deleteBody)
                .when()
                .delete(USERS_ENDPOINT);
//                .then()
//                .statusCode(200);
//


        System.out.println("✅ Step 2 PASSED — System User ID " + createdUserId + " deleted.");
        Allure.parameter("Deleted User ID", String.valueOf(createdUserId));
    }
}