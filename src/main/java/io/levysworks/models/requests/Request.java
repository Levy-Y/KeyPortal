package io.levysworks.models.requests;

import java.time.Instant;

/**
 * Represents a pending SSH key request.
 *
 * @param id Unique identifier of the key request
 * @param user_uuid UUID of the user who made the request
 * @param server Name of the target server
 * @param key Public SSH key associated with the request
 * @param timestamp Time when the key request was created
 */
public record Request(int id, String user_uuid, String server, String key, Instant timestamp) {}
