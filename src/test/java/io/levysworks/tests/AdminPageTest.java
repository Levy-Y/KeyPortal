package io.levysworks.tests;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class AdminPageTest {
    public void testAdminPageLoadsNative() {
        given()
                .when()
                .get("/management/admin")
                .then()
                .statusCode(200)
                .body(containsString("<title>SSH Key Manager</title>"));
    }
}
