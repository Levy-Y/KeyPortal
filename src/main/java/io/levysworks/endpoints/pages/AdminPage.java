package io.levysworks.endpoints.pages;

import io.levysworks.beans.HealthManager;
import io.levysworks.configs.AgentsConfig;
import io.levysworks.beans.DatabaseManager;
import io.levysworks.models.*;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Path("/management/admin")
public class AdminPage {
    @Inject
    Template admin;

    @Inject
    Template profile;

    @Inject
    Template notfound;

    @Inject
    AgentsConfig agentsConfig;

    @Inject
    DatabaseManager dbManager;

    @Inject
    HealthManager healthManager;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response admin(@QueryParam("query_user") Optional<String> query_user) throws SQLException {
        if (query_user.isPresent()) {
            CompositeUserData user = dbManager.getUserByUUID(query_user.get());

            if (user != null) {
                List<CompositeUserData> keys = dbManager.getUserKeysByUUID(query_user.get());

                return Response.ok(
                        profile
                                .data("user", user)
                                .data("keys", keys)
                                .render()
                ).build();
            } else {
                return Response.ok(
                        notfound
                                .data("uuid", query_user.get())
                                .render()
                ).build();
            }


        } else {
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

    }
}
