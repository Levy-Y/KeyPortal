package io.levysworks.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.sql.Timestamp;

@ApplicationScoped
public class LogManager {
    @Inject
    DatabaseManager dbManager;

    public LogManager() {}

    public void log(String title, String message) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        dbManager.addNewLog(title, message, timestamp);
    }

}
