package io.levysworks.endpoints.pages;

import io.levysworks.beans.HealthManager;
import io.levysworks.configs.AgentsConfig;
import io.levysworks.beans.DatabaseManager;
import io.levysworks.models.*;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;

/**
 * Endpoint serving the admin interface, including the dashboard, user profiles, and user creation form.
 */
@Path("/management/admin")
public class AdminPage {
    @Inject
    Template admin;

    @Inject
    Template profile;

    @Inject
    Template adduser;

    @Inject
    Template notfound;

    @Inject
    AgentsConfig agentsConfig;

    @Inject
    DatabaseManager dbManager;

    @Inject
    HealthManager healthManager;

    /**
     * Handles GET requests to the admin page.
     * <p>
     * Renders system-wide admin dashboard with statistics, user/request/key data, and logs.
     *
     * @return HTML response with rendered admin view
     * @throws SQLException if a database access error occurs
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response admin() throws SQLException {
        int activeKeys = dbManager.getKeyCount();
        int pendingRequestCount = dbManager.getPendingRequestCount();
        int userCount = dbManager.getUserCount();
        int serverCount = agentsConfig.servers().size();

        boolean isSystemHealthy = healthManager.isSystemHealthy();

        List<CompositeUserData> requests = dbManager.getRequestsForTemplate();
        List<CompositeUserData> keys = dbManager.getKeysForTemplate();
        List<CompositeUserData> users = dbManager.getUsersForTemplate();
        List<CompositeUserData> logs = dbManager.getLogs(20);

        return Response.ok(
                admin
                        .data("healthy", isSystemHealthy)
                        .data("activeKeyCount", activeKeys)
                        .data("pendingRequestCount", pendingRequestCount)
                        .data("userCount", userCount)
                        .data("serverCount", serverCount)
                        .data("requests", requests)
                        .data("logs", logs)
                        .data("keys", keys)
                        .data("users", users)
                        .render()
        ).build();
    }

    /**
     * Handles GET requests for an individual user's profile page.
     * <p>
     * Retrieves user data and associated keys by UUID. If the user is not found, renders a not-found page.
     *
     * @param query_user the UUID of the user
     * @return rendered HTML response with the user profile or not-found page
     * @throws SQLException if a database access error occurs
     */
    @GET
    @Path("/user/{query_user}")
    @Produces(MediaType.TEXT_HTML)
    public Response handleUserProfileRequest(@PathParam("query_user") String query_user) throws SQLException {
        CompositeUserData user = dbManager.getUserByUUID(query_user);

        if (user != null) {
            List<CompositeUserData> keys = dbManager.getUserKeysByUUID(query_user);

            return Response.ok(
                    profile
                            .data("user", user)
                            .data("keys", keys)
                            .render()
            ).build();
        } else {
            return Response.ok(
                    notfound
                            .data("uuid", query_user)
                            .render()
            ).build();
        }
    }

    /**
     * Handles GET requests to the create page.
     * <p>
     * Renders user creation form.
     *
     * @return HTML response with rendered user creation form view
     * @throws SQLException if a database access error occurs
     */
    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response handleCreateUserAction() throws SQLException {
        return Response.ok(
                adduser.render()
        ).build();
    }
}
