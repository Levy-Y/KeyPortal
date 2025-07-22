package io.levysworks.models;

import java.sql.Timestamp;

/**
 * Composite data record representing detailed user information,
 * associated keys, server details, and related log entries.
 *
 * @param initials User initials
 * @param username Username
 * @param uuid User's UUID
 * @param first_name User's first name
 * @param last_name User's last name
 * @param email User's email address
 * @param department User's department or organizational unit
 * @param notes Additional notes related to the user
 * @param key_count Number of keys associated with the user
 * @param servers Comma-separated list of servers related to the user
 * @param request_id Identifier for a specific key request
 * @param key_type Type of the OpenSSH key
 * @param key_uid Unique identifier for the key
 * @param server Server name related to the key or request
 * @param public_key Public SSH key string
 * @param fingerprint Fingerprint of the public key
 * @param accepted_by Admin who accepted the request
 * @param issued_date Timestamp when the key was issued
 * @param valid_until Expiration timestamp for the key
 * @param log_title Title or summary of the log entry
 * @param log_message Detailed log message
 * @param log_timestamp Timestamp of the log entry
 */
public record CompositeUserData(String initials, String username, String uuid, String first_name, String last_name,
                                String email, String department, String notes, Integer key_count, String servers,
                                Integer request_id, String key_type,  String key_uid, String server, String public_key, String fingerprint,
                                String accepted_by, Timestamp issued_date, Timestamp valid_until, String log_title, String log_message, Timestamp log_timestamp) {
}
