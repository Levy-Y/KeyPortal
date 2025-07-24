//package io.levysworks.tests;
//
//import io.quarkus.test.junit.QuarkusIntegrationTest;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.containsString;
//
//@QuarkusIntegrationTest
//public class RootPageTestIT {
//    @Test
//    public void testBuiltRootPage() {
//        given()
//                .when().get("/")
//                .then()
//                .statusCode(200)
//                .contentType(ContentType.HTML)
//                .body(containsString("<form id=\"sshKeyForm\""))
//                .body(containsString("Generate SSH Key"))
//                .body(containsString("User ID:"))
//                .body(containsString("Server:"));
//    }
//}
//
//
