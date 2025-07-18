package io.levysworks.models;

import java.sql.Timestamp;

public record KeysFullTemplate(String initials, String username, String email, String fingerprint, String type, Timestamp expires_date, Timestamp issued_date) {}
