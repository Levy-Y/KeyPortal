package io.levysworks.tests;

import io.levysworks.configs.AgentsConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RegisterEndpointWithRateLimitTest {
    @Inject
    AgentsConfig agentsConfig;

    @Test
    void testRegisterEndpointRateLimit() {
        String agent = agentsConfig.servers().getFirst().name();

        given()
                .queryParam("server", agent)
                .when()
                .get("/api/v1/register")
                .then()
                .statusCode(200);

        given()
                .queryParam("server", agent)
                .when()
                .get("/api/v1/register")
                .then()
                .statusCode(429);
    }
}
