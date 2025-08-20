
package com.inventory.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL  = "jdbc:postgresql://localhost:5432/inventorydb"; // <-- your DB name
    private static final String USER = "postgres";   // <-- your username
    private static final String PASS = "ingrid"; // <-- your password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
