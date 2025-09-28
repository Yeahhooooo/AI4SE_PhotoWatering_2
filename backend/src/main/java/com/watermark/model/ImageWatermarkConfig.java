package com.watermark.model;

/**
 * 图片水印配置
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class ImageWatermarkConfig extends WatermarkConfig {
    
    private String imagePath;              // 水印图片路径
    private int width = 100;               // 水印宽度
    private int height = 100;              // 水印高度
    private boolean maintainAspectRatio = true; // 保持宽高比
    
    public ImageWatermarkConfig() {
        super(WatermarkType.IMAGE);
    }
    
    public ImageWatermarkConfig(String imagePath) {
        super(WatermarkType.IMAGE);
        this.imagePath = imagePath;
    }
    
    @Override
    public WatermarkConfig copy() {
        ImageWatermarkConfig copy = new ImageWatermarkConfig();
        
        // 复制基类属性
        copy.setPosition(this.getPosition());
        copy.setOffsetX(this.getOffsetX());
        copy.setOffsetY(this.getOffsetY());
        copy.setOpacity(this.getOpacity());
        copy.setRotation(this.getRotation());
        copy.setScale(this.getScale());
        
        // 复制图片水印属性
        copy.imagePath = this.imagePath;
        copy.width = this.width;
        copy.height = this.height;
        copy.maintainAspectRatio = this.maintainAspectRatio;
        
        return copy;
    }
    
    /**
     * 获取缩放后的宽度
     */
    public int getScaledWidth() {
        return Math.round(width * getScale());
    }
    
    /**
     * 获取缩放后的高度
     */
    public int getScaledHeight() {
        return Math.round(height * getScale());
    }
    
    /**
     * 根据原始图片尺寸调整水印大小（保持比例）
     */
    public void adjustSize(int originalWidth, int originalHeight) {
        if (maintainAspectRatio && originalWidth > 0 && originalHeight > 0) {
            float aspectRatio = (float) originalWidth / originalHeight;
            
            if (aspectRatio > 1) { // 宽图
                height = Math.round(width / aspectRatio);
            } else { // 高图
                width = Math.round(height * aspectRatio);
            }
        }
    }
    
    // Getters and Setters
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { 
        this.width = Math.max(10, Math.min(2000, width)); 
    }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { 
        this.height = Math.max(10, Math.min(2000, height)); 
    }
    
    public boolean isMaintainAspectRatio() { return maintainAspectRatio; }
    public void setMaintainAspectRatio(boolean maintainAspectRatio) { 
        this.maintainAspectRatio = maintainAspectRatio; 
    }
    
    @Override
    public String toString() {
        return "ImageWatermarkConfig{" +
                "imagePath='" + imagePath + '\'' +
                ", size=" + width + "×" + height +
                ", position=" + getPosition() +
                ", opacity=" + getOpacity() +
                '}';
    }
}