package com.watermark.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.ImageInfo;
import com.watermark.model.WatermarkConfig;
import com.watermark.model.TextWatermarkConfig;
import com.watermark.model.ImageWatermarkConfig;
import com.watermark.dto.WatermarkConfigData;
import com.watermark.strategy.WatermarkStrategy;
import com.watermark.strategy.WatermarkStrategyFactory;
import com.watermark.util.PathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * 水印服务
 * 处理水印的生成和应用
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WatermarkService {
    
    private static final Logger logger = LoggerFactory.getLogger(WatermarkService.class);
    
    private static WatermarkService instance;
    private final ImageService imageService;
    
    private WatermarkService() {
        this.imageService = ImageService.getInstance();
    }
    
    public static synchronized WatermarkService getInstance() {
        if (instance == null) {
            instance = new WatermarkService();
        }
        return instance;
    }
    
    /**
     * 异步生成水印预览
     */
    public CompletableFuture<String> generatePreviewAsync(String imageId, WatermarkConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generatePreview(imageId, config);
            } catch (Exception e) {
                logger.error("异步生成预览失败", e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 生成水印预览
     */
    public String generatePreview(String imageId, WatermarkConfig config) throws Exception {
        ImageInfo imageInfo = imageService.getImageInfo(imageId);
        if (imageInfo == null) {
            throw new IllegalArgumentException("图片不存在: " + imageId);
        }
        
        // 加载原始图片
        BufferedImage originalImage = imageService.loadImage(imageInfo);
        
        // 应用水印
        BufferedImage watermarkedImage = applyWatermark(originalImage, config);
        
        // 生成预览图（缩小到合适的大小）
        BufferedImage previewImage = createPreviewImage(watermarkedImage);
        
        // 转换为Base64字符串
        return imageToBase64(previewImage, "jpg");
    }
    
    /**
     * 应用水印到图片
     */
    public BufferedImage applyWatermark(BufferedImage originalImage, WatermarkConfig config) throws Exception {
        // 使用策略模式选择合适的水印处理器
        logger.debug("应用水印，配置类型: {}, 配置对象类型: {}", config.getType(), config.getClass().getSimpleName());
        
        WatermarkStrategy strategy = WatermarkStrategyFactory.getStrategy(config.getType());
        logger.debug("选择的水印策略: {}", strategy.getClass().getSimpleName());
        
        return strategy.applyWatermark(originalImage, config);
    }
    
    /**
     * 处理并保存带水印的图片
     */
    public String processAndSaveImage(String imageId, WatermarkConfig config, String outputPath) throws Exception {
        ImageInfo imageInfo = imageService.getImageInfo(imageId);
        if (imageInfo == null) {
            throw new IllegalArgumentException("图片不存在: " + imageId);
        }
        
        // 加载原始图片
        BufferedImage originalImage = imageService.loadImage(imageInfo);
        
        // 应用水印
        BufferedImage watermarkedImage = applyWatermark(originalImage, config);
        
        // 保存图片
        saveImage(watermarkedImage, outputPath, getOutputFormat(outputPath));
        
        logger.info("水印图片保存完成: {}", outputPath);
        return outputPath;
    }
    
    /**
     * 创建预览图片（限制尺寸以提高性能）
     */
    private BufferedImage createPreviewImage(BufferedImage originalImage) {
        int maxSize = 800; // 预览图最大尺寸
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        if (originalWidth <= maxSize && originalHeight <= maxSize) {
            return originalImage;
        }
        
        // 计算缩放比例
        double scale = Math.min((double) maxSize / originalWidth, (double) maxSize / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        scaledImage.createGraphics().drawImage(
            originalImage.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH),
            0, 0, null
        );
        
        return scaledImage;
    }
    
    /**
     * 将图片转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image, String format) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/" + format + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 保存图片文件
     */
    private void saveImage(BufferedImage image, String outputPath, String format) throws Exception {
        File outputFile = new File(outputPath);
        
        // 确保输出目录存在
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 保存图片
        if (!ImageIO.write(image, format, outputFile)) {
            throw new Exception("不支持的图片格式: " + format);
        }
    }
    
    /**
     * 根据文件路径获取输出格式
     */
    private String getOutputFormat(String outputPath) {
        String extension = PathManager.getFileExtension(outputPath);
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "bmp":
                return "bmp";
            case "tiff":
            case "tif":
                return "tiff";
            default:
                return "jpg"; // 默认使用JPEG格式
        }
    }
    
    /**
     * 处理图片并应用水印 (支持JSON配置)
     */
    public String processImageWithWatermark(String imagePath, String configJson) throws Exception {
        try {
            logger.info("开始处理图片: {}", imagePath);
            logger.debug("配置JSON: {}", configJson);
            
            // 解析JSON配置为WatermarkConfigData对象
            WatermarkConfigData configData = parseWatermarkConfig(configJson);
            
            // 加载原始图片
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage == null) {
                throw new RuntimeException("无法读取图片文件: " + imagePath);
            }
            
            // 将DTO转换为WatermarkConfig
            WatermarkConfig config = convertToWatermarkConfig(configData);
            
            // 应用水印
            BufferedImage watermarkedImage = applyWatermark(originalImage, config);
            
            // 生成输出路径
            String outputPath = generateOutputPath(imagePath, configData.outputPath, configData.outputConfig);
            
            // 保存图片
            saveImage(watermarkedImage, outputPath, getOutputFormat(outputPath));
            
            logger.info("图片处理完成: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            logger.error("图片处理失败: " + e.getMessage(), e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析JSON配置为WatermarkConfigData对象
     */
    private WatermarkConfigData parseWatermarkConfig(String configJson) {
        try {
            logger.debug("开始解析JSON配置: {}", configJson);
            
            JSONObject jsonObject = JSON.parseObject(configJson);
            WatermarkConfigData config = new WatermarkConfigData();
            
            // 解析基本字段
            config.type = jsonObject.getString("type");
            if (config.type == null) config.type = "TEXT";
            
            config.text = jsonObject.getString("text");
            if (config.text == null) config.text = "水印";
            
            config.position = jsonObject.getString("position");
            if (config.position == null) config.position = "BOTTOM_RIGHT";
            
            config.opacity = jsonObject.getFloatValue("opacity");
            if (config.opacity == 0) config.opacity = 0.7f;
            
            config.fontSize = jsonObject.getIntValue("fontSize");
            if (config.fontSize == 0) config.fontSize = 24;
            
            config.fontColor = jsonObject.getString("fontColor");
            if (config.fontColor == null) config.fontColor = "#FFFFFF";
            
            config.imagePath = jsonObject.getString("imagePath");
            if (config.imagePath == null) config.imagePath = "";
            
            config.outputPath = jsonObject.getString("outputPath");
            if (config.outputPath == null) config.outputPath = "";
            
            // 解析高级字段
            config.scale = jsonObject.getFloatValue("scale");
            if (config.scale == 0) config.scale = 1.0f;
            
            config.watermarkWidth = jsonObject.getIntValue("watermarkWidth");
            if (config.watermarkWidth == 0) config.watermarkWidth = 100;
            
            config.watermarkHeight = jsonObject.getIntValue("watermarkHeight");
            if (config.watermarkHeight == 0) config.watermarkHeight = 100;
            
            config.offsetX = jsonObject.getIntValue("offsetX");
            // 只有当JSON中没有offsetX字段时才使用默认值，允许0值
            if (!jsonObject.containsKey("offsetX")) config.offsetX = 20;
            
            config.offsetY = jsonObject.getIntValue("offsetY");
            // 只有当JSON中没有offsetY字段时才使用默认值，允许0值
            if (!jsonObject.containsKey("offsetY")) config.offsetY = 20;
            
            config.rotation = jsonObject.getFloatValue("rotation");
            
            // 解析输出配置
            JSONObject outputConfigJson = jsonObject.getJSONObject("outputConfig");
            if (outputConfigJson != null) {
                config.outputConfig.namingRule = outputConfigJson.getString("namingRule");
                if (config.outputConfig.namingRule == null) config.outputConfig.namingRule = "suffix";
                
                config.outputConfig.filePrefix = outputConfigJson.getString("filePrefix");
                if (config.outputConfig.filePrefix == null) config.outputConfig.filePrefix = "wm_";
                
                config.outputConfig.fileSuffix = outputConfigJson.getString("fileSuffix");
                if (config.outputConfig.fileSuffix == null) config.outputConfig.fileSuffix = "_watermarked";
            }
            
            logger.debug("JSON解析完成: type={}, text={}, position={}, outputPath={}, namingRule={}", 
                config.type, config.text, config.position, config.outputPath, config.outputConfig.namingRule);
            
            return config;
            
        } catch (Exception e) {
            logger.error("JSON解析失败: " + e.getMessage(), e);
            // 返回默认配置
            WatermarkConfigData config = new WatermarkConfigData();
            config.type = "TEXT";
            config.text = "水印";
            config.position = "BOTTOM_RIGHT";
            config.opacity = 0.7f;
            config.fontSize = 24;
            config.fontColor = "#FFFFFF";
            config.imagePath = "";
            config.outputPath = "";
            config.scale = 1.0f;
            config.watermarkWidth = 100;
            config.watermarkHeight = 100;
            return config;
        }
    }
    
    /**
     * 将DTO转换为WatermarkConfig对象
     */
    private WatermarkConfig convertToWatermarkConfig(WatermarkConfigData configData) {
        logger.debug("转换水印配置，类型: {}", configData.type);
        
        if ("IMAGE".equalsIgnoreCase(configData.type)) {
            // 图片水印配置
            ImageWatermarkConfig config = new ImageWatermarkConfig(configData.imagePath);
            
            // 设置基本属性
            config.setOpacity(configData.opacity);
            config.setScale(configData.scale);
            config.setOffsetX(configData.offsetX);
            config.setOffsetY(configData.offsetY);
            config.setRotation(configData.rotation);
            
            // 设置位置
            try {
                WatermarkConfig.Position position = WatermarkConfig.Position.valueOf(configData.position.toUpperCase());
                config.setPosition(position);
            } catch (Exception e) {
                logger.warn("无效的位置配置: {}, 使用默认值", configData.position);
                config.setPosition(WatermarkConfig.Position.BOTTOM_RIGHT);
            }
            
            // 设置输出路径
            config.setOutputPath(configData.outputPath);
            
            // 设置图片水印特有属性
            config.setWidth(configData.watermarkWidth);
            config.setHeight(configData.watermarkHeight);
            config.setMaintainAspectRatio(configData.maintainAspectRatio);
            
            logger.debug("创建图片水印配置: imagePath={}, width={}, height={}, type={}", 
                configData.imagePath, configData.watermarkWidth, configData.watermarkHeight, config.getType());
            
            return config;
        } else {
            // 文本水印配置
            TextWatermarkConfig config = new TextWatermarkConfig();
            config.setText(configData.text);
            config.setFontSize(configData.fontSize);
            config.setOpacity(configData.opacity);
            config.setScale(configData.scale);
            config.setOffsetX(configData.offsetX);
            config.setOffsetY(configData.offsetY);
            config.setRotation(configData.rotation);
            
            // 设置位置
            try {
                WatermarkConfig.Position position = WatermarkConfig.Position.valueOf(configData.position.toUpperCase());
                config.setPosition(position);
            } catch (Exception e) {
                logger.warn("无效的位置配置: {}, 使用默认值", configData.position);
                config.setPosition(WatermarkConfig.Position.BOTTOM_RIGHT);
            }
            
            // 设置输出路径
            config.setOutputPath(configData.outputPath);
            
            // 设置文本水印特有属性
            config.setFontFamily(configData.fontFamily);
            config.setBold(configData.bold);
            config.setItalic(configData.italic);
            
            // 解析颜色
            try {
                java.awt.Color fontColor = parseColor(configData.fontColor);
                config.setColor(fontColor);
            } catch (Exception e) {
                logger.warn("无效的颜色配置: {}, 使用默认值", configData.fontColor);
                config.setColor(java.awt.Color.WHITE);
            }
            
            logger.debug("创建文本水印配置: text={}, fontSize={}, fontColor={}", 
                configData.text, configData.fontSize, configData.fontColor);
            
            return config;
        }
    }
    
    /**
     * 生成输出路径（支持自定义命名规则）
     */
    private String generateOutputPath(String inputPath, String userOutputPath, WatermarkConfigData.OutputConfig outputConfig) {
        File inputFile = new File(inputPath);
        String fileName = inputFile.getName();
        String name = fileName;
        String extension = "";
        
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
            name = fileName.substring(0, dotIndex);
        }
        
        // 根据命名规则生成新文件名
        String newFileName;
        String prefix = outputConfig.filePrefix != null ? outputConfig.filePrefix : "";
        String suffix = outputConfig.fileSuffix != null ? outputConfig.fileSuffix : "";
        
        switch (outputConfig.namingRule) {
            case "original":
                newFileName = fileName;
                break;
            case "prefix":
                newFileName = prefix + fileName;
                break;
            case "suffix":
                newFileName = name + suffix + extension;
                break;
            case "custom":
                newFileName = prefix + name + suffix + extension;
                break;
            default:
                // 默认使用后缀模式
                newFileName = name + "_watermarked" + extension;
                break;
        }
        
        logger.debug("文件命名规则: {}, 原文件名: {}, 新文件名: {}", 
            outputConfig.namingRule, fileName, newFileName);
        
        if (userOutputPath != null && !userOutputPath.isEmpty()) {
            // 使用用户指定的输出路径
            String normalizedPath = userOutputPath.replace('/', File.separatorChar);
            return new File(normalizedPath, newFileName).getAbsolutePath();
        } else {
            // 使用默认输出路径
            String outputDir = "output";
            new File(outputDir).mkdirs();
            return outputDir + File.separator + newFileName;
        }
    }
    
    /**
     * 验证水印配置
     */
    public boolean validateConfig(WatermarkConfig config) {
        if (config == null) {
            return false;
        }
        
        // 基本参数验证
        if (config.getOpacity() < 0 || config.getOpacity() > 1) {
            return false;
        }
        
        if (config.getRotation() < -180 || config.getRotation() > 180) {
            return false;
        }
        
        if (config.getScale() <= 0 || config.getScale() > 5) {
            return false;
        }
        
        // 类型特定的验证将在具体的策略类中进行
        return true;
    }
    
    /**
     * 解析颜色字符串为Color对象
     */
    private java.awt.Color parseColor(String colorStr) {
        try {
            if (colorStr != null && colorStr.startsWith("#")) {
                return java.awt.Color.decode(colorStr);
            } else {
                return java.awt.Color.WHITE; // 默认白色
            }
        } catch (Exception e) {
            logger.warn("解析颜色失败: {}, 使用默认白色", colorStr);
            return java.awt.Color.WHITE;
        }
    }
}