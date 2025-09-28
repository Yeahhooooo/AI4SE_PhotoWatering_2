package com.watermark.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * SQLite Database Viewer Tool
 * 用于查看数据库内容的简单工具
 */
public class SQLiteViewer {
    
    public static void main(String[] args) {
        String dbPath = "C:\\Users\\18206\\AppData\\Roaming\\WatermarkApp\\watermark_app.db";
        
        System.out.println("======== SQLite Database Viewer ========");
        System.out.println("Database: " + dbPath);
        System.out.println();
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // 显示所有表
            showAllTables(conn);
            
            // 显示每个表的内容
            showTableContent(conn, "watermark_templates");
            showTableContent(conn, "user_settings");
            showTableContent(conn, "processing_history");
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showAllTables(Connection conn) throws Exception {
        System.out.println("=== Database Tables ===");
        String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            String tableName = rs.getString("name");
            System.out.println("Table: " + tableName);
        }
        System.out.println();
    }
    
    private static void showTableContent(Connection conn, String tableName) throws Exception {
        System.out.println("=== Content of table: " + tableName + " ===");
        
        // 首先获取表结构
        String schemaSql = "PRAGMA table_info(" + tableName + ");";
        Statement stmt = conn.createStatement();
        ResultSet schemaRs = stmt.executeQuery(schemaSql);
        
        System.out.println("Table Schema:");
        while (schemaRs.next()) {
            System.out.println("  " + schemaRs.getString("name") + 
                             " (" + schemaRs.getString("type") + ")" +
                             (schemaRs.getBoolean("pk") ? " PRIMARY KEY" : "") +
                             (schemaRs.getBoolean("notnull") ? " NOT NULL" : ""));
        }
        System.out.println();
        
        // 然后显示数据
        String dataSql = "SELECT * FROM " + tableName + ";";
        ResultSet dataRs = stmt.executeQuery(dataSql);
        ResultSetMetaData metaData = dataRs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // 显示列名
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(metaData.getColumnName(i));
            if (i < columnCount) System.out.print(" | ");
        }
        System.out.println();
        
        // 显示分隔线
        for (int i = 1; i <= columnCount; i++) {
            for (int j = 0; j < metaData.getColumnName(i).length(); j++) {
                System.out.print("-");
            }
            if (i < columnCount) System.out.print("-+-");
        }
        System.out.println();
        
        // 显示数据行
        int rowCount = 0;
        while (dataRs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String value = dataRs.getString(i);
                if (value == null) value = "NULL";
                // 限制长度，避免输出过长
                if (value.length() > 50) {
                    value = value.substring(0, 47) + "...";
                }
                System.out.print(value);
                if (i < columnCount) System.out.print(" | ");
            }
            System.out.println();
            rowCount++;
        }
        
        if (rowCount == 0) {
            System.out.println("(No data in this table)");
        }
        System.out.println("Total rows: " + rowCount);
        System.out.println();
    }
}