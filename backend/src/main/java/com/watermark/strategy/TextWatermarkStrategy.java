package com.watermark.strategy;

import com.watermark.model.TextWatermarkConfig;
import com.watermark.model.WatermarkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 文本水印策略实现
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class TextWatermarkStrategy implements WatermarkStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(TextWatermarkStrategy.class);
    
    @Override
    public BufferedImage applyWatermark(BufferedImage originalImage, WatermarkConfig config) throws Exception {
        if (!(config instanceof TextWatermarkConfig)) {
            throw new IllegalArgumentException("配置类型不匹配，期望TextWatermarkConfig");
        }
        
        TextWatermarkConfig textConfig = (TextWatermarkConfig) config;
        
        // 验证文本配置
        if (textConfig.getText() == null || textConfig.getText().trim().isEmpty()) {
            logger.warn("文本水印内容为空，跳过处理");
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
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // 应用旋转变换
            if (textConfig.getRotation() != 0) {
                Point center = new Point(originalImage.getWidth() / 2, originalImage.getHeight() / 2);
                g2d.rotate(Math.toRadians(textConfig.getRotation()), center.x, center.y);
            }
            
            // 设置字体
            Font font = textConfig.createFont();
            g2d.setFont(font);
            
            // 计算文本尺寸
            FontMetrics fontMetrics = g2d.getFontMetrics(font);
            String text = textConfig.getText();
            int textWidth = fontMetrics.stringWidth(text);
            int textHeight = fontMetrics.getHeight();
            int ascent = fontMetrics.getAscent();
            
            // 计算文本位置
            Point textPosition = textConfig.calculatePosition(
                originalImage.getWidth(),
                originalImage.getHeight(),
                textWidth,
                textHeight
            );
            
            // 绘制阴影（如果启用）
            if (textConfig.isHasShadow()) {
                drawTextShadow(g2d, text, textPosition, textConfig, ascent);
            }
            
            // 绘制描边（如果启用）
            if (textConfig.isHasStroke()) {
                drawTextStroke(g2d, text, textPosition, textConfig, ascent);
            }
            
            // 绘制主文本
            drawMainText(g2d, text, textPosition, textConfig, ascent);
            
            logger.debug("文本水印应用完成: {}", textConfig);
            
        } finally {
            g2d.dispose();
        }
        
        return watermarkedImage;
    }
    
    /**
     * 绘制文本阴影
     */
    private void drawTextShadow(Graphics2D g2d, String text, Point position, 
                               TextWatermarkConfig config, int ascent) {
        g2d.setColor(config.getShadowColorWithOpacity());
        
        // 简单阴影效果
        int shadowX = position.x + config.getShadowOffsetX();
        int shadowY = position.y + ascent + config.getShadowOffsetY();
        
        if (config.getShadowBlur() > 0) {
            // 多重绘制模拟模糊效果
            float alpha = 0.3f / config.getShadowBlur();
            for (int i = 0; i < config.getShadowBlur(); i++) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawString(text, shadowX + i, shadowY + i);
                g2d.drawString(text, shadowX - i, shadowY - i);
            }
            // 重置合成模式
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2d.drawString(text, shadowX, shadowY);
        }
    }
    
    /**
     * 绘制文本描边
     */
    private void drawTextStroke(Graphics2D g2d, String text, Point position, 
                               TextWatermarkConfig config, int ascent) {
        g2d.setColor(config.getStrokeColorWithOpacity());
        g2d.setStroke(new BasicStroke(config.getStrokeWidth()));
        
        Font font = config.createFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        
        // 创建文本轮廓
        java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(text, font, frc);
        Shape textShape = textLayout.getOutline(null);
        
        // 平移到正确位置
        g2d.translate(position.x, position.y + ascent);
        g2d.draw(textShape);
        g2d.translate(-position.x, -(position.y + ascent));
    }
    
    /**
     * 绘制主文本
     */
    private void drawMainText(Graphics2D g2d, String text, Point position, 
                             TextWatermarkConfig config, int ascent) {
        g2d.setColor(config.getColorWithOpacity());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getOpacity()));
        g2d.drawString(text, position.x, position.y + ascent);
    }
}