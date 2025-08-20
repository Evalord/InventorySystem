package com.inventory.dao;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String URL;
    private static String USER;
    private static String PASS;

    // Static initializer block to load properties once when the class is loaded
    static {
        Properties props = new Properties();
        try {
            Path configFilePath = Paths.get("config.properties");

            if (!Files.exists(configFilePath)) {
                throw new RuntimeException("config.properties not found at: " + configFilePath.toAbsolutePath());
            }

            try (InputStream input = Files.newInputStream(configFilePath)) {
                props.load(input);
            }

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASS = props.getProperty("db.password");

            if (URL == null || USER == null || PASS == null ||
                    URL.trim().isEmpty() || USER.trim().isEmpty() || PASS.trim().isEmpty()) {
                throw new RuntimeException("Database connection properties (db.url, db.user, db.password) are missing or empty in config.properties.");
            }

            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            System.out.println("DBConnection: Configuration loaded successfully from " + configFilePath.toAbsolutePath());
            System.out.println("DBConnection: Connecting to: " + URL);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Please add PostgreSQL JDBC driver to your classpath.", e);
        } catch (Exception e) {
            System.err.println("DBConnection: Error loading database configuration: " + e.getMessage());
            throw new RuntimeException("Failed to load database configuration.", e);
        }
    }

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("DBConnection: Successfully connected to database");
        return conn;
    }

    /**
     * Test the database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("DBConnection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get connection info (for debugging)
     */
    public static String getConnectionInfo() {
        return "URL: " + URL + ", User: " + USER;
    }
}