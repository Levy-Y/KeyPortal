package io.levysworks.tests;

import io.levysworks.beans.PollManager;
import io.levysworks.configs.AgentsConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
public class KeyPollEndpointTest {
    @Inject
    AgentsConfig agentsConfig;

    @Inject
    PollManager pollManager;

    @Test
    void testPollEndpointReturns204WhenEmpty() {
        given()
                .header("X-Agent-Name", agentsConfig.servers().getFirst().name())
                .header("X-Poll-Key", agentsConfig.servers().getFirst().poll_key())
                .accept("application/json")
                .when()
                .get("/api/v1/poll")
                .then()
                .statusCode(204);
    }

    @Test
    void testPollEndpointReturns200WithKeys() {
        var agent = agentsConfig.servers().getFirst();
        pollManager.addKeyForAgent(1, agent.name(), "ssh-rsa abc");

        given()
                .header("X-Agent-Name", agent.name())
                .header("X-Poll-Key", agent.poll_key())
                .accept("application/json")
                .when()
                .get("/api/v1/poll")
                .then()
                .statusCode(200)
                .body("$", hasItem("ssh-rsa abc"));
    }

    @Test
    void testPollEndpointReturn400ForMissingHeaders() {
        given()
                .accept("application/json")
                .when()
                .get("/api/v1/poll")
                .then()
                .statusCode(400);
    }

    @Test
    void testPollEndpointReturn403ForNonExistingAgentNameKeyPair() {
        var agent = agentsConfig.servers().getFirst();
        var key = "test-key";

        given()
                .header("X-Agent-Name", agent.name())
                .header("X-Poll-Key", key)
                .accept("application/json")
                .when()
                .get("/api/v1/poll")
                .then()
                .statusCode(401);
    }
}
