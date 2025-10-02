package com.watermark.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 水印配置模型
 * 支持文本水印和图片水印
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextWatermarkConfig.class, name = "TEXT"),
    @JsonSubTypes.Type(value = ImageWatermarkConfig.class, name = "IMAGE")
})
public abstract class WatermarkConfig {
    
    public enum WatermarkType {
        TEXT, IMAGE
    }
    
    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
        CUSTOM
    }
    
    private WatermarkType type;
    private Position position = Position.BOTTOM_RIGHT;
    private int offsetX = 20;        // X轴偏移量
    private int offsetY = 20;        // Y轴偏移量
    private float opacity = 0.7f;    // 透明度 (0.0-1.0)
    private float rotation = 0.0f;   // 旋转角度 (-180 to +180)
    private float scale = 1.0f;      // 缩放比例
    private String outputPath = "";  // 输出路径
    
    public WatermarkConfig(WatermarkType type) {
        this.type = type;
    }
    
    // Abstract method to be implemented by subclasses
    public abstract WatermarkConfig copy();
    
    // Getters and Setters
    public WatermarkType getType() { return type; }
    public void setType(WatermarkType type) { this.type = type; }
    
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    
    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int offsetX) { this.offsetX = offsetX; }
    
    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int offsetY) { this.offsetY = offsetY; }
    
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { 
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity)); 
    }
    
    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { 
        this.rotation = Math.max(-180.0f, Math.min(180.0f, rotation)); 
    }
    
    public float getScale() { return scale; }
    public void setScale(float scale) { 
        this.scale = Math.max(0.1f, Math.min(5.0f, scale)); 
    }
    
    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { 
        this.outputPath = outputPath != null ? outputPath : ""; 
    }
    
    /**
     * 计算水印位置坐标
     */
    public java.awt.Point calculatePosition(int imageWidth, int imageHeight, 
                                           int watermarkWidth, int watermarkHeight) {
        int x, y;
        
        switch (position) {
            case TOP_LEFT:
                x = offsetX;
                y = offsetY;
                break;
            case TOP_CENTER:
                x = (imageWidth - watermarkWidth) / 2 + offsetX;
                y = offsetY;
                break;
            case TOP_RIGHT:
                x = imageWidth - watermarkWidth - offsetX;
                y = offsetY;
                break;
            case MIDDLE_LEFT:
                x = offsetX;
                y = (imageHeight - watermarkHeight) / 2 + offsetY;
                break;
            case MIDDLE_CENTER:
                x = (imageWidth - watermarkWidth) / 2 + offsetX;
                y = (imageHeight - watermarkHeight) / 2 + offsetY;
                break;
            case MIDDLE_RIGHT:
                x = imageWidth - watermarkWidth - offsetX;
                y = (imageHeight - watermarkHeight) / 2 + offsetY;
                break;
            case BOTTOM_LEFT:
                x = offsetX;
                y = imageHeight - watermarkHeight - offsetY;
                break;
            case BOTTOM_CENTER:
                x = (imageWidth - watermarkWidth) / 2 + offsetX;
                y = imageHeight - watermarkHeight - offsetY;
                break;
            case BOTTOM_RIGHT:
                x = imageWidth - watermarkWidth - offsetX;
                y = imageHeight - watermarkHeight - offsetY;
                break;
            case CUSTOM:
            default:
                x = offsetX;
                y = offsetY;
                break;
        }
        
        // 确保水印不会超出图片边界
        x = Math.max(0, Math.min(x, imageWidth - watermarkWidth));
        y = Math.max(0, Math.min(y, imageHeight - watermarkHeight));
        
        return new java.awt.Point(x, y);
    }
}