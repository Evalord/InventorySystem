package com.inventory.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

    private static HikariDataSource dataSource;

    // Static initializer block to set up HikariCP connection pool
    static {
        HikariConfig config = new HikariConfig();
        // Replace with your PostgreSQL connection details
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/inventory_db"); // Your database name
        config.setUsername("postgres"); // Your PostgreSQL username
        config.setPassword("0123"); // Your PostgreSQL password

        // Optional HikariCP properties for tuning
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);     // 10 minutes
        config.setMaxLifetime(1800000);    // 30 minutes
        config.setPoolName("InventoryHikariCP");

        try {
            dataSource = new HikariDataSource(config);
            System.out.println("DBConnection: HikariCP initialized for PostgreSQL.");
        } catch (Exception e) {
            System.err.println("DBConnection: Error initializing HikariCP: " + e.getMessage());
            // Re-throw as a RuntimeException to fail fast if DB connection cannot be established
            throw new RuntimeException("Failed to initialize database connection pool.", e);
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private DBConnection() {
        // Private constructor
    }

    /**
     * Provides a connection from the HikariCP connection pool.
     * Callers must close the connection when done (using try-with-resources).
     * @return A database connection.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized or has been closed.");
        }
        return dataSource.getConnection();
    }

    /**
     * Shuts down the HikariCP connection pool.
     * This should be called once when the application exits.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("DBConnection: HikariCP connection pool shut down.");
        }
    }
}