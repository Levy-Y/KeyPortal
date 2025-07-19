package io.levysworks.models;

import java.sql.Timestamp;

public record CompositeUserData(String initials, String username, String uuid, String first_name, String last_name,
                                String email, String department, String notes, Integer key_count, String servers,
                                Integer request_id, String key_type,  String key_uid, String server, String public_key, String fingerprint,
                                String accepted_by, Timestamp issued_date, Timestamp valid_until, String log_title, String log_message, Timestamp log_timestamp) {
}
