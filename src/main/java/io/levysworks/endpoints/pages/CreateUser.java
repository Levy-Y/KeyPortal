package io.levysworks.endpoints.pages;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

@Path("/management/admin")
public class CreateUser {
    @Inject
    Template adduser;

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public Response handleCreateUserAction() throws SQLException {
        return Response.ok(
                adduser.render()
        ).build();
    }
}
