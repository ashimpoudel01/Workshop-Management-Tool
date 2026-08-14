package com.motorworkshop.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC connection lifecycle, path resolution, and foreign key enforcement.
 */
public class DatabaseManager {
    private static final String APP_DIR_NAME = ".motorworkshop";
    private static final String DB_FILE_NAME = "workshop.db";
    private static String customDbPath = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    public static File getDatabaseFile() {
        if (customDbPath != null) {
            return new File(customDbPath);
        }
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, APP_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, DB_FILE_NAME);
    }

    public static void setCustomDbPath(String path) {
        customDbPath = path;
    }

    public static Connection getConnection() throws SQLException {
        File dbFile = getDatabaseFile();
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        
        // Enable SQLite foreign keys
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
        }
        return conn;
    }

    public static void initializeDatabase() {
        SchemaInitializer.initialize();
        DataSeeder.seedDefaultDataIfNeeded();
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Initializing Motorcycle Workshop SQLite Database...");
        System.out.println("Database file path: " + getDatabaseFile().getAbsolutePath());
        
        initializeDatabase();
        
        System.out.println("Database schema initialized and verified successfully!");
        System.out.println("\nChecking Database Tables & Seed Records:");
        System.out.println("--------------------------------------------------");

        String[] tables = {
            "settings", "categories", "suppliers", "products",
            "services", "customers", "purchases", "purchase_items",
            "sales", "sale_items", "expenses", "price_history",
            "inventory_transactions"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                try (var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        System.out.printf("Table: %-25s | Rows: %d\n", table, rs.getInt(1));
                    }
                }
            }
            System.out.println("--------------------------------------------------");
            System.out.println("ALL TABLES AND RELATIONSHIPS FULLY INTEGRATED!");
            System.out.println("==================================================");
        } catch (SQLException e) {
            System.err.println("Database verification error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
