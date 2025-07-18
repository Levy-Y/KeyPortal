package io.levysworks.models;

import java.sql.Timestamp;

public record RequestFullTemplate(String initials, String username, String email, Integer id, String user_uuid, String server,  String keyType, Timestamp timestamp) {
}
