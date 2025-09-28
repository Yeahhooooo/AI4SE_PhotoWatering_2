package com.watermark.strategy;

import com.watermark.model.WatermarkConfig;

import java.awt.image.BufferedImage;

/**
 * 水印策略接口
 * 定义水印应用的统一接口
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public interface WatermarkStrategy {
    
    /**
     * 应用水印到图片
     * 
     * @param originalImage 原始图片
     * @param config 水印配置
     * @return 应用水印后的图片
     * @throws Exception 处理异常
     */
    BufferedImage applyWatermark(BufferedImage originalImage, WatermarkConfig config) throws Exception;
}