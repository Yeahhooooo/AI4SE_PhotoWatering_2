package com.watermark.service;

import com.watermark.util.PathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 数据库服务
 * 管理SQLite数据库连接和表结构
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private static DatabaseService instance;
    private Connection connection;
    private String dbPath; // 添加数据库路径字段
    
    private DatabaseService() {
        // 私有构造函数，单例模式
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    /**
     * 初始化数据库
     */
    public void initialize() throws SQLException {
        try {
            this.dbPath = PathManager.getDatabasePath(); // 设置实例字段
            String url = "jdbc:sqlite:" + dbPath;
            
            connection = DriverManager.getConnection(url);
            
            // 启用外键约束
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            
            // 创建表结构
            createTables();
            
            // 插入默认数据
            insertDefaultData();
            
            logger.info("数据库初始化完成: {}", dbPath);
            
        } catch (SQLException e) {
            logger.error("数据库初始化失败", e);
            throw e;
        }
    }
    
    /**
     * 创建数据库表结构
     */
    private void createTables() throws SQLException {
        // 创建水印模板表
        String createTemplatesTable = "CREATE TABLE IF NOT EXISTS watermark_templates (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "description TEXT," +
                "type TEXT NOT NULL CHECK (type IN ('TEXT', 'IMAGE'))," +
                "config_json TEXT NOT NULL," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        // 创建用户设置表
        String createSettingsTable = "CREATE TABLE IF NOT EXISTS user_settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT NOT NULL," +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        // 创建处理历史表
        String createHistoryTable = "CREATE TABLE IF NOT EXISTS processing_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "source_path TEXT NOT NULL," +
                "output_path TEXT NOT NULL," +
                "template_id INTEGER," +
                "processing_time_ms INTEGER DEFAULT 0," +
                "processed_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (template_id) REFERENCES watermark_templates(id) ON DELETE SET NULL" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTemplatesTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createHistoryTable);
            
            // 创建索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_templates_type ON watermark_templates(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_processed_at ON processing_history(processed_at)");
            
            logger.info("数据库表结构创建完成");
        }
    }
    
    /**
     * 插入默认数据
     */
    private void insertDefaultData() throws SQLException {
        // 插入默认设置
        insertSettingIfNotExists("app_version", "1.0.0");
        insertSettingIfNotExists("last_output_dir", System.getProperty("user.home"));
        insertSettingIfNotExists("default_image_quality", "90");
        insertSettingIfNotExists("auto_cleanup_days", "7");
        
        // 创建默认文本水印模板
        String defaultTextTemplate = "{" +
                "\"type\": \"TEXT\"," +
                "\"position\": \"BOTTOM_RIGHT\"," +
                "\"offsetX\": 20," +
                "\"offsetY\": 20," +
                "\"opacity\": 0.7," +
                "\"rotation\": 0.0," +
                "\"scale\": 1.0," +
                "\"text\": \"水印\"," +
                "\"fontFamily\": \"Microsoft YaHei\"," +
                "\"fontSize\": 36," +
                "\"bold\": false," +
                "\"italic\": false," +
                "\"color\": {\"red\": 255, \"green\": 255, \"blue\": 255, \"alpha\": 255}," +
                "\"hasShadow\": true," +
                "\"shadowColor\": {\"red\": 0, \"green\": 0, \"blue\": 0, \"alpha\": 255}," +
                "\"shadowOffsetX\": 2," +
                "\"shadowOffsetY\": 2," +
                "\"shadowBlur\": 3," +
                "\"hasStroke\": false," +
                "\"strokeColor\": {\"red\": 0, \"green\": 0, \"blue\": 0, \"alpha\": 255}," +
                "\"strokeWidth\": 1" +
                "}";
        
        insertTemplateIfNotExists("默认文本水印", "简单的白色文本水印", "TEXT", defaultTextTemplate);
        
        logger.info("默认数据插入完成");
    }
    
    /**
     * 插入设置项（如果不存在）
     */
    private void insertSettingIfNotExists(String key, String value) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM user_settings WHERE key = ?";
        String insertSql = "INSERT INTO user_settings (key, value) VALUES (?, ?)";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, key);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, key);
                        insertStmt.setString(2, value);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * 插入模板（如果不存在）
     */
    private void insertTemplateIfNotExists(String name, String description, String type, String configJson) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM watermark_templates WHERE name = ?";
        String insertSql = "INSERT INTO watermark_templates (name, description, type, config_json) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, name);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, description);
                        insertStmt.setString(3, type);
                        insertStmt.setString(4, configJson);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    /**
     * 获取数据库连接
     * 每次调用都创建新的连接，以避免连接关闭的问题
     */
    public Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SELECT 1");
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("数据库连接测试失败", e);
        }
        return false;
    }
    
    /**
     * 执行数据库备份
     */
    public void backup(String backupPath) throws SQLException {
        String sql = "VACUUM INTO '" + backupPath + "'";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("数据库备份完成: {}", backupPath);
        } catch (SQLException e) {
            logger.error("数据库备份失败", e);
            throw e;
        }
    }
    
    /**
     * 优化数据库
     */
    public void optimize() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("VACUUM");
            stmt.execute("ANALYZE");
            logger.info("数据库优化完成");
        } catch (SQLException e) {
            logger.error("数据库优化失败", e);
            throw e;
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            logger.error("关闭数据库连接失败", e);
        }
    }
}