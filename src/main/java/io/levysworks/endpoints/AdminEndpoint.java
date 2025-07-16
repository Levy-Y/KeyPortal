package io.levysworks.endpoints;

import io.levysworks.beans.DatabaseManager;
import io.levysworks.beans.LogManager;
import io.levysworks.beans.PollManager;
import io.levysworks.models.ActionRequest;
import io.levysworks.models.Request;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
    public Response handleRequestAction(@PathParam("id") Integer id, ActionRequest body) throws SQLException {
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
            pollManager.addKeyForAgent(request.userId(), request.server(), request.key());
            dbManager.addActiveKey(request.userId(), request.server(), request.key());
        }

        dbManager.removePendingRequest(id);

        String adminName = "admin1";

        String actionTmp = action.equals("approve") ? "approved" : "declined";
        logManager.log("Request " + actionTmp, "A request with id: " + id + " has been " + actionTmp );

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
