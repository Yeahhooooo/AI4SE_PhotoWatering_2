package com.watermark.test;

import com.watermark.service.DatabaseService;
import com.watermark.util.PathManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SQLite Database Storage Test
 */
public class SQLiteTest {
    
    public static void main(String[] args) {
        System.out.println("======== SQLite Database Test ========");
        
        try {
            testPathSetup();
            testDatabaseConnection();
            testTableCreation();
            testDataInsertion();
            testDataQuery();
            testDataUpdate();
            testDataDeletion();
            
            System.out.println("======== All SQLite Tests Passed! ========");
        } catch (Exception e) {
            System.err.println("SQLite test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPathSetup() {
        System.out.println("\n--- Testing Database Path Setup ---");
        
        try {
            String databasePath = PathManager.getDatabasePath();
            System.out.println("Database path: " + databasePath);
            
            // Ensure parent directory exists
            File dbFile = new File(databasePath);
            File parentDir = dbFile.getParentFile();
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                System.out.println("Created parent directory: " + created);
            }
            
            // Initialize app directories
            PathManager.initializeAppDirectories();
            
            System.out.println("+ Database path setup successful");
        } catch (Exception e) {
            System.out.println("- Database path setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDatabaseConnection() {
        System.out.println("\n--- Testing Database Connection ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            dbService.initialize();
            
            Connection conn = dbService.getConnection();
            System.out.println("Connection valid: " + !conn.isClosed());
            
            System.out.println("+ Database connection successful");
        } catch (Exception e) {
            System.out.println("- Database connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testTableCreation() {
        System.out.println("\n--- Testing Table Creation ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            Connection conn = dbService.getConnection();
            
            // Check if tables exist
            String[] expectedTables = {"watermark_templates", "user_settings", "processing_history"};
            
            for (String tableName : expectedTables) {
                String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, tableName);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("Table exists: " + tableName);
                    } else {
                        throw new RuntimeException("Table not found: " + tableName);
                    }
                }
            }
            
            System.out.println("+ Table creation verification successful");
        } catch (Exception e) {
            System.out.println("- Table creation verification failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDataInsertion() {
        System.out.println("\n--- Testing Data Insertion ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            Connection conn = dbService.getConnection();
            
            // Insert test setting
            String insertSetting = "INSERT OR REPLACE INTO user_settings (key, value) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSetting)) {
                stmt.setString(1, "test_key");
                stmt.setString(2, "test_value");
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Settings insert rows affected: " + rowsAffected);
            }
            
            // Insert test template
            String insertTemplate = "INSERT OR REPLACE INTO watermark_templates (name, description, type, config_json) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTemplate)) {
                stmt.setString(1, "Test Template");
                stmt.setString(2, "Test Description");
                stmt.setString(3, "TEXT");
                stmt.setString(4, "{\"text\":\"Test Watermark\"}");
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Template insert rows affected: " + rowsAffected);
            }
            
            System.out.println("+ Data insertion successful");
        } catch (Exception e) {
            System.out.println("- Data insertion failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDataQuery() {
        System.out.println("\n--- Testing Data Query ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            Connection conn = dbService.getConnection();
            
            // Query settings
            String querySetting = "SELECT value FROM user_settings WHERE key = ?";
            try (PreparedStatement stmt = conn.prepareStatement(querySetting)) {
                stmt.setString(1, "test_key");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String value = rs.getString("value");
                    System.out.println("Retrieved setting value: " + value);
                    if (!"test_value".equals(value)) {
                        throw new RuntimeException("Setting value mismatch");
                    }
                } else {
                    throw new RuntimeException("Setting not found");
                }
            }
            
            // Query templates
            String queryTemplate = "SELECT * FROM watermark_templates WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryTemplate)) {
                stmt.setString(1, "Test Template");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    String config = rs.getString("config_json");
                    System.out.println("Retrieved template - Name: " + name + ", Type: " + type);
                    System.out.println("Template config: " + config);
                } else {
                    throw new RuntimeException("Template not found");
                }
            }
            
            System.out.println("+ Data query successful");
        } catch (Exception e) {
            System.out.println("- Data query failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDataUpdate() {
        System.out.println("\n--- Testing Data Update ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            Connection conn = dbService.getConnection();
            
            // Update setting
            String updateSetting = "UPDATE user_settings SET value = ? WHERE key = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSetting)) {
                stmt.setString(1, "updated_value");
                stmt.setString(2, "test_key");
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Setting update rows affected: " + rowsAffected);
            }
            
            // Verify update
            String querySetting = "SELECT value FROM user_settings WHERE key = ?";
            try (PreparedStatement stmt = conn.prepareStatement(querySetting)) {
                stmt.setString(1, "test_key");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String value = rs.getString("value");
                    System.out.println("Updated setting value: " + value);
                    if (!"updated_value".equals(value)) {
                        throw new RuntimeException("Setting update failed");
                    }
                }
            }
            
            System.out.println("+ Data update successful");
        } catch (Exception e) {
            System.out.println("- Data update failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private static void testDataDeletion() {
        System.out.println("\n--- Testing Data Deletion ---");
        
        try {
            DatabaseService dbService = DatabaseService.getInstance();
            Connection conn = dbService.getConnection();
            
            // Delete test data
            String deleteSetting = "DELETE FROM user_settings WHERE key = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSetting)) {
                stmt.setString(1, "test_key");
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Setting deletion rows affected: " + rowsAffected);
            }
            
            String deleteTemplate = "DELETE FROM watermark_templates WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTemplate)) {
                stmt.setString(1, "Test Template");
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Template deletion rows affected: " + rowsAffected);
            }
            
            System.out.println("+ Data deletion successful");
        } catch (Exception e) {
            System.out.println("- Data deletion failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}