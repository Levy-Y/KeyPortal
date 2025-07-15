package io.levysworks.endpoints.pages;

import io.levysworks.configs.AgentsConfig;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/management/admin")
public class AdminPage {
    @Inject
    Template admin;

    @Inject
    AgentsConfig agentsConfig;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response admin() {
        int activeKeys = 10;
        int pendingRequestCount = 2;
        int userCount = 20;

        return Response.ok(admin.data("activeKeyCount", activeKeys).data("pendingRequestCount", pendingRequestCount).data("userCount", userCount).data("serverCount", agentsConfig.servers().size()).render()).build();
    }
}
