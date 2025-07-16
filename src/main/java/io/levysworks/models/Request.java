package io.levysworks.models;

import java.time.Instant;

public record Request(int id, int userId, String server, String key, Instant timestamp) {}
