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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

/**
 * Endpoint used by users to request new OpenSSH keys
 * <br>
 * Rate limited to 1 request per day
 */
@Path("/api/v1/register")
@RateLimit(value = 1, window = 1, windowUnit = ChronoUnit.DAYS)
@APIResponse(responseCode = "429", description = "Rate limit exceeded")
public class RegisterEndpoint {
    @Inject
    PollManager pollManager;

    @Inject
    DatabaseManager dbManager;

    /**
     * Generates an OpenSSH-compliant keypair and returns the private key as a downloadable file.
     * The corresponding public key is stored in the pending requests table for the specified server.
     *
     * @param user_uuid UUID of the requesting user
     * @param agent Name of the target server
     * @return HTTP response containing the private key as an attachment
     * @throws IOException if key generation fails
     * @throws InterruptedException if the key generation process is interrupted
     * @throws SQLException if a database error occurs
     * @throws RateLimitException if the request exceeds rate limits
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response registerKey(@QueryParam("user") String user_uuid, @QueryParam("server") String agent) throws IOException, InterruptedException, RateLimitException, SQLException {
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
            dbManager.addPendingRequest(user_uuid, agent, publicKey);
        } catch (PSQLException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User doesn't exist with uuid " + user_uuid).build();
        }

        return Response.ok(privateKey)
                .header("Content-Disposition", "attachment; filename=\"id_rsa\"")
                .build();
    }
}
