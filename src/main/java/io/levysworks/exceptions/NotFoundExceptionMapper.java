package io.levysworks.exceptions;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Inject
    Template pagenotfound;

    @Override
    public Response toResponse(WebApplicationException exception) {
        if (exception.getResponse().getStatus() == 404) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(pagenotfound.render())
                    .type("text/html")
                    .build();
        }

        return exception.getResponse();
    }
}
