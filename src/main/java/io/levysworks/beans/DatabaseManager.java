package io.levysworks.beans;

import javax.sql.DataSource;

import io.levysworks.models.*;
import io.levysworks.models.requests.Request;
import io.levysworks.utilities.KeyGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Database manager bean, used for connecting, and interacting with the database.
 * <p>
 * This class initializes a connection after all dependencies are injected
 */
@ApplicationScoped
public class DatabaseManager {
    @Inject
    DataSource dataSource;

    @Inject
    KeyGenerator.KeyHasher keyHasher;

    private final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private Connection conn;

    /**
     * Creates a new connection to the Postgres database
     * <p>
     * Automatically called after the dependency injection is complete.
     *
     * @throws SQLException if the target postgres database is down
     */
    @PostConstruct
    void init() throws SQLException {
        conn = dataSource.getConnection();
    }

    /**
     * Queries the number of users present in the {@code users} table.
     *
     * @return the number of users in the database
     * @throws SQLException if the {@code users} table doesn't exist or the connection is closed
     */
    public int getUserCount() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }

    /**
     * Queries the number of ssh keys present in the {@code ssh_keys} table.
     *
     * @return the number of ssh keys in the database
     * @throws SQLException if the {@code ssh_keys} table doesn't exist or the connection is closed
     */
    public int getKeyCount() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ssh_keys");
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }

    /**
     * Queries the number of requests present in the {@code requests} table.
     *
     * @return the number of requests in the database
     * @throws SQLException if the {@code requests} table doesn't exist or the connection is closed
     */
    public int getPendingRequestCount() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM requests");
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }

    /**
     * Queries a request by its id from the {@code requests} table
     *
     * @param requestId The id of the request in the {@code requests} table
     * @return a {@link Request} containing the queried data
     * @throws SQLException if the {@code requests} table doesn't exist or the connection is closed
     */
    public Request getPendingRequestById(int requestId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM requests WHERE id = ?")) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String user_uuid = rs.getString("user_uuid");
                String server = rs.getString("server");
                String key = rs.getString("public_key");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                return new Request(id, user_uuid, server, key, timestamp.toInstant());
            } else {
                return null;
            }
        }
    }

    /**
     * Queries a set of user and request information by joining the {@code requests} and {@code users} tables.
     * <p>
     * The result contains combined user-request data including {@code name}, {@code email}, {@code request ID}, {@code server}, {@code key type}, and {@code timestamp}.
     *
     * @return a {@link List} of {@link CompositeUserData} objects representing the joined data.
     * @throws SQLException if a database access error occurs or the query fails.
     */
    public List<CompositeUserData> getRequestsForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                        SELECT
                            u.first_name,
                            u.last_name,
                            u.email,
                            r.id,
                            r.user_uuid,
                            r.server,
                            r.key_type,
                            r.timestamp
                        FROM requests r
                        JOIN users u ON r.user_uuid = u.uuid;
                    """);
            {
                List<CompositeUserData> requests = new ArrayList<>();

                while (rs.next()) {

                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");

                    String email = rs.getString("email");
                    int id = rs.getInt("id");
                    String user_uuid = rs.getString("user_uuid");
                    String server = rs.getString("server");
                    String keyType = rs.getString("key_type");
                    Timestamp timestamp = rs.getTimestamp("timestamp");

                    String username  = first_name + " " + last_name;

                    String initialsString = getInitials(username);

                    requests.add(new CompositeUserDataBuilder()
                            .initials(initialsString)
                            .username(username)
                            .email(email)
                            .request_id(id)
                            .uuid(user_uuid)
                            .server(server)
                            .key_type(keyType)
                            .issued_date(timestamp)
                            .build());
                }

                return requests;
            }
        }
    }

    /**
     * Inserts a new row into the {@code requests} table with the provided parameters
     * @param user_uuid The UUID of the user request
     * @param server The server the user requested a key to
     * @param public_key The public key generated by the server
     * @throws SQLException if a database access error occurs or the query fails.
     */
    public void addPendingRequest(String user_uuid, String server, String public_key) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO requests (user_uuid, server, public_key, key_type, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """)) {

            String[] keyParts = public_key.trim().split(" ", 3);
            String key_type = keyParts[0].replace("ssh-", "");

            stmt.setString(1, user_uuid);
            stmt.setString(2, server);
            stmt.setString(3, public_key);
            stmt.setString(4, key_type);
            stmt.setTimestamp(5, timestamp);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a new row into the {@code ssh_keys} table with the provided parameters
     * <p>
     * Computes a fingerprint for the public key for the sql statement
     * @param user_uuid UUID of the owner of the key
     * @param server The server, the key will be on
     * @param public_key The public key to add
     * @throws SQLException if a database access error occurs or the query fails.
     * @throws NoSuchAlgorithmException if the {@link KeyGenerator.KeyHasher#generateFingerprint} fails to compute the fingerprint of the public key
     */
    public void addActiveKey(String user_uuid, String server, String public_key) throws SQLException, NoSuchAlgorithmException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LocalDateTime tempDateTime = timestamp.toLocalDateTime().plusMonths(3);
        Timestamp validUntil = Timestamp.valueOf(tempDateTime);

        String[] keyParts = public_key.trim().split(" ", 3);
        String key_type = keyParts[0].replace("ssh-", "");

        String fingerprintHash = keyHasher.generateFingerprint(keyParts[1]);

        String uid = keyParts[2];

//        TODO: Change from constant key type, and admin!
        String accepted_by = "admin1";

        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO ssh_keys (uid, user_uuid, key_type, server, public_key, fingerprint, accepted_by, issued_date, valid_until) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {

            stmt.setString(1, uid);
            stmt.setString(2, user_uuid);
            stmt.setString(3, key_type);
            stmt.setString(4, server);
            stmt.setString(5, public_key);
            stmt.setString(6, fingerprintHash);
            stmt.setString(7, accepted_by);
            stmt.setTimestamp(8, timestamp);
            stmt.setTimestamp(9, validUntil);

            stmt.executeUpdate();
        }
    }

    /**
     * Queries a set of user and SSH key information by joining the {@code ssh_keys} and {@code users} tables.
     * <p>
     * The result contains combined user–SSH key data including {@code name}, {@code email}, {@code fingerprint},
     * {@code key type}, {@code issued date}, {@code valid until}, {@code UID}, and {@code server}.
     *
     * @return a {@link List} of {@link CompositeUserData} objects representing the joined user and key data.
     * @throws SQLException if a database access error occurs or the query fails.
     */
    public List<CompositeUserData> getKeysForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                        SELECT
                            u.last_name,
                            u.first_name,
                            u.email,
                            r.uid,
                            r.server,
                            r.fingerprint,
                            r.key_type,
                            r.issued_date,
                            r.valid_until
                        FROM ssh_keys r
                        JOIN users u ON r.user_uuid = u.uuid;
                    """);
            {
                List<CompositeUserData> requests = new ArrayList<>();

                while (rs.next()) {

                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");

                    String email = rs.getString("email");
                    String fingerprint = rs.getString("fingerprint");
                    String key_type = rs.getString("key_type");
                    Timestamp valid_until = rs.getTimestamp("valid_until");

                    String uid = rs.getString("uid");
                    String server = rs.getString("server");

                    String username = first_name + " " + last_name;

                    String truncated = fingerprint.length() > 15 ? fingerprint.substring(0, 15) + "..." : fingerprint;
                    String initialsString = getInitials(username);

                    requests.add(new CompositeUserDataBuilder()
                            .initials(initialsString)
                            .username(username)
                            .email(email)
                            .fingerprint(truncated)
                            .key_type(key_type)
                            .issued_date(valid_until)
                            .valid_until(valid_until)
                            .key_uid(uid)
                            .server(server)
                            .build());
                }

                return requests;
            }
        }
    }

    /**
     * Retrieves user data along with SSH key metadata by joining {@code users} and {@code ssh_keys}.
     * <p>
     * Includes name, email, UUID, department, key count, and associated servers.
     *
     * @return a {@link List} of {@link CompositeUserData} with user and key summary.
     * @throws SQLException if a database access error occurs.
     */
    public List<CompositeUserData> getUsersForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                    SELECT
                        u.last_name,
                        u.first_name,
                        u.uuid,
                        u.email,
                        u.department,
                        COUNT(k.uid) AS key_count,
                        ARRAY_AGG(DISTINCT k.server ORDER BY k.server) AS servers
                    FROM
                        users u
                    LEFT JOIN
                        ssh_keys k ON k.user_uuid = u.uuid
                    GROUP BY
                        u.uuid, u.last_name, u.first_name, u.email, u.department;
                    """);
            {
                List<CompositeUserData> requests = new ArrayList<>();

                while (rs.next()) {
                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");

                    String uuid = rs.getString("uuid");

                    String department = rs.getString("department");
                    String email = rs.getString("email");
                    Integer key_count = rs.getInt("key_count");
                    Array array = rs.getArray("servers");
                    String[] servers = (String[]) array.getArray();

                    String username = first_name + " " + last_name;
                    String initialsString = getInitials(username);

                    servers = Arrays.stream(servers)
                            .filter(s -> s != null && !s.equalsIgnoreCase("null"))
                            .toArray(String[]::new);

                    String serverString = servers.length == 0 ? "None" : String.join(", ", servers);

                    requests.add(new CompositeUserDataBuilder()
                            .initials(initialsString)
                            .uuid(uuid)
                            .username(username)
                            .first_name(first_name)
                            .last_name(last_name)
                            .department(department)
                            .email(email)
                            .key_count(key_count)
                            .servers(serverString)
                            .build());
                }

                return requests;
            }
        }
    }

    /**
     * Queries the {@code users} table for the user with the specified UUID.
     * @param uuid The UUID of the queried user
     * @return a {@link CompositeUserData} object containing the {@code first name}, {@code last name}, {@code email}, {@code department} and admin {@code notes} about the user.
     * @throws SQLException if a database access error occurs.
     */
    public CompositeUserData getUserByUUID(String uuid) throws SQLException {
        String sql = """
            SELECT * FROM users WHERE uuid = ?;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");
                    String email = rs.getString("email");
                    String department = rs.getString("department");

                    String notes = rs.getString("notes");

                    String username = first_name + " " + last_name;
                    String initialsString = getInitials(username);

                    return new CompositeUserDataBuilder()
                            .initials(initialsString)
                            .username(username)
                            .uuid(uuid)
                            .first_name(first_name)
                            .last_name(last_name)
                            .email(email)
                            .department(department)
                            .notes(notes)
                            .build();
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Queries the {@code ssh_keys} table for all SSH keys associated with the specified user UUID.
     * @param uuid The UUID of the user
     * @return a {@link List} of {@link CompositeUserData} objects, each containing a separate SSH key's data
     * @throws SQLException if a database access error occurs.
     */
    public List<CompositeUserData> getUserKeysByUUID(String uuid) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM ssh_keys WHERE user_uuid = ?;
                """)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();

            List<CompositeUserData> keys = new ArrayList<>();

            while (rs.next()) {
                String server = rs.getString("server");
                String fingerprint = rs.getString("fingerprint");
                String key_type = rs.getString("key_type");
                String uid = rs.getString("uid");
                Timestamp issued_date = rs.getTimestamp("issued_date");

                String truncated = fingerprint.length() > 15 ? fingerprint.substring(0, 15) + "..." : fingerprint;

                keys.add(new CompositeUserDataBuilder()
                        .issued_date(issued_date)
                        .key_type(key_type)
                        .fingerprint(truncated)
                        .server(server)
                        .key_uid(uid)
                        .build());
            }

            return keys;
        }
    }

    /**
     * Removes the key entry from the {@code ssh_keys} table where {@code server} and {@code uid} both match
     * @param server The name of the server
     * @param uid The UID of the SSH Key
     * @throws SQLException if a database access error occurs.
     */
    public void removeKeyByUID(String server, String uid) throws SQLException {
        String sql = """
                DELETE FROM ssh_keys WHERE uid = ? AND server = ?;
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uid);
            stmt.setString(2, server);

            stmt.executeUpdate();
        }
    }

    /**
     * Updates the user with matching {@code uuid} in the {@code users} table
     * @param uuid The updatable user's UUID
     * @param first_name The firs name to update to
     * @param last_name The last name to update to
     * @param email The email to update to
     * @param department The department to update to
     * @param notes The notes to update to
     * @throws SQLException if a database access error occurs.
     */
    public void updateUser(String uuid, String first_name, String last_name, String email, String department, String notes) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE users
                SET first_name = ?, last_name = ?, email = ?, department = ?, notes = ?
                WHERE uuid = ?
            """)) {
            stmt.setString(1, first_name);
            stmt.setString(2, last_name);
            stmt.setString(3, email);
            stmt.setString(4, department);
            stmt.setString(5, notes);
            stmt.setString(6, uuid);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No user records updated");
            }
        }
    }

    /**
     * Inserts a new row into the {@code audit_log} table with the provided parameters
     * @param title Title of the log
     * @param message Message of the log
     * @param timestamp Timestamp of the log
     * @throws SQLException if a database access error occurs.
     */
    public void addNewLog(String title, String message, Timestamp timestamp) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO audit_log (title, message, timestamp)  VALUES (?, ?, ?)
                """)) {
            stmt.setString(1, title);
            stmt.setString(2, message);
            stmt.setTimestamp(3, timestamp);

            stmt.executeUpdate();
        }
    }

    /**
     * Queries the {@code audit_log} table for the latest logs ordered descending by the {@code timestamp} column,
     * limited to the specified amount.
     * @param limit The amount of logs to return
     * @return a {@link List} of {@link CompositeUserData} objects representing the log entries
     * @throws SQLException if a database access error occurs
     */
    public List<CompositeUserData> getLogs(int limit) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ?")) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            List<CompositeUserData> entries = new ArrayList<>();

            while (rs.next()) {
                String title = rs.getString("title");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                entries.add(new CompositeUserDataBuilder()
                        .log_title(title)
                        .log_message(message)
                        .log_timestamp(timestamp)
                        .build());
            }

            return entries;
        }
    }

    /**
     * Checks whether an SSH Key exists in the {@code ssh_keys} table with the specified UID
     * @param uid The UID to check
     * @return a {@code boolean} representing whether the key exists or not
     * @throws SQLException if a database access error occurs
     */
    public boolean checkKeyExists(String uid) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM ssh_keys WHERE uid = ?"
        )) {
            stmt.setString(1, uid);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        }
    }

    /**
     * Removes a request from the {@code requests} table by its ID
     * @param id The ID of the removable request
     * @throws SQLException if a database access error occurs
     */
    public void removePendingRequest(int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM requests WHERE id = ?
                """)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a new user into the {@code users} table with the specified parameters
     * @param first_name The first name of the user
     * @param last_name The last name of the user
     * @param email The email of the user
     * @param department The department the user is in
     * @param notes The admin notes about this user
     * @throws SQLException if a database access error occurs
     */
    public void addUser(String first_name, String last_name, String email, String department, String notes) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO users (uuid, first_name, last_name, email, department, notes)  VALUES (?, ?, ?, ?, ?, ?)
                """)) {

            String uuid = UUID.randomUUID().toString();

            stmt.setString(1, uuid);
            stmt.setString(2, first_name);
            stmt.setString(3, last_name);
            stmt.setString(4, email);
            stmt.setString(5, department);
            stmt.setString(6, notes);

            stmt.executeUpdate();
        }
    }

    /**
     * Removes a user from the {@code users} table by their UUID
     * @param uuid The UUID of the user
     * @throws SQLException if a database access error occurs
     */
    public void removeUser(String uuid) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("User not found or already deleted. UUID: " + uuid);
            }
        }
    }

    /**
     * Returns the uppercase initials from a given username.
     * <p>
     * Example: {@code "John Doe"} → {@code "JD"}
     *
     * @param username the full name of the user
     * @return a {@link String} containing the initials in uppercase
     */
    private String getInitials(String username) {
        String[] parts = username.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return initials.toString();
    }
}
