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

        logManager.log("Updated", "Updated user with UUID: " + uuid);

        return Response.status(204).build();
    }

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

}
