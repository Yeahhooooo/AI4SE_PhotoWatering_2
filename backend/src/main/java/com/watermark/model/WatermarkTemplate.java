package com.watermark.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 水印模板模型
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WatermarkTemplate {
    
    private Long id;
    private String name;
    private String description;
    private WatermarkConfig config;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public WatermarkTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public WatermarkTemplate(String name, WatermarkConfig config) {
        this();
        this.name = name;
        this.config = config;
    }
    
    public WatermarkTemplate(String name, String description, WatermarkConfig config) {
        this(name, config);
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public WatermarkConfig getConfig() { return config; }
    public void setConfig(WatermarkConfig config) { this.config = config; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 创建模板副本
     */
    public WatermarkTemplate copy() {
        WatermarkTemplate copy = new WatermarkTemplate();
        copy.name = this.name + " (副本)";
        copy.description = this.description;
        copy.config = this.config.copy();
        return copy;
    }
    
    @Override
    public String toString() {
        return "WatermarkTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + (config != null ? config.getType() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}