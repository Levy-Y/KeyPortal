//package io.levysworks.tests;
//
//import io.levysworks.configs.AgentsConfig;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.containsString;
//
//@QuarkusTest
//class RootPageTest {
//    @Inject
//    AgentsConfig agentsConfig;
//
//    @Test
//    void testRootEndpointReturnsHtmlWithExpectedElements() {
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
//
//    @Test
//    void testAllServersFromAgentsConfigAreRendered() {
//        for (var server : agentsConfig.servers()) {
//            given()
//                    .when().get("/")
//                    .then()
//                    .body(containsString("<option value=\"" + server.name() + "\">" + server.name() + "</option>"));
//        }
//    }
//
//    @Test
//    void testDownloadSectionIsPresentButInitiallyHidden() {
//        given()
//                .when().get("/")
//                .then()
//                .body(containsString("id=\"downloadSection\""))
//                .body(containsString("class=\"download-section hidden\""));
//    }
//
//}