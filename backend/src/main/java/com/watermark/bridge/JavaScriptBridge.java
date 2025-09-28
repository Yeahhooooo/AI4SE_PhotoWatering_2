package com.watermark.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watermark.model.ImageInfo;
import com.watermark.model.WatermarkConfig;
import com.watermark.model.WatermarkTemplate;
import com.watermark.service.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * JavaScript-Java桥接器
 * 提供Vue前端调用Java后端服务的接口
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class JavaScriptBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptBridge.class);
    
    private final ObjectMapper objectMapper;
    private final ImageService imageService;
    private final WatermarkService watermarkService;
    private final TemplateService templateService;
    private final ExportService exportService;
    
    private Stage stage; // 用于显示文件对话框
    
    public JavaScriptBridge() {
        this.objectMapper = new ObjectMapper();
        this.imageService = ImageService.getInstance();
        this.watermarkService = WatermarkService.getInstance();
        this.templateService = TemplateService.getInstance();
        this.exportService = ExportService.getInstance();
        
        logger.info("JavaScript桥接器初始化完成");
    }
    
    /**
     * 设置Stage引用
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    // ==================== 文件操作相关 ====================
    
    /**
     * 显示文件选择对话框 - 选择图片文件
     */
    public String selectImageFiles() {
        try {
            if (stage == null) {
                logger.warn("Stage未设置，无法显示文件对话框");
                return null;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择图片文件");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有图片", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tiff"),
                new FileChooser.ExtensionFilter("JPEG文件", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG文件", "*.png"),
                new FileChooser.ExtensionFilter("BMP文件", "*.bmp"),
                new FileChooser.ExtensionFilter("TIFF文件", "*.tiff")
            );
            
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
            
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 处理选中的文件
                List<ImageInfo> imageInfos = imageService.processImageFiles(selectedFiles);
                return objectMapper.writeValueAsString(imageInfos);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("选择图片文件失败", e);
            return createErrorResponse("选择文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示目录选择对话框 - 选择输出目录
     */
    public String selectOutputDirectory() {
        try {
            if (stage == null) {
                logger.warn("Stage未设置，无法显示目录对话框");
                return null;
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择输出目录");
            
            File selectedDirectory = directoryChooser.showDialog(stage);
            
            if (selectedDirectory != null) {
                return selectedDirectory.getAbsolutePath();
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("选择输出目录失败", e);
            return null;
        }
    }
    
    /**
     * 批量导入图片文件夹
     */
    public String importImageFolder() {
        try {
            if (stage == null) {
                return createErrorResponse("Stage未设置");
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择图片文件夹");
            
            File selectedDirectory = directoryChooser.showDialog(stage);
            
            if (selectedDirectory != null) {
                List<ImageInfo> imageInfos = imageService.processImageFolder(selectedDirectory);
                return objectMapper.writeValueAsString(imageInfos);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("导入图片文件夹失败", e);
            return createErrorResponse("导入文件夹失败: " + e.getMessage());
        }
    }
    
    // ==================== 图片处理相关 ====================
    
    /**
     * 生成水印预览
     */
    public String generateWatermarkPreview(String imageId, String watermarkConfigJson) {
        try {
            WatermarkConfig config = objectMapper.readValue(watermarkConfigJson, WatermarkConfig.class);
            
            // 异步生成预览
            CompletableFuture<String> future = watermarkService.generatePreviewAsync(imageId, config);
            
            // 返回预览图片的base64编码或临时文件路径
            return future.get();
            
        } catch (Exception e) {
            logger.error("生成水印预览失败", e);
            return createErrorResponse("生成预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量处理图片
     */
    public void batchProcessImages(String imageIdsJson, String watermarkConfigJson, 
                                   String outputConfig, String progressCallback) {
        try {
            List<String> imageIds = objectMapper.readValue(imageIdsJson, List.class);
            WatermarkConfig watermarkConfig = objectMapper.readValue(watermarkConfigJson, WatermarkConfig.class);
            
            // 创建后台任务
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int total = imageIds.size();
                    
                    for (int i = 0; i < imageIds.size(); i++) {
                        String imageId = imageIds.get(i);
                        
                        // 处理单个图片
                        exportService.processAndExportImage(imageId, watermarkConfig, outputConfig);
                        
                        // 更新进度
                        int progress = (int) ((double) (i + 1) / total * 100);
                        updateProgress(progress, 100);
                        
                        // 通知前端进度更新
                        Platform.runLater(() -> {
                            callJavaScriptFunction(progressCallback, progress);
                        });
                    }
                    
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        callJavaScriptFunction(progressCallback, 100);
                    });
                }
                
                @Override
                protected void failed() {
                    logger.error("批量处理图片失败", getException());
                    Platform.runLater(() -> {
                        callJavaScriptFunction(progressCallback, -1);
                    });
                }
            };
            
            // 在后台线程执行任务
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
            
        } catch (Exception e) {
            logger.error("启动批量处理失败", e);
            callJavaScriptFunction(progressCallback, -1);
        }
    }
    
    // ==================== 模板管理相关 ====================
    
    /**
     * 保存水印模板
     */
    public String saveWatermarkTemplate(String templateJson) {
        try {
            WatermarkTemplate template = objectMapper.readValue(templateJson, WatermarkTemplate.class);
            WatermarkTemplate savedTemplate = templateService.saveTemplate(template);
            return objectMapper.writeValueAsString(savedTemplate);
        } catch (Exception e) {
            logger.error("保存水印模板失败", e);
            return createErrorResponse("保存模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有模板
     */
    public String getAllTemplates() {
        try {
            List<WatermarkTemplate> templates = templateService.getAllTemplates();
            return objectMapper.writeValueAsString(templates);
        } catch (Exception e) {
            logger.error("获取模板列表失败", e);
            return createErrorResponse("获取模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除模板
     */
    public String deleteTemplate(long templateId) {
        try {
            boolean success = templateService.deleteTemplate(templateId);
            return success ? "success" : "failed";
        } catch (Exception e) {
            logger.error("删除模板失败", e);
            return createErrorResponse("删除模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载模板
     */
    public String loadTemplate(long templateId) {
        try {
            WatermarkTemplate template = templateService.getTemplate(templateId);
            if (template != null) {
                return objectMapper.writeValueAsString(template);
            } else {
                return createErrorResponse("模板不存在");
            }
        } catch (Exception e) {
            logger.error("加载模板失败", e);
            return createErrorResponse("加载模板失败: " + e.getMessage());
        }
    }
    
    // ==================== 系统信息相关 ====================
    
    /**
     * 获取系统信息
     */
    public String getSystemInfo() {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "osVersion", System.getProperty("os.version"),
                "userHome", System.getProperty("user.home"),
                "appDataDir", com.watermark.util.PathManager.getAppDataDir()
            ));
        } catch (Exception e) {
            logger.error("获取系统信息失败", e);
            return createErrorResponse("获取系统信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 日志记录方法（供JavaScript调用）
     */
    public void log(String level, String message) {
        switch (level.toLowerCase()) {
            case "debug":
                logger.debug("JS: {}", message);
                break;
            case "info":
                logger.info("JS: {}", message);
                break;
            case "warn":
                logger.warn("JS: {}", message);
                break;
            case "error":
                logger.error("JS: {}", message);
                break;
            default:
                logger.info("JS: {}", message);
                break;
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建错误响应
     */
    private String createErrorResponse(String message) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                "error", true,
                "message", message
            ));
        } catch (Exception e) {
            return "{\"error\":true,\"message\":\"" + message + "\"}";
        }
    }
    
    /**
     * 调用JavaScript函数
     */
    private void callJavaScriptFunction(String functionName, Object... args) {
        try {
            // 构建JavaScript调用语句
            StringBuilder jsCall = new StringBuilder(functionName + "(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) jsCall.append(",");
                if (args[i] instanceof String) {
                    jsCall.append("'").append(args[i]).append("'");
                } else {
                    jsCall.append(args[i]);
                }
            }
            jsCall.append(");");
            
            // 执行JavaScript
            Platform.runLater(() -> {
                // 这里需要WebViewController的引用来执行JavaScript
                // 暂时使用日志记录
                logger.debug("调用JavaScript函数: {}", jsCall.toString());
            });
            
        } catch (Exception e) {
            logger.error("调用JavaScript函数失败: {}", functionName, e);
        }
    }
}