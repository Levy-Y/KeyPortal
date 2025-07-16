package io.levysworks.endpoints.pages;

import io.levysworks.configs.AgentsConfig;
import io.levysworks.beans.DatabaseManager;
import io.levysworks.models.LogEntry;
import io.levysworks.models.RequestFullTemplate;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;

@Path("/management/admin")
public class AdminPage {
    @Inject
    Template admin;

    @Inject
    AgentsConfig agentsConfig;

    @Inject
    DatabaseManager dbManager;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response admin() throws SQLException {
        int activeKeys = dbManager.getKeyCount();
        int pendingRequestCount = dbManager.getPendingRequestCount();
        int userCount = dbManager.getUserCount();
        int serverCount = agentsConfig.servers().size();

        List<RequestFullTemplate> rft = dbManager.getRequestsForTemplate();
        List<LogEntry> le = dbManager.getLogs(20);

        return Response.ok(
                admin
                        .data("activeKeyCount", activeKeys)
                        .data("pendingRequestCount", pendingRequestCount)
                        .data("userCount", userCount)
                        .data("serverCount", serverCount)
                        .data("requests", rft)
                        .data("logs", le)
                        .render()
        ).build();
    }
}
