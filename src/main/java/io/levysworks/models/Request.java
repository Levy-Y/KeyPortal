package io.levysworks.models;

import java.time.Instant;

public record Request(int id, String user_uuid, String server, String key, Instant timestamp) {}
