package io.levysworks;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class RootResourceTest {
    @Test
    void testRootEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(200)
             .body(is(""));
    }

}