package com.watermark.strategy;

import com.watermark.model.WatermarkConfig;

/**
 * 水印策略工厂
 * 根据水印类型创建相应的策略实例
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WatermarkStrategyFactory {
    
    private static final TextWatermarkStrategy textStrategy = new TextWatermarkStrategy();
    private static final ImageWatermarkStrategy imageStrategy = new ImageWatermarkStrategy();
    
    /**
     * 根据水印类型获取策略实例
     */
    public static WatermarkStrategy getStrategy(WatermarkConfig.WatermarkType type) {
        switch (type) {
            case TEXT:
                return textStrategy;
            case IMAGE:
                return imageStrategy;
            default:
                throw new IllegalArgumentException("不支持的水印类型: " + type);
        }
    }
}