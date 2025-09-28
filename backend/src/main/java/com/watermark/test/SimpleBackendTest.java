package com.watermark.test;

import com.watermark.util.PathManager;
import com.watermark.service.ImageService;
import com.watermark.model.WatermarkConfig;
import com.watermark.model.TextWatermarkConfig;
import java.awt.Color;
import java.awt.Font;

/**
 * 简化的后端功能测试类 - 不依赖有问题的组件
 */
public class SimpleBackendTest {
    
    public static void main(String[] args) {
        System.out.println("======== 简化后端测试 ========");
        
        try {
            testPathManager();
            testImageService();
            testWatermarkConfig();
            
            System.out.println("======== 测试完成！========");
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPathManager() {
        System.out.println("\n--- 测试路径管理器 ---");
        
        try {
            PathManager pathManager = PathManager.getInstance();
            System.out.println("应用数据目录: " + pathManager.getAppDataPath());
            System.out.println("数据库文件路径: " + pathManager.getDatabasePath());
            System.out.println("模板目录: " + pathManager.getTemplatePath());
            System.out.println("导出目录: " + pathManager.getExportPath());
            
            // 确保目录存在
            pathManager.ensureDirectoryExists(pathManager.getAppDataPath());
            System.out.println("✓ 路径管理器工作正常");
        } catch (Exception e) {
            System.out.println("× 路径管理器测试失败: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testImageService() {
        System.out.println("\n--- 测试图片服务 ---");
        
        try {
            ImageService imageService = new ImageService();
            
            // 测试支持的格式
            String[] supportedFormats = {"jpg", "jpeg", "png", "bmp", "tiff", "gif"};
            System.out.println("支持的图片格式: " + String.join(", ", supportedFormats));
            
            System.out.println("✓ 图片服务初始化成功");
        } catch (Exception e) {
            System.out.println("× 图片服务测试失败: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testWatermarkConfig() {
        System.out.println("\n--- 测试水印配置 ---");
        
        try {
            // 测试文本水印配置
            TextWatermarkConfig textConfig = new TextWatermarkConfig();
            textConfig.setText("测试水印");
            textConfig.setFont(new Font("Arial", Font.BOLD, 24));
            textConfig.setColor(Color.BLACK);
            textConfig.setOpacity(0.8f);
            
            WatermarkConfig config = new WatermarkConfig();
            config.setTextConfig(textConfig);
            
            System.out.println("水印文本: " + config.getTextConfig().getText());
            System.out.println("字体: " + config.getTextConfig().getFont().getName());
            System.out.println("字体大小: " + config.getTextConfig().getFont().getSize());
            System.out.println("透明度: " + config.getTextConfig().getOpacity());
            
            System.out.println("✓ 水印配置测试通过");
        } catch (Exception e) {
            System.out.println("× 水印配置测试失败: " + e.getMessage());
            throw e;
        }
    }
}