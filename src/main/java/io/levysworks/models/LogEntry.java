package io.levysworks.models;

import java.sql.Timestamp;

public record LogEntry(String title, String message, Timestamp timestamp) {}
