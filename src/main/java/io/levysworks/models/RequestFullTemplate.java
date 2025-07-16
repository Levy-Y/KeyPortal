package io.levysworks.models;

import java.sql.Timestamp;

public record RequestFullTemplate(String initials, String username, String email, Integer id, Integer user_id, String server, Timestamp timestamp) {
}
