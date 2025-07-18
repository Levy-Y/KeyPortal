package io.levysworks.beans;

import javax.sql.DataSource;

import io.levysworks.models.*;
import io.levysworks.utilities.KeyGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseManager {
    @Inject
    DataSource dataSource;

    @Inject
    KeyGenerator.KeyHasher keyHasher;

    private final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private Connection conn;

    @PostConstruct
    void init() throws SQLException {
        conn = dataSource.getConnection();
    }

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

    public List<Request> getPendingRequests() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM requests");
            List<Request> requests = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String user_uuid = rs.getString("user_uuid");
                String server = rs.getString("server");
                String key = rs.getString("public_key");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                requests.add(new Request(id, user_uuid, server, key, timestamp.toInstant()));
            }

            return requests;
        }
    }

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

    public List<RequestFullTemplate> getRequestsForTemplate() throws SQLException {
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
                List<RequestFullTemplate> requests = new ArrayList<>();

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

                    requests.add(new RequestFullTemplate(initialsString, username, email, id, user_uuid, server, keyType, timestamp));
                }

                return requests;
            }
        }
    }

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

    public void addActiveKey(String user_uuid, String server, String public_key) throws SQLException, NoSuchAlgorithmException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LocalDateTime tempDateTime = timestamp.toLocalDateTime().plusMonths(3);
        Timestamp validUntil = Timestamp.valueOf(tempDateTime);

        String[] keyParts = public_key.trim().split(" ", 3);
        String key_type = keyParts[0].replace("ssh-", "");

        String fingerprintHash = keyHasher.generateFingerprint(keyParts[1]);

//        TODO: Change from constant key type, and admin!
        String accepted_by = "admin1";

        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO ssh_keys (user_uuid, key_type, server, public_key, fingerprint, accepted_by, issued_date, valid_until) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {

            stmt.setString(1, user_uuid);
            stmt.setString(2, key_type);
            stmt.setString(3, server);
            stmt.setString(4, public_key);
            stmt.setString(5, fingerprintHash);
            stmt.setString(6, accepted_by);
            stmt.setTimestamp(7, timestamp);
            stmt.setTimestamp(8, validUntil);

            stmt.executeUpdate();
        }
    }

    public List<KeysFullTemplate> getKeysForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                        SELECT
                            u.last_name,
                            u.first_name,
                            u.email,
                            r.id,
                            r.fingerprint,
                            r.key_type,
                            r.issued_date,
                            r.valid_until
                        FROM ssh_keys r
                        JOIN users u ON r.user_uuid = u.uuid;
                    """);
            {
                List<KeysFullTemplate> requests = new ArrayList<>();

                while (rs.next()) {

                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");

                    String email = rs.getString("email");
                    String fingerprint = rs.getString("fingerprint");
                    String key_type = rs.getString("key_type");
                    Timestamp issued_date = rs.getTimestamp("issued_date");
                    Timestamp valid_until = rs.getTimestamp("valid_until");

                    String username = first_name + " " + last_name;

                    String truncated = fingerprint.length() > 15 ? fingerprint.substring(0, 15) + "..." : fingerprint;
                    String initialsString = getInitials(username);

                    requests.add(new KeysFullTemplate(initialsString, username, email, truncated, key_type, issued_date, valid_until));
                }

                return requests;
            }
        }
    }

    public List<UsersFullTemplate> getUsersForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                    SELECT
                        u.last_name,
                        u.first_name,
                        u.uuid,
                        u.email,
                        u.department,
                        COUNT(k.id) AS key_count,
                        ARRAY_AGG(DISTINCT k.server ORDER BY k.server) AS servers
                    FROM
                        users u
                    LEFT JOIN
                        ssh_keys k ON k.user_uuid = u.uuid
                    GROUP BY
                        u.uuid, u.last_name, u.first_name, u.email, u.department;
                    """);
            {
                List<UsersFullTemplate> requests = new ArrayList<>();

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

                    requests.add(new UsersFullTemplate(initialsString, uuid, username, first_name, last_name, department, email, key_count, serverString));
                }

                return requests;
            }
        }
    }

    public UserProfile getUserByUUID(String uuid) throws SQLException {
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

                    return new UserProfile(initialsString, username, uuid, first_name, last_name, email, department, notes);
                } else {
                    return null;
                }
            }
        }
    }

    public List<UserKey> getUserKeysByUUID(String uuid) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM ssh_keys WHERE user_uuid = ?;
                """)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();

            List<UserKey> keys = new ArrayList<>();

            while (rs.next()) {
                String server = rs.getString("server");
                String fingerprint = rs.getString("fingerprint");
                String key_type = rs.getString("key_type");
                Timestamp issued_date = rs.getTimestamp("issued_date");

                String truncated = fingerprint.length() > 15 ? fingerprint.substring(0, 15) + "..." : fingerprint;

                keys.add(new UserKey(issued_date, key_type, truncated, server));
            }

            return keys;
        }
    }

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
            stmt.executeUpdate();
        }
    }

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

    public List<LogEntry> getLogs(int limit) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM audit_log LIMIT " + limit + ";");
            List<LogEntry> entries = new ArrayList<>();

            while (rs.next()) {
                String title = rs.getString("title");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                entries.add(new LogEntry(title, message, timestamp));
            }

            return entries;
        }
    }

    public void removePendingRequest(int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM requests WHERE id = ?
                """)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
