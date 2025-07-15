package io.levysworks.endpoints;

import io.levysworks.beans.PollManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/poll")
public class KeyPollEndpoint {
    @Inject
    PollManager pollManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollKeys(@HeaderParam("X-Agent-Name") String name, @HeaderParam("X-Poll-Key") String key) {
        if (key == null || key.isEmpty() || name == null || name.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!pollManager.checkAgentKeyMatch(name, key)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<String> keys = pollManager.pollKeys(name);

        if (keys.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {

            return Response.ok(keys).build();
        }
    }
}
