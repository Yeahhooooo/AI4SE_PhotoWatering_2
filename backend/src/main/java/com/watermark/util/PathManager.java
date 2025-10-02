package com.watermark.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 路径管理工具类
 * 管理应用程序的数据目录和文件路径
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class PathManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PathManager.class);
    
    private static final String APP_NAME = "WatermarkApp";
    
    // 主要目录路径
    private static final String APP_DATA_DIR;
    private static final String THUMBNAILS_DIR;
    private static final String TEMP_DIR;
    private static final String LOGS_DIR;
    private static final String DATABASE_PATH;
    
    static {
        // 初始化路径
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            // Windows: %APPDATA%\WatermarkApp
            String appData = System.getenv("APPDATA");
            APP_DATA_DIR = appData != null ? appData + File.separator + APP_NAME :
                          userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + APP_NAME;
        } else if (osName.contains("mac")) {
            // macOS: ~/Library/Application Support/WatermarkApp
            APP_DATA_DIR = userHome + File.separator + "Library" + File.separator + 
                          "Application Support" + File.separator + APP_NAME;
        } else {
            // Linux: ~/.watermarkapp
            APP_DATA_DIR = userHome + File.separator + ".watermarkapp";
        }
        
        THUMBNAILS_DIR = APP_DATA_DIR + File.separator + "thumbnails";
        TEMP_DIR = APP_DATA_DIR + File.separator + "temp";
        LOGS_DIR = APP_DATA_DIR + File.separator + "logs";
        DATABASE_PATH = APP_DATA_DIR + File.separator + "watermark_app.db";
        System.out.println("Database Path: " + DATABASE_PATH);
    }
    
    /**
     * 初始化应用程序目录结构
     */
    public static void initializeAppDirectories() throws Exception {
        try {
            createDirectoryIfNotExists(APP_DATA_DIR);
            createDirectoryIfNotExists(THUMBNAILS_DIR);
            createDirectoryIfNotExists(TEMP_DIR);
            createDirectoryIfNotExists(LOGS_DIR);
            
            logger.info("应用程序目录初始化完成: {}", APP_DATA_DIR);
        } catch (Exception e) {
            logger.error("初始化应用程序目录失败", e);
            throw e;
        }
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private static void createDirectoryIfNotExists(String dirPath) throws Exception {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.debug("创建目录: {}", dirPath);
        }
    }
    
    /**
     * 清理临时文件
     */
    public static void cleanupTempFiles() {
        try {
            Path tempPath = Paths.get(TEMP_DIR);
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                     .filter(Files::isRegularFile)
                     .forEach(file -> {
                         try {
                             Files.delete(file);
                         } catch (Exception e) {
                             logger.warn("删除临时文件失败: {}", file, e);
                         }
                     });
                logger.info("临时文件清理完成");
            }
        } catch (Exception e) {
            logger.error("清理临时文件失败", e);
        }
    }
    
    /**
     * 清理过期的缩略图
     */
    public static void cleanupOldThumbnails(int maxDays) {
        try {
            Path thumbnailPath = Paths.get(THUMBNAILS_DIR);
            if (Files.exists(thumbnailPath)) {
                long cutoffTime = System.currentTimeMillis() - (maxDays * 24L * 60 * 60 * 1000);
                
                Files.walk(thumbnailPath)
                     .filter(Files::isRegularFile)
                     .filter(file -> {
                         try {
                             return Files.getLastModifiedTime(file).toMillis() < cutoffTime;
                         } catch (Exception e) {
                             return false;
                         }
                     })
                     .forEach(file -> {
                         try {
                             Files.delete(file);
                         } catch (Exception e) {
                             logger.warn("删除过期缩略图失败: {}", file, e);
                         }
                     });
                
                logger.info("过期缩略图清理完成");
            }
        } catch (Exception e) {
            logger.error("清理过期缩略图失败", e);
        }
    }
    
    /**
     * 生成缩略图文件路径
     */
    public static String generateThumbnailPath(String originalFilePath) {
        String fileName = Paths.get(originalFilePath).getFileName().toString();
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        return THUMBNAILS_DIR + File.separator + "thumb_" + nameWithoutExt + ".jpg";
    }
    
    /**
     * 生成临时文件路径
     */
    public static String generateTempFilePath(String prefix, String extension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = prefix + "_" + timestamp + "." + extension;
        return TEMP_DIR + File.separator + fileName;
    }
    
    /**
     * 验证文件路径是否有效
     */
    public static boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isRegularFile(path);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证目录路径是否有效
     */
    public static boolean isValidDirectoryPath(String dirPath) {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(dirPath);
            return Files.exists(path) && Files.isDirectory(path);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * 检查是否为支持的图片格式
     */
    public static boolean isSupportedImageFormat(String fileName) {
        String ext = getFileExtension(fileName);
        return ext.equals("jpg") || ext.equals("jpeg") || 
               ext.equals("png") || ext.equals("bmp") || 
               ext.equals("tiff") || ext.equals("tif");
    }
    
    // Getters for directory paths
    public static String getAppDataDir() { return APP_DATA_DIR; }
    public static String getThumbnailsDir() { return THUMBNAILS_DIR; }
    public static String getTempDir() { return TEMP_DIR; }
    public static String getLogsDir() { return LOGS_DIR; }
    public static String getDatabasePath() { return DATABASE_PATH; }
}