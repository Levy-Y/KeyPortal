package io.levysworks.endpoints;

import io.levysworks.beans.DatabaseManager;
import io.levysworks.beans.PollManager;
import io.levysworks.utilities.KeyGenerator;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

@Path("/api/v1/register")
//@RateLimit(value = 1, window = 1, windowUnit = ChronoUnit.DAYS)
public class RegisterEndpoint {
    @Inject
    PollManager pollManager;

    @Inject
    DatabaseManager dbManager;

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response registerKey(@QueryParam("user") int userId, @QueryParam("server") String agent) throws IOException, InterruptedException, RateLimitException, SQLException {
        KeyGenerator.SSHKeyPair sshKeyPair = KeyGenerator.RegisterKeypair();

        String publicKey = sshKeyPair.getPublicKey();
        String privateKey = sshKeyPair.getPrivateKey();

        if (agent == null || agent.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!pollManager.checkAgentExists(agent)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Server not found").build();
        }

        try {
            dbManager.addPendingRequest(userId, agent, publicKey);
        } catch (PSQLException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User doesn't exist with id " + userId).build();
        }

        return Response.ok(privateKey)
                .header("Content-Disposition", "attachment; filename=\"id_rsa\"")
                .build();
    }
}
