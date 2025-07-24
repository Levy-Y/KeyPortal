package io.levysworks.tests;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusIntegrationTest
public class AdminPageTestIT extends AdminPageTest {
    @Test
    public void testAdminPageLoadsJVM() {
        given()
                .when()
                .get("/management/admin")
                .then()
                .statusCode(200)
                .body(containsString("<title>SSH Key Manager</title>"));
    }
}
