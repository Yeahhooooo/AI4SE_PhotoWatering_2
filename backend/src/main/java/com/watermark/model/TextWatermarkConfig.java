package com.watermark.model;

import java.awt.*;

/**
 * 文本水印配置
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class TextWatermarkConfig extends WatermarkConfig {
    
    private String text = "水印";          // 水印文本
    private String fontFamily = "Microsoft YaHei"; // 字体家族
    private int fontSize = 36;             // 字体大小
    private boolean bold = false;          // 粗体
    private boolean italic = false;        // 斜体
    private Color color = Color.WHITE;     // 字体颜色
    
    // 高级样式选项
    private boolean hasShadow = true;      // 是否有阴影
    private Color shadowColor = Color.BLACK; // 阴影颜色
    private int shadowOffsetX = 2;         // 阴影X偏移
    private int shadowOffsetY = 2;         // 阴影Y偏移
    private int shadowBlur = 3;            // 阴影模糊度
    
    private boolean hasStroke = false;     // 是否有描边
    private Color strokeColor = Color.BLACK; // 描边颜色
    private int strokeWidth = 1;           // 描边宽度
    
    public TextWatermarkConfig() {
        super(WatermarkType.TEXT);
    }
    
    public TextWatermarkConfig(String text) {
        super(WatermarkType.TEXT);
        this.text = text;
    }
    
    @Override
    public WatermarkConfig copy() {
        TextWatermarkConfig copy = new TextWatermarkConfig();
        
        // 复制基类属性
        copy.setPosition(this.getPosition());
        copy.setOffsetX(this.getOffsetX());
        copy.setOffsetY(this.getOffsetY());
        copy.setOpacity(this.getOpacity());
        copy.setRotation(this.getRotation());
        copy.setScale(this.getScale());
        
        // 复制文本水印属性
        copy.text = this.text;
        copy.fontFamily = this.fontFamily;
        copy.fontSize = this.fontSize;
        copy.bold = this.bold;
        copy.italic = this.italic;
        copy.color = new Color(this.color.getRGB());
        
        copy.hasShadow = this.hasShadow;
        copy.shadowColor = new Color(this.shadowColor.getRGB());
        copy.shadowOffsetX = this.shadowOffsetX;
        copy.shadowOffsetY = this.shadowOffsetY;
        copy.shadowBlur = this.shadowBlur;
        
        copy.hasStroke = this.hasStroke;
        copy.strokeColor = new Color(this.strokeColor.getRGB());
        copy.strokeWidth = this.strokeWidth;
        
        return copy;
    }
    
    /**
     * 创建字体对象
     */
    public Font createFont() {
        int style = Font.PLAIN;
        if (bold && italic) {
            style = Font.BOLD | Font.ITALIC;
        } else if (bold) {
            style = Font.BOLD;
        } else if (italic) {
            style = Font.ITALIC;
        }
        
        float scaledFontSize = fontSize * getScale();
        return new Font(fontFamily, style, Math.round(scaledFontSize));
    }
    
    /**
     * 获取带透明度的文本颜色
     */
    public Color getColorWithOpacity() {
        return new Color(
            color.getRed(),
            color.getGreen(), 
            color.getBlue(),
            Math.round(255 * getOpacity())
        );
    }
    
    /**
     * 获取带透明度的阴影颜色
     */
    public Color getShadowColorWithOpacity() {
        return new Color(
            shadowColor.getRed(),
            shadowColor.getGreen(),
            shadowColor.getBlue(),
            Math.round(255 * getOpacity() * 0.8f) // 阴影透明度稍低
        );
    }
    
    /**
     * 获取带透明度的描边颜色
     */
    public Color getStrokeColorWithOpacity() {
        return new Color(
            strokeColor.getRed(),
            strokeColor.getGreen(),
            strokeColor.getBlue(),
            Math.round(255 * getOpacity())
        );
    }
    
    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { 
        this.fontSize = Math.max(8, Math.min(200, fontSize)); 
    }
    
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    
    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    public boolean isHasShadow() { return hasShadow; }
    public void setHasShadow(boolean hasShadow) { this.hasShadow = hasShadow; }
    
    public Color getShadowColor() { return shadowColor; }
    public void setShadowColor(Color shadowColor) { this.shadowColor = shadowColor; }
    
    public int getShadowOffsetX() { return shadowOffsetX; }
    public void setShadowOffsetX(int shadowOffsetX) { this.shadowOffsetX = shadowOffsetX; }
    
    public int getShadowOffsetY() { return shadowOffsetY; }
    public void setShadowOffsetY(int shadowOffsetY) { this.shadowOffsetY = shadowOffsetY; }
    
    public int getShadowBlur() { return shadowBlur; }
    public void setShadowBlur(int shadowBlur) { 
        this.shadowBlur = Math.max(0, Math.min(20, shadowBlur)); 
    }
    
    public boolean isHasStroke() { return hasStroke; }
    public void setHasStroke(boolean hasStroke) { this.hasStroke = hasStroke; }
    
    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }
    
    public int getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(int strokeWidth) { 
        this.strokeWidth = Math.max(1, Math.min(10, strokeWidth)); 
    }
    
    @Override
    public String toString() {
        return "TextWatermarkConfig{" +
                "text='" + text + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontSize=" + fontSize +
                ", position=" + getPosition() +
                ", opacity=" + getOpacity() +
                '}';
    }
}