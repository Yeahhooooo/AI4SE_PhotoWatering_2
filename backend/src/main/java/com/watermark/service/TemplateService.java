package com.watermark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.watermark.model.TextWatermarkConfig;
import com.watermark.model.WatermarkTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板服务
 * 管理水印模板的CRUD操作
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class TemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);
    
    private static TemplateService instance;
    private final DatabaseService databaseService;
    private final ObjectMapper objectMapper;
    
    private TemplateService() {
        this.databaseService = DatabaseService.getInstance();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // 注册JSR310模块支持LocalDateTime
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
    }
    
    public static synchronized TemplateService getInstance() {
        if (instance == null) {
            instance = new TemplateService();
        }
        return instance;
    }
    
    /**
     * 保存水印模板
     */
    public WatermarkTemplate saveTemplate(WatermarkTemplate template) throws Exception {
        String sql;
        boolean isUpdate = template.getId() != null;
        
        if (isUpdate) {
            sql = "UPDATE watermark_templates " +
                  "SET name = ?, description = ?, config_json = ?, updated_at = CURRENT_TIMESTAMP " +
                  "WHERE id = ?";
            template.updateTimestamp();
        } else {
            sql = "INSERT INTO watermark_templates (name, description, type, config_json) " +
                  "VALUES (?, ?, ?, ?)";
        }
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // 将配置对象序列化为JSON
            String configJson = objectMapper.writeValueAsString(template.getConfig());

            System.out.println("Serialized config JSON: " + configJson);
            
            stmt.setString(1, template.getName());
            stmt.setString(2, template.getDescription());
            
            if (isUpdate) {
                stmt.setString(3, configJson);
                stmt.setLong(4, template.getId());
            } else {
                stmt.setString(3, template.getConfig().getType().name());
                stmt.setString(4, configJson);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("保存模板失败，没有行被影响");
            }
            
            // 如果是新增，使用SQLite特有的方式获取生成的ID
            if (!isUpdate) {
                try (PreparedStatement lastIdStmt = conn.prepareStatement("SELECT last_insert_rowid()");
                     ResultSet rs = lastIdStmt.executeQuery()) {
                    if (rs.next()) {
                        template.setId(rs.getLong(1));
                    } else {
                        throw new SQLException("保存模板失败，无法获取生成的ID");
                    }
                }
            }
            
            logger.info("模板保存成功: {}", template);
            return template;
            
        } catch (Exception e) {
            logger.error("保存模板失败", e);
            throw e;
        }
    }
    
    /**
     * 根据ID获取模板
     */
    public WatermarkTemplate getTemplate(long id) throws Exception {
        String sql = "SELECT * FROM watermark_templates WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTemplate(rs);
                } else {
                    return null;
                }
            }
            
        } catch (Exception e) {
            logger.error("获取模板失败: id={}", id, e);
            throw e;
        }
    }
    
    /**
     * 获取所有模板
     */
    public List<WatermarkTemplate> getAllTemplates() throws Exception {
        String sql = "SELECT * FROM watermark_templates ORDER BY created_at DESC";
        List<WatermarkTemplate> templates = new ArrayList<>();
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }
            
            logger.debug("获取到 {} 个模板", templates.size());
            return templates;
            
        } catch (Exception e) {
            logger.error("获取模板列表失败", e);
            throw e;
        }
    }
    
    /**
     * 根据类型获取模板
     */
    public List<WatermarkTemplate> getTemplatesByType(String type) throws Exception {
        String sql = "SELECT * FROM watermark_templates WHERE type = ? ORDER BY created_at DESC";
        List<WatermarkTemplate> templates = new ArrayList<>();
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapResultSetToTemplate(rs));
                }
            }
            
            logger.debug("获取到 {} 个 {} 类型的模板", templates.size(), type);
            return templates;
            
        } catch (Exception e) {
            logger.error("根据类型获取模板失败: type={}", type, e);
            throw e;
        }
    }
    
    /**
     * 删除模板
     */
    public boolean deleteTemplate(long id) throws Exception {
        String sql = "DELETE FROM watermark_templates WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            boolean success = affectedRows > 0;
            if (success) {
                logger.info("模板删除成功: id={}", id);
            } else {
                logger.warn("模板删除失败，可能不存在: id={}", id);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("删除模板失败: id={}", id, e);
            throw e;
        }
    }
    
    /**
     * 复制模板
     */
    public WatermarkTemplate duplicateTemplate(long id) throws Exception {
        WatermarkTemplate original = getTemplate(id);
        if (original == null) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }
        
        WatermarkTemplate copy = original.copy();
        return saveTemplate(copy);
    }
    
    /**
     * 检查模板名称是否存在
     */
    public boolean isTemplateNameExists(String name, Long excludeId) throws Exception {
        String sql = "SELECT COUNT(*) FROM watermark_templates WHERE name = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            if (excludeId != null) {
                stmt.setLong(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (Exception e) {
            logger.error("检查模板名称失败: name={}", name, e);
            throw e;
        }
        
        return false;
    }
    
    /**
     * 将ResultSet映射为WatermarkTemplate对象
     */
    private WatermarkTemplate mapResultSetToTemplate(ResultSet rs) throws Exception {
        WatermarkTemplate template = new WatermarkTemplate();
        
        template.setId(rs.getLong("id"));
        template.setName(rs.getString("name"));
        template.setDescription(rs.getString("description"));
        
        // 解析配置JSON
        String configJson = rs.getString("config_json");
        String type = rs.getString("type");
        
        // 根据类型解析配置
        if ("TEXT".equals(type)) {
            template.setConfig(objectMapper.readValue(configJson, 
                com.watermark.model.TextWatermarkConfig.class));
        } else if ("IMAGE".equals(type)) {
            template.setConfig(objectMapper.readValue(configJson, 
                com.watermark.model.ImageWatermarkConfig.class));
        }
        
        // 解析时间字段
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            template.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            template.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return template;
    }
}