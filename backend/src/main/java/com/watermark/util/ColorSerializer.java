package com.watermark.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.Color;
import java.io.IOException;

/**
 * Color 类型的 JSON 序列化器
 * 将 Color 对象序列化为十六进制字符串
 */
public class ColorSerializer extends JsonSerializer<Color> {
    
    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeString("#FFFFFF"); // 默认白色
            return;
        }
        
        // 将颜色序列化为十六进制字符串
        String hexColor = String.format("#%02X%02X%02X", 
            value.getRed(), 
            value.getGreen(), 
            value.getBlue()
        );
        
        gen.writeString(hexColor);
    }
}