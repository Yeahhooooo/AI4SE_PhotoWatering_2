package com.watermark.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        System.out.println("=== JavaScriptBridge 构造函数被调用 ===");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // 注册JSR310模块支持LocalDateTime
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
        this.imageService = ImageService.getInstance();
        this.watermarkService = WatermarkService.getInstance();
        this.templateService = TemplateService.getInstance();
        this.exportService = ExportService.getInstance();
        
        System.out.println("JavaScriptBridge 初始化完成");
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
     * 选择单个图片文件 (前端兼容性方法)
     */
    public String selectImage() {
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
            
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile != null) {
                String result = selectedFile.getAbsolutePath();
                logger.info("选择的图片文件: {}", result);
                return result;
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("选择图片文件失败", e);
            return null;
        }
    }
    
    /**
     * 选择输出目录 (前端兼容性方法)
     */
    public String selectDirectory() {
        return selectOutputDirectory();
    }
    
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
    
    /**
     * 列出指定目录中的所有图片文件（前端兼容性方法）
     */
    public String listImagesInDirectory(String directoryPath) {
        try {
            if (directoryPath == null || directoryPath.isEmpty()) {
                return createErrorResponse("目录路径不能为空");
            }
            
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                return createErrorResponse("无效的目录路径: " + directoryPath);
            }
            
            // 使用ImageService处理文件夹
            List<ImageInfo> imageInfos = imageService.processImageFolder(directory);
            
            // 返回图片路径列表给前端
            List<String> imagePaths = imageInfos.stream()
                .map(ImageInfo::getFilePath)
                .collect(java.util.stream.Collectors.toList());
            
            logger.info("在目录 {} 中找到 {} 张图片", directoryPath, imagePaths.size());
            return objectMapper.writeValueAsString(imagePaths);
            
        } catch (Exception e) {
            logger.error("列出目录图片失败: {}", directoryPath, e);
            return createErrorResponse("列出目录图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 选择多个图片文件（前端兼容性方法）
     */
    public String selectMultipleImages() {
        try {
            if (stage == null) {
                logger.warn("Stage未设置，无法显示文件对话框");
                return null;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择多个图片文件");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有图片", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tiff"),
                new FileChooser.ExtensionFilter("JPEG文件", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG文件", "*.png"),
                new FileChooser.ExtensionFilter("BMP文件", "*.bmp"),
                new FileChooser.ExtensionFilter("TIFF文件", "*.tiff")
            );
            
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
            
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 返回文件路径列表
                List<String> filePaths = selectedFiles.stream()
                    .map(File::getAbsolutePath)
                    .collect(java.util.stream.Collectors.toList());
                
                logger.info("选择了 {} 个图片文件", filePaths.size());
                return objectMapper.writeValueAsString(filePaths);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("选择多个图片文件失败", e);
            return createErrorResponse("选择文件失败: " + e.getMessage());
        }
    }
    
    // ==================== 图片处理相关 ====================
    
    /**
     * 处理单个图片并应用水印
     */
    public String processImage(String imagePath, String watermarkConfigJson) {
        try {
            logger.info("开始处理图片: {}", imagePath);
            logger.debug("水印配置: {}", watermarkConfigJson);
            
            // 使用WatermarkService处理图片（支持JSON配置）
            String outputPath = watermarkService.processImageWithWatermark(imagePath, watermarkConfigJson);
            
            logger.info("图片处理完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            logger.error("处理图片失败: " + e.getMessage(), e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }
    
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
        System.out.println("=== saveWatermarkTemplate 被调用 ===");
        System.out.println("输入参数: " + templateJson);
        logger.info("保存水印模板被调用，参数: {}", templateJson);
        
        try {
            // 首先解析JSON字符串来查看前端发送的数据
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(templateJson);
            // 查看颜色相关字段
            com.fasterxml.jackson.databind.JsonNode configNode = jsonNode.get("config");
            
            WatermarkTemplate template = objectMapper.readValue(templateJson, WatermarkTemplate.class);
            WatermarkTemplate savedTemplate = templateService.saveTemplate(template);
            
            String result = objectMapper.writeValueAsString(savedTemplate);
            System.out.println("保存结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("保存模板失败: " + e.getMessage());
            e.printStackTrace();
            logger.error("保存水印模板失败", e);
            return createErrorResponse("保存模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有模板
     */
    public String getAllTemplates() {
        try {
            System.out.println("获取所有模板被调用");
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
     * 复制模板
     */
    public String duplicateTemplate(long templateId) {
        try {
            WatermarkTemplate duplicatedTemplate = templateService.duplicateTemplate(templateId);
            if (duplicatedTemplate != null) {
                return objectMapper.writeValueAsString(duplicatedTemplate);
            } else {
                return createErrorResponse("模板复制失败");
            }
        } catch (Exception e) {
            logger.error("复制模板失败", e);
            return createErrorResponse("复制模板失败: " + e.getMessage());
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
    
    /**
     * 获取所有模板 (前端兼容方法)
     */
    public String getTemplates() {
        return getAllTemplates();
    }
    
    /**
     * 保存模板 (前端兼容方法)
     */
    public String saveTemplate(String templateJson) {
        System.out.println("Saving template: " + templateJson);
        return saveWatermarkTemplate(templateJson);
    }
    
    /**
     * 获取处理历史
     */
    public String getProcessHistory() {
        // 这里需要实现历史记录的获取逻辑
        // 暂时返回空数组
        return "[]";
    }
    
    /**
     * 获取用户设置
     */
    public String getUserSettings() {
        // 这里需要实现用户设置的获取逻辑
        // 暂时返回默认设置
        try {
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("defaultOutputDir", "");
            defaultSettings.put("defaultImageQuality", 90);
            defaultSettings.put("autoCleanupDays", 7);
            defaultSettings.put("showPreviewByDefault", true);
            defaultSettings.put("theme", "light");
            return objectMapper.writeValueAsString(defaultSettings);
        } catch (Exception e) {
            logger.error("获取用户设置失败", e);
            return "{}";
        }
    }
    
    /**
     * 保存用户设置
     */
    public String saveUserSettings(String settingsJson) {
        // 这里需要实现用户设置的保存逻辑
        logger.info("保存用户设置: {}", settingsJson);
        return "success";
    }
    
    // ==================== 系统信息相关 ====================
    
    /**
     * 获取系统信息
     */
    public String getSystemInfo() {
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("version", "1.0.0");
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("userHome", System.getProperty("user.home"));
            systemInfo.put("appDataDir", com.watermark.util.PathManager.getAppDataDir());
            return objectMapper.writeValueAsString(systemInfo);
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", message);
            return objectMapper.writeValueAsString(errorResponse);
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
    
    /**
     * 批量处理图片列表（前端兼容性方法）
     * @param imagePathsJson 图片路径列表的JSON字符串
     * @param watermarkConfigJson 水印配置的JSON字符串
     * @param outputDirectory 输出目录路径
     * @return 处理结果的JSON字符串
     */
    public String batchProcessImageList(String imagePathsJson, String watermarkConfigJson, String outputDirectory) {
        try {
            logger.info("开始批量处理图片列表");
            logger.debug("图片路径JSON: {}", imagePathsJson);
            logger.debug("水印配置JSON: {}", watermarkConfigJson);
            logger.debug("输出目录: {}", outputDirectory);
            
            // 解析图片路径列表
            @SuppressWarnings("unchecked")
            List<String> imagePaths = objectMapper.readValue(imagePathsJson, List.class);
            logger.info("需要处理的图片数量: {}", imagePaths.size());
            
            // 验证输出目录
            if (outputDirectory == null || outputDirectory.trim().isEmpty()) {
                return createErrorResponse("输出目录不能为空");
            }
            
            // 解析水印配置并添加输出目录
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = objectMapper.readValue(watermarkConfigJson, Map.class);
            configMap.put("outputPath", outputDirectory);
            String updatedConfigJson = objectMapper.writeValueAsString(configMap);
            
            logger.debug("更新后的配置JSON: {}", updatedConfigJson);
            
            int successCount = 0;
            int failureCount = 0;
            
            // 逐个处理图片
            for (String imagePath : imagePaths) {
                try {
                    logger.debug("处理图片: {}", imagePath);
                    String result = watermarkService.processImageWithWatermark(imagePath, updatedConfigJson);
                    
                    if (result != null && !result.isEmpty()) {
                        successCount++;
                        logger.debug("图片处理成功: {} -> {}", imagePath, result);
                    } else {
                        failureCount++;
                        logger.warn("图片处理失败: {}", imagePath);
                    }
                    
                } catch (Exception e) {
                    failureCount++;
                    logger.error("处理图片失败: {}", imagePath, e);
                }
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("total", imagePaths.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("message", String.format("批量处理完成：成功 %d 张，失败 %d 张", successCount, failureCount));
            
            String resultJson = objectMapper.writeValueAsString(result);
            logger.info("批量处理完成: {}", resultJson);
            return resultJson;
            
        } catch (Exception e) {
            logger.error("批量处理图片列表失败", e);
            return createErrorResponse("批量处理失败: " + e.getMessage());
        }
    }
}