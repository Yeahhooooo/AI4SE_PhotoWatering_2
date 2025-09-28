package com.watermark.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 图片信息模型
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class ImageInfo {
    
    private String id;              // 唯一标识
    private String fileName;        // 文件名
    private String filePath;        // 完整文件路径
    private String thumbnailPath;   // 缩略图路径
    private long fileSize;          // 文件大小（字节）
    private int width;              // 图片宽度
    private int height;             // 图片高度
    private String format;          // 图片格式
    private boolean hasAlphaChannel; // 是否有透明通道
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime importTime; // 导入时间
    
    public ImageInfo() {
        this.importTime = LocalDateTime.now();
    }
    
    public ImageInfo(String fileName, String filePath) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.id = generateId();
    }
    
    private String generateId() {
        return "img_" + System.currentTimeMillis() + "_" + 
               Math.abs(filePath.hashCode());
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public boolean isHasAlphaChannel() { return hasAlphaChannel; }
    public void setHasAlphaChannel(boolean hasAlphaChannel) { this.hasAlphaChannel = hasAlphaChannel; }
    
    public LocalDateTime getImportTime() { return importTime; }
    public void setImportTime(LocalDateTime importTime) { this.importTime = importTime; }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取图片尺寸信息
     */
    public String getDimensions() {
        return width + " × " + height;
    }
    
    @Override
    public String toString() {
        return "ImageInfo{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", dimensions=" + getDimensions() +
                ", format='" + format + '\'' +
                ", fileSize=" + getFormattedFileSize() +
                '}';
    }
}