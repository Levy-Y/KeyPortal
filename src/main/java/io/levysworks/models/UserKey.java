package io.levysworks.models;

import java.sql.Timestamp;

public record UserKey(Timestamp created, String key_type, String fingerprint, String server) {}
