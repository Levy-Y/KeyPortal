package io.levysworks.exceptions;

import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Exception mapper for {@link RateLimitException}.
 * <p>
 * Converts a {@code RateLimitException} thrown by the application into an HTTP 429 Too Many Requests
 * response with a message.
 * </p>
 */
@Provider
public class RateLimitExceptionMapper implements ExceptionMapper<RateLimitException> {
    /**
     * Converts a {@link RateLimitException} into a {@link Response} with HTTP status 429 (Too Many Requests).
     *
     * @param exception the {@code RateLimitException} instance that was thrown
     * @return a {@code Response} with status 429 and a message indicating the rate limit was exceeded
     */
    @Override
    public Response toResponse(RateLimitException exception) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity("Rate limit exceeded, try again in 1 day(s).")
                .build();
    }
}