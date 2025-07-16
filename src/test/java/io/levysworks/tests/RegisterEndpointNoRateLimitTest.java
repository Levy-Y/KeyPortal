package io.levysworks.tests;

import io.levysworks.configs.AgentsConfig;
import io.levysworks.profiles.NoRateLimitTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(NoRateLimitTestProfile.class)
public class RegisterEndpointNoRateLimitTest {
    @Inject
    AgentsConfig agentsConfig;

    @Test
    void testMissingAgentReturns400() {
        given()
                .when()
                .get("/api/v1/register")
                .then()
                .statusCode(400);
    }

    @Test
    void testUnknownAgentReturns404() {
        given()
                .queryParam("server", "unknown-agent")
                .when()
                .get("/api/v1/register")
                .then()
                .statusCode(404)
                .body(equalTo("Server not found"));
    }

    @Test
    void testValidAgentReturnsPrivateKey() {
        given()
                .queryParam("user", 1)
                .queryParam("server", agentsConfig.servers().getFirst().name())
                .when()
                .get("/api/v1/register")
                .then()
                .statusCode(200)
                .header("Content-Disposition", containsString("filename=\"id_rsa\""))
                .body(not(emptyString()));
    }
}
