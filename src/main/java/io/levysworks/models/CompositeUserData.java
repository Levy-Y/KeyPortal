package io.levysworks.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.sql.Timestamp;

/**
 * Composite data record representing detailed user information,
 * associated keys, server details, and related log entries.
 *
 * @param initials User initials
 * @param username Username
 * @param uuid User's UUID
 * @param firstName User's first name
 * @param lastName User's last name
 * @param email User's email address
 * @param department User's department or organizational unit
 * @param notes Additional notes related to the user
 * @param keyCount Number of keys associated with the user
 * @param servers Comma-separated list of servers related to the user
 * @param requestId Identifier for a specific key request
 * @param keyType Type of the OpenSSH key
 * @param keyUid Unique identifier for the key
 * @param server Server name related to the key or request
 * @param publicKey Public SSH key string
 * @param fingerprint Fingerprint of the public key
 * @param acceptedBy Admin who accepted the request
 * @param issuedDate Timestamp when the key was issued
 * @param validUntil Expiration timestamp for the key
 * @param logTitle Title or summary of the log entry
 * @param logMessage Detailed log message
 * @param logTimestamp Timestamp of the log entry
 */
@RegisterForReflection
public record CompositeUserData(String initials, String username, String uuid, String firstName, String lastName,
                                String email, String department, String notes, Integer keyCount, String servers,
                                Integer requestId, String keyType, String keyUid, String server, String publicKey, String fingerprint,
                                String acceptedBy, Timestamp issuedDate, Timestamp validUntil, String logTitle, String logMessage, Timestamp logTimestamp) {
}
