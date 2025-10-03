package com.watermark.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.Color;
import java.io.IOException;

/**
 * Color 类型的 JSON 反序列化器
 * 支持从十六进制字符串反序列化为 Color 对象
 */
public class ColorDeserializer extends JsonDeserializer<Color> {
    
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String colorValue = p.getValueAsString();
        
        if (colorValue == null || colorValue.trim().isEmpty()) {
            return Color.WHITE; // 默认白色
        }
        
        try {
            // 去除可能的空格
            colorValue = colorValue.trim();
            
            // 处理十六进制格式
            if (colorValue.startsWith("#")) {
                if (colorValue.length() == 7) {
                    // #RRGGBB 格式
                    return Color.decode(colorValue);
                } else if (colorValue.length() == 9) {
                    // #AARRGGBB 格式 (带透明度)
                    long value = Long.parseLong(colorValue.substring(1), 16);
                    int alpha = (int) ((value >> 24) & 0xFF);
                    int red = (int) ((value >> 16) & 0xFF);
                    int green = (int) ((value >> 8) & 0xFF);
                    int blue = (int) (value & 0xFF);
                    return new Color(red, green, blue, alpha);
                }
            } else if (colorValue.length() == 6) {
                // RRGGBB 格式 (无#前缀)
                return Color.decode("#" + colorValue);
            } else if (colorValue.length() == 8) {
                // AARRGGBB 格式 (无#前缀，带透明度)
                long value = Long.parseLong(colorValue, 16);
                int alpha = (int) ((value >> 24) & 0xFF);
                int red = (int) ((value >> 16) & 0xFF);
                int green = (int) ((value >> 8) & 0xFF);
                int blue = (int) (value & 0xFF);
                return new Color(red, green, blue, alpha);
            }
            
            // 尝试作为RGB整数值解析
            try {
                int rgb = Integer.parseInt(colorValue);
                return new Color(rgb);
            } catch (NumberFormatException e) {
                // 忽略，继续下面的处理
            }
            
        } catch (Exception e) {
            System.err.println("解析颜色值失败: " + colorValue + ", 错误: " + e.getMessage());
        }
        
        // 如果所有解析都失败，返回默认白色
        return Color.WHITE;
    }
}