package io.levysworks.endpoints.pages;

import io.levysworks.configs.AgentsConfig;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/")
public class RootPage {

    @Inject
    Template root;

    @Inject
    AgentsConfig agentsConfig;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String root() {

        List<String> agents = new ArrayList<>();

        agentsConfig.servers().forEach(server -> agents.add(server.name()));

        return root.data("servers", agents).render();
    }
}
