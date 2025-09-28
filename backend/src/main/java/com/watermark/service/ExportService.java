package com.watermark.service;

import com.watermark.model.ImageInfo;
import com.watermark.model.WatermarkConfig;
import com.watermark.util.PathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

/**
 * 导出服务
 * 处理图片的批量导出和保存
 * 
 * @author Watermark Team  
 * @version 1.0.0
 */
public class ExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    
    private static ExportService instance;
    private final ImageService imageService;
    private final WatermarkService watermarkService;
    
    private ExportService() {
        this.imageService = ImageService.getInstance();
        this.watermarkService = WatermarkService.getInstance();
    }
    
    public static synchronized ExportService getInstance() {
        if (instance == null) {
            instance = new ExportService();
        }
        return instance;
    }
    
    /**
     * 导出配置类
     */
    public static class ExportConfig {
        private String outputDirectory;
        private String fileNamePattern = "original"; // original, prefix, suffix
        private String customPrefix = "wm_";
        private String customSuffix = "_watermarked";
        private String outputFormat = "original"; // original, jpg, png
        private int jpegQuality = 90; // 0-100
        private boolean preventOverwrite = true;
        
        // Getters and setters
        public String getOutputDirectory() { return outputDirectory; }
        public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
        
        public String getFileNamePattern() { return fileNamePattern; }
        public void setFileNamePattern(String fileNamePattern) { this.fileNamePattern = fileNamePattern; }
        
        public String getCustomPrefix() { return customPrefix; }
        public void setCustomPrefix(String customPrefix) { this.customPrefix = customPrefix; }
        
        public String getCustomSuffix() { return customSuffix; }
        public void setCustomSuffix(String customSuffix) { this.customSuffix = customSuffix; }
        
        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        
        public int getJpegQuality() { return jpegQuality; }
        public void setJpegQuality(int jpegQuality) { 
            this.jpegQuality = Math.max(1, Math.min(100, jpegQuality)); 
        }
        
        public boolean isPreventOverwrite() { return preventOverwrite; }
        public void setPreventOverwrite(boolean preventOverwrite) { this.preventOverwrite = preventOverwrite; }
    }
    
    /**
     * 处理并导出单个图片
     */
    public String processAndExportImage(String imageId, WatermarkConfig watermarkConfig, 
                                       String exportConfigJson) throws Exception {
        
        ImageInfo imageInfo = imageService.getImageInfo(imageId);
        if (imageInfo == null) {
            throw new IllegalArgumentException("图片不存在: " + imageId);
        }
        
        // 解析导出配置
        ExportConfig exportConfig = parseExportConfig(exportConfigJson);
        
        // 验证输出目录
        if (!PathManager.isValidDirectoryPath(exportConfig.getOutputDirectory())) {
            throw new IllegalArgumentException("输出目录无效: " + exportConfig.getOutputDirectory());
        }
        
        // 生成输出文件路径
        String outputPath = generateOutputPath(imageInfo, exportConfig);
        
        // 检查文件是否已存在
        if (exportConfig.isPreventOverwrite() && new File(outputPath).exists()) {
            outputPath = generateUniqueOutputPath(outputPath);
        }
        
        // 处理并保存图片
        watermarkService.processAndSaveImage(imageId, watermarkConfig, outputPath);
        
        logger.info("图片导出完成: {} -> {}", imageInfo.getFileName(), outputPath);
        return outputPath;
    }
    
    /**
     * 解析导出配置JSON
     */
    private ExportConfig parseExportConfig(String configJson) throws Exception {
        // 这里应该使用Jackson解析JSON，为简化起见使用默认配置
        ExportConfig config = new ExportConfig();
        
        // 简单解析 - 实际应用中应该使用完整的JSON解析
        if (configJson != null && configJson.contains("outputDirectory")) {
            // 提取输出目录路径
            String outputDir = extractJsonValue(configJson, "outputDirectory");
            if (outputDir != null) {
                config.setOutputDirectory(outputDir);
            }
        }
        
        // 设置默认输出目录
        if (config.getOutputDirectory() == null) {
            config.setOutputDirectory(System.getProperty("user.home") + File.separator + "WatermarkOutput");
        }
        
        return config;
    }
    
    /**
     * 简单的JSON值提取（实际应用中应该使用Jackson）
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        int valueStart = json.indexOf("\"", startIndex);
        if (valueStart == -1) return null;
        
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) return null;
        
        return json.substring(valueStart + 1, valueEnd);
    }
    
    /**
     * 生成输出文件路径
     */
    private String generateOutputPath(ImageInfo imageInfo, ExportConfig config) {
        String originalName = imageInfo.getFileName();
        String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.'));
        String originalExt = PathManager.getFileExtension(originalName);
        
        // 确定输出文件名
        String outputFileName;
        switch (config.getFileNamePattern()) {
            case "prefix":
                outputFileName = config.getCustomPrefix() + originalName;
                break;
            case "suffix":
                outputFileName = nameWithoutExt + config.getCustomSuffix() + "." + originalExt;
                break;
            case "original":
            default:
                outputFileName = originalName;
                break;
        }
        
        // 确定输出格式
        String outputExt;
        switch (config.getOutputFormat()) {
            case "jpg":
                outputExt = "jpg";
                outputFileName = changeFileExtension(outputFileName, outputExt);
                break;
            case "png":
                outputExt = "png";
                outputFileName = changeFileExtension(outputFileName, outputExt);
                break;
            case "original":
            default:
                outputExt = originalExt;
                break;
        }
        
        return Paths.get(config.getOutputDirectory(), outputFileName).toString();
    }
    
    /**
     * 修改文件扩展名
     */
    private String changeFileExtension(String fileName, String newExt) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName + "." + newExt;
        }
        return fileName.substring(0, lastDot + 1) + newExt;
    }
    
    /**
     * 生成唯一的输出文件路径（避免覆盖）
     */
    private String generateUniqueOutputPath(String originalPath) {
        File originalFile = new File(originalPath);
        String parentDir = originalFile.getParent();
        String fileName = originalFile.getName();
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        String ext = PathManager.getFileExtension(fileName);
        
        int counter = 1;
        String uniquePath;
        
        do {
            String uniqueFileName = nameWithoutExt + "_(" + counter + ")." + ext;
            uniquePath = Paths.get(parentDir, uniqueFileName).toString();
            counter++;
        } while (new File(uniquePath).exists() && counter < 1000); // 最多尝试1000次
        
        return uniquePath;
    }
    
    /**
     * 验证导出配置
     */
    public boolean validateExportConfig(ExportConfig config) {
        if (config == null) {
            return false;
        }
        
        // 验证输出目录
        if (config.getOutputDirectory() == null || config.getOutputDirectory().trim().isEmpty()) {
            return false;
        }
        
        // 验证JPEG质量参数
        if (config.getJpegQuality() < 1 || config.getJpegQuality() > 100) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建输出目录（如果不存在）
     */
    public void ensureOutputDirectory(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new Exception("无法创建输出目录: " + directoryPath);
            }
            logger.info("创建输出目录: {}", directoryPath);
        }
    }
}