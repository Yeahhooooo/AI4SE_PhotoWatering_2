package com.watermark.dto;

/**
 * 水印配置数据传输对象
 * 用于前端到后端的JSON数据传输
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WatermarkConfigData {
    
    public String type = "TEXT";           // TEXT 或 IMAGE
    public String text = "水印";           // 水印文本
    public String position = "BOTTOM_RIGHT"; // 位置
    public float opacity = 0.7f;           // 透明度
    public int fontSize = 24;              // 字体大小
    public String fontColor = "#FFFFFF";   // 字体颜色
    public String imagePath = "";          // 图片水印路径
    public String outputPath = "";         // 输出路径
    
    // 新增字段以支持完整功能
    public String fontFamily = "Microsoft YaHei"; // 字体家族
    public boolean bold = false;           // 粗体
    public boolean italic = false;         // 斜体
    public int offsetX = 20;               // X轴偏移量
    public int offsetY = 20;               // Y轴偏移量
    public float rotation = 0.0f;          // 旋转角度
    public float scale = 1.0f;             // 缩放比例
    
    // 阴影效果
    public boolean hasShadow = true;       // 是否有阴影
    public String shadowColor = "#000000"; // 阴影颜色
    public int shadowOffsetX = 2;          // 阴影X偏移
    public int shadowOffsetY = 2;          // 阴影Y偏移
    public int shadowBlur = 3;             // 阴影模糊度
    
    // 描边效果
    public boolean hasStroke = false;      // 是否有描边
    public String strokeColor = "#000000"; // 描边颜色
    public int strokeWidth = 1;            // 描边宽度
    
    // 图片水印相关
    public int watermarkWidth = 100;       // 水印宽度
    public int watermarkHeight = 100;      // 水印高度
    public boolean maintainAspectRatio = true; // 保持宽高比
    
    // 输出配置
    public OutputConfig outputConfig = new OutputConfig();
    
    /**
     * 输出配置内部类
     */
    public static class OutputConfig {
        public String namingRule = "suffix";  // original, prefix, suffix, custom
        public String filePrefix = "wm_";     // 文件前缀
        public String fileSuffix = "_watermarked"; // 文件后缀
        
        public OutputConfig() {
        }
        
        public OutputConfig(String namingRule, String filePrefix, String fileSuffix) {
            this.namingRule = namingRule;
            this.filePrefix = filePrefix;
            this.fileSuffix = fileSuffix;
        }
    }
    
    /**
     * 默认构造函数
     */
    public WatermarkConfigData() {
    }
    
    /**
     * 带参构造函数
     */
    public WatermarkConfigData(String type, String text) {
        this.type = type;
        this.text = text;
    }
    
    @Override
    public String toString() {
        return "WatermarkConfigData{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", position='" + position + '\'' +
                ", opacity=" + opacity +
                ", fontSize=" + fontSize +
                ", fontColor='" + fontColor + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }
}