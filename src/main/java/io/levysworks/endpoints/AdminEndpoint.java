package io.levysworks.endpoints;

import io.levysworks.beans.DatabaseManager;
import io.levysworks.beans.LogManager;
import io.levysworks.beans.PollManager;
import io.levysworks.models.requests.ActionRequest;
import io.levysworks.models.requests.Request;
import io.levysworks.models.requests.UserRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

/**
 * Endpoint reserved for admin actions
 */
@Path("/api/v1/secured")
public class AdminEndpoint {
    @Inject
    DatabaseManager dbManager;

    @Inject
    PollManager pollManager;

    @Inject
    LogManager logManager;
    @Inject
    jakarta.ws.rs.core.Request request;

    /**
     * Handles PATCH request
     * <p>
     * Approves or declines a user request based on what the request body contains
     *
     * @param id ID of the {@code request} to work on
     * @param body The body containing the action
     * @return a {@link Response} with {@code 204 No Content} if successful, or an appropriate error code
     * @throws SQLException if a database access error occurs
     * @throws NoSuchAlgorithmException in case the {@link DatabaseManager#addActiveKey} fails to fingerprint the public key
     */
    @PATCH
    @Path("/requests/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleRequestAction(@PathParam("id") Integer id, ActionRequest body) throws SQLException, NoSuchAlgorithmException {
        String action = body.action();

        if (id < 0 || action == null || action.isBlank() ||
                (!action.equals("approve") && !action.equalsIgnoreCase("decline"))) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Request request = dbManager.getPendingRequestById(id);

        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (action.equals("approve")) {
            try {
                pollManager.addKeyForAgent(request.server(), request.key());
                dbManager.addActiveKey(request.user_uuid(), request.server(), request.key());
            } catch (SQLException | TimeoutException | IOException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        dbManager.removePendingRequest(id);

        // TODO: Remove constant admin name
        String adminName = "admin1";

        String actionTmp = action.equals("approve") ? "approved" : "declined";
        logManager.log("Request " + actionTmp, "A request with id: " + id + " has been " + actionTmp + " by " + adminName);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Handles DELETE request
     * <p>
     * Revokes the key with matching {@code uid} from the matching {@code agent}
     * @param uid The UID of the key
     * @param agent Name of the agent
     * @return a {@link Response} with {@code 204 No Content} if successful, or an appropriate error code
     * @throws SQLException if a database access error occurs
     * @throws TimeoutException if the RabbitMQ request times out
     */
    @DELETE
    @Path("/remove/{uid}")
    public Response handleRemoveRequest(@PathParam("uid") String uid, @QueryParam("agent") String agent) throws SQLException, TimeoutException {
        if (agent == null || agent.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!dbManager.checkKeyExists(uid)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            pollManager.removeKeyFromAgent(agent, uid);
            dbManager.removeKeyByUID(agent, uid);

            logManager.log("Revoked", "Revoked user key with " + uid + " from " + agent);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles PATCH request
     * <p>
     * Updates the user identified by {@code uuid} with data from the {@link UserRequest}
     * @param uuid The UUID of the updatable user
     * @param body A {@link UserRequest} object containing the information to update the user to
     * @return a {@link Response} with {@code 204 No Content} if successful, or an appropriate error code
     * @throws SQLException if a database access error occurs
     */
    @PATCH
    @Path("/users/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleUserAction(@PathParam("uuid") String uuid, UserRequest body) throws SQLException {
        if (body == null || uuid == null || uuid.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            dbManager.updateUser(uuid, body.first_name(), body.last_name(), body.email(), body.department(), body.notes());
        } catch (SQLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        logManager.log("User Profile Updated", "Updated user with UUID: " + uuid);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Handles POST request
     * <p>
     * Creates a new user with data from the {@link UserRequest}
     * @param body A {@link UserRequest} object containing the information to create the user from
     * @return a {@link Response} with {@code 204 No Content} if successful, or an appropriate error code
     * @throws SQLException if a database access error occurs
     */
    @POST
    @Path("/users/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleCreateUserAction(UserRequest body) throws SQLException {
        if (body == null ) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            dbManager.addUser(body.first_name(), body.last_name(), body.email(), body.department(), body.notes());
        } catch (SQLException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        logManager.log("Created", "New user created for email: " + body.email());

        return Response.status(204).build();
    }

    /**
     * Handles DELETE request
     * <p>
     * Deletes the user with a matching {@code uuid}
     * @param uuid UUID of the user to delete
     * @return a {@link Response} with {@code 204 No Content} if successful, or an appropriate error code
     * @throws SQLException if a database access error occurs
     */
    @DELETE
    @Path("/users/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleRemoveUserAction(@QueryParam("uuid") String uuid) throws SQLException {
        if (uuid == null || uuid.isBlank() ) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            dbManager.removeUser(uuid);
        } catch (SQLException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        logManager.log("Deleted", "User deleted with UUID: " + uuid);

        return Response.status(204).build();
    }

}
