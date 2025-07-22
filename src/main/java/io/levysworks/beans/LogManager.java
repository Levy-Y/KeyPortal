package io.levysworks.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Bean used for creating new logs
 */
@ApplicationScoped
public class LogManager {
    @Inject
    DatabaseManager dbManager;

    public LogManager() {}

    /**
     * Creates new log entries in the database using {@link DatabaseManager#addNewLog}
     * <br>
     * Generates the current timestamp for the log entry.
     * @param title Title of the log entry
     * @param message Message of the log entry
     * @throws SQLException if a database access error occurs.
     */
    public void log(String title, String message) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        dbManager.addNewLog(title, message, timestamp);
    }

}
