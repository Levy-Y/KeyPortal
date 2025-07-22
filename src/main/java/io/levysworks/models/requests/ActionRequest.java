package io.levysworks.models.requests;

/**
 * Represents the request payload for an admin action.
 * <p>
 * Used by {@link io.levysworks.endpoints.AdminEndpoint#handleRequestAction} to specify the action to perform.
 *
 * @param action The action to execute
 */
public record ActionRequest(String action) {}
