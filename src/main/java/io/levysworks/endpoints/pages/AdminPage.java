package io.levysworks.endpoints.pages;

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
import java.util.ArrayList;
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

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response admin(@QueryParam("query_user") Optional<String> query_user) throws SQLException {
        if (query_user.isPresent()) {
            UserProfile user = dbManager.getUserByUUID(query_user.get());

            if (user != null) {
                List<UserKey> keys = dbManager.getUserKeysByUUID(query_user.get());

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

            List<RequestFullTemplate> rft = dbManager.getRequestsForTemplate();
            List<LogEntry> le = dbManager.getLogs(20);
            List<KeysFullTemplate> kft = dbManager.getKeysForTemplate();
            List<UsersFullTemplate> uft = dbManager.getUsersForTemplate();

            return Response.ok(
                    admin
                            .data("activeKeyCount", activeKeys)
                            .data("pendingRequestCount", pendingRequestCount)
                            .data("userCount", userCount)
                            .data("serverCount", serverCount)
                            .data("requests", rft)
                            .data("logs", le)
                            .data("keys", kft)
                            .data("users", uft)
                            .render()
            ).build();
        }

    }
}
