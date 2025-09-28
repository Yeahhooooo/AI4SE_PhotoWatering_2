package com.watermark.strategy;

import com.watermark.model.ImageWatermarkConfig;
import com.watermark.model.WatermarkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 图片水印策略实现
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class ImageWatermarkStrategy implements WatermarkStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageWatermarkStrategy.class);
    
    @Override
    public BufferedImage applyWatermark(BufferedImage originalImage, WatermarkConfig config) throws Exception {
        if (!(config instanceof ImageWatermarkConfig)) {
            throw new IllegalArgumentException("配置类型不匹配，期望ImageWatermarkConfig");
        }
        
        ImageWatermarkConfig imageConfig = (ImageWatermarkConfig) config;
        
        // 验证水印图片路径
        if (imageConfig.getImagePath() == null || imageConfig.getImagePath().trim().isEmpty()) {
            logger.warn("水印图片路径为空，跳过处理");
            return originalImage;
        }
        
        // 加载水印图片
        BufferedImage watermarkImage = loadWatermarkImage(imageConfig.getImagePath());
        if (watermarkImage == null) {
            logger.error("无法加载水印图片: {}", imageConfig.getImagePath());
            return originalImage;
        }
        
        // 创建带水印的图片副本
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(),
            originalImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        try {
            // 绘制原始图片
            g2d.drawImage(originalImage, 0, 0, null);
            
            // 设置渲染质量
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 缩放水印图片
            BufferedImage scaledWatermark = scaleWatermarkImage(watermarkImage, imageConfig);
            
            // 计算水印位置
            Point watermarkPosition = imageConfig.calculatePosition(
                originalImage.getWidth(),
                originalImage.getHeight(),
                scaledWatermark.getWidth(),
                scaledWatermark.getHeight()
            );
            
            // 应用旋转变换（如果需要）
            if (imageConfig.getRotation() != 0) {
                applyRotation(g2d, watermarkPosition, scaledWatermark, imageConfig.getRotation(), imageConfig.getOpacity());
            } else {
                // 设置透明度并绘制水印
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageConfig.getOpacity()));
                g2d.drawImage(scaledWatermark, watermarkPosition.x, watermarkPosition.y, null);
            }
            
            logger.debug("图片水印应用完成: {}", imageConfig);
            
        } finally {
            g2d.dispose();
        }
        
        return watermarkedImage;
    }
    
    /**
     * 加载水印图片
     */
    private BufferedImage loadWatermarkImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                logger.error("水印图片文件不存在: {}", imagePath);
                return null;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                logger.error("无法读取水印图片: {}", imagePath);
                return null;
            }
            
            return image;
            
        } catch (Exception e) {
            logger.error("加载水印图片失败: {}", imagePath, e);
            return null;
        }
    }
    
    /**
     * 缩放水印图片
     */
    private BufferedImage scaleWatermarkImage(BufferedImage watermarkImage, ImageWatermarkConfig config) {
        int targetWidth = config.getScaledWidth();
        int targetHeight = config.getScaledHeight();
        
        // 如果尺寸相同，直接返回原图
        if (watermarkImage.getWidth() == targetWidth && watermarkImage.getHeight() == targetHeight) {
            return watermarkImage;
        }
        
        // 如果需要保持宽高比，重新计算尺寸
        if (config.isMaintainAspectRatio()) {
            double aspectRatio = (double) watermarkImage.getWidth() / watermarkImage.getHeight();
            
            if (aspectRatio > 1) { // 宽图
                targetHeight = (int) (targetWidth / aspectRatio);
            } else { // 高图
                targetWidth = (int) (targetHeight * aspectRatio);
            }
        }
        
        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            g2d.drawImage(watermarkImage, 0, 0, targetWidth, targetHeight, null);
            
        } finally {
            g2d.dispose();
        }
        
        return scaledImage;
    }
    
    /**
     * 应用旋转变换
     */
    private void applyRotation(Graphics2D g2d, Point position, BufferedImage watermark, float rotation, float opacity) {
        // 计算旋转中心
        int centerX = position.x + watermark.getWidth() / 2;
        int centerY = position.y + watermark.getHeight() / 2;
        
        // 保存当前变换
        java.awt.geom.AffineTransform originalTransform = g2d.getTransform();
        
        try {
            // 应用旋转
            g2d.rotate(Math.toRadians(rotation), centerX, centerY);
            
            // 设置透明度并绘制水印
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2d.drawImage(watermark, position.x, position.y, null);
            
        } finally {
            // 恢复变换
            g2d.setTransform(originalTransform);
        }
    }
}