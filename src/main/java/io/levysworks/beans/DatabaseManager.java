package io.levysworks.beans;

import javax.sql.DataSource;

import io.levysworks.models.LogEntry;
import io.levysworks.models.Request;
import io.levysworks.models.RequestFullTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseManager {
    @Inject
    DataSource dataSource;

    private final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private Connection conn;

    @PostConstruct
    void init() throws SQLException {
        conn = dataSource.getConnection();
        ensureDatabase();
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
                int userId = rs.getInt("user_id");
                String server = rs.getString("server");
                String key = rs.getString("public_key");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                requests.add(new Request(id, userId, server, key, timestamp.toInstant()));
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
                int userId = rs.getInt("user_id");
                String server = rs.getString("server");
                String key = rs.getString("public_key");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                return new Request(id, userId, server, key, timestamp.toInstant());
            } else {
                return null;
            }
        }
    }

    public List<RequestFullTemplate> getRequestsForTemplate() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("""
                        SELECT
                            u.username,
                            u.email,
                            r.id,
                            r.user_id,
                            r.server,
                            r.timestamp
                        FROM requests r
                        JOIN users u ON r.user_id = u.id;
                    """);
            {
                List<RequestFullTemplate> requests = new ArrayList<>();

                while (rs.next()) {

                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    String server = rs.getString("server");
                    Timestamp timestamp = rs.getTimestamp("timestamp");

                    String[] parts = username.trim().split("\\s+");
                    StringBuilder initials = new StringBuilder();
                    for (String part : parts) {
                        if (!part.isEmpty()) {
                            initials.append(Character.toUpperCase(part.charAt(0)));
                        }
                    }
                    String initialsString = initials.toString();

                    requests.add(new RequestFullTemplate(initialsString, username, email, id, userId, server, timestamp));
                }

                return requests;
            }
        }
    }

    public void addPendingRequest(int user_id, String server, String public_key) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO requests (user_id, server, public_key, timestamp)
                VALUES (?, ?, ?, ?)
                """)) {
            stmt.setInt(1, user_id);
            stmt.setString(2, server);
            stmt.setString(3, public_key);
            stmt.setTimestamp(4, timestamp);
            stmt.executeUpdate();
        }
    }

    public void addActiveKey(int user_id, String server, String public_key) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LocalDateTime tempDateTime = timestamp.toLocalDateTime().plusMonths(3);
        Timestamp validUntil = Timestamp.valueOf(tempDateTime);

//        TODO: Change from constant key type, and admin!
        String key_type = "ed25519";
        String accepted_by = "admin1";

        try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO ssh_keys (user_id, key_type, server, public_key, accepted_by, issued_date, valid_until) VALUES (?, ?, ?, ?, ?, ?, ?)
                """)) {

            stmt.setInt(1, user_id);
            stmt.setString(2, key_type);
            stmt.setString(3, server);
            stmt.setString(4, public_key);
            stmt.setString(5, accepted_by);
            stmt.setTimestamp(6, timestamp);
            stmt.setTimestamp(7, validUntil);

            stmt.executeUpdate();
        }
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

    public void ensureDatabase() throws SQLException {
        String[] statements = {
                """
        CREATE TABLE IF NOT EXISTS "users" (
          "id" SERIAL PRIMARY KEY,
          "username" VARCHAR(255) UNIQUE NOT NULL,
          "email" VARCHAR(255) UNIQUE NOT NULL,
          "key_count" INTEGER DEFAULT 0
        )
        """,
                """
        CREATE TABLE IF NOT EXISTS "ssh_keys" (
          "id" SERIAL PRIMARY KEY,
          "user_id" INTEGER NOT NULL,
          "key_type" VARCHAR(50) NOT NULL,
          "server" TEXT NOT NULL,
          "public_key" TEXT NOT NULL,
          "accepted_by" VARCHAR(255) NOT NULL,
          "issued_date" TIMESTAMP DEFAULT now(),
          "valid_until" TIMESTAMP NOT NULL
        )
        """,
                """
        CREATE TABLE IF NOT EXISTS "audit_log" (
          "id" SERIAL PRIMARY KEY,
          "title" VARCHAR(255) NOT NULL,
          "message" TEXT NOT NULL,
          "timestamp" TIMESTAMP DEFAULT now()
        )
        """,
                """
        CREATE TABLE IF NOT EXISTS "requests" (
          "id" SERIAL PRIMARY KEY,
          "user_id" INTEGER NOT NULL,
          "server" TEXT NOT NULL,
          "public_key" TEXT NOT NULL,
          "timestamp" TIMESTAMP DEFAULT now()
        )
        """,
                """
        ALTER TABLE "ssh_keys"
        ADD CONSTRAINT fk_user FOREIGN KEY ("user_id") REFERENCES "users"("id")
        """,
                """
        ALTER TABLE "requests"
        ADD CONSTRAINT fk_user_request FOREIGN KEY ("user_id") REFERENCES "users"("id");
        """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    if (!e.getSQLState().equals("42710")) {
                        System.out.println("SQL state: " + e.getSQLState() + " - object already exists.");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Database setup failed: " + e.getMessage());
        }
    }
}
