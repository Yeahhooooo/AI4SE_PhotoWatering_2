package com.watermark.service;

import com.watermark.model.ImageInfo;
import com.watermark.model.WatermarkConfig;
import com.watermark.strategy.WatermarkStrategy;
import com.watermark.strategy.WatermarkStrategyFactory;
import com.watermark.util.PathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
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
        WatermarkStrategy strategy = WatermarkStrategyFactory.getStrategy(config.getType());
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
}