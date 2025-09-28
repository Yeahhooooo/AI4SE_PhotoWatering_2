package com.watermark.test;

import com.watermark.service.DatabaseService;
import com.watermark.service.ImageService;
import com.watermark.service.TemplateService;
import com.watermark.util.PathManager;
import com.watermark.model.ImageInfo;
import com.watermark.model.TextWatermarkConfig;
import com.watermark.model.WatermarkTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 后端功能测试类
 * 在没有JavaFX环境的情况下测试核心功能
 */
public class BackendTest {
    
    public static void main(String[] args) {
        System.out.println("=== 水印应用后端测试 ===");
        
        try {
            // 1. 初始化路径管理器
            System.out.println("\n1. 初始化应用目录...");
            PathManager.initializeAppDirectories();
            System.out.println("✓ 应用目录初始化成功");
            System.out.println("数据目录: " + PathManager.getAppDataDir());
            
            // 2. 初始化数据库
            System.out.println("\n2. 初始化数据库...");
            DatabaseService dbService = DatabaseService.getInstance();
            dbService.initialize();
            System.out.println("✓ 数据库初始化成功");
            
            // 3. 测试图片服务
            System.out.println("\n3. 测试图片服务...");
            testImageService();
            
            // 4. 测试模板服务
            System.out.println("\n4. 测试模板服务...");
            testTemplateService();
            
            // 5. 测试路径工具
            System.out.println("\n5. 测试路径工具...");
            testPathManager();
            
            System.out.println("\n=== 所有测试完成 ===");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testImageService() {
        try {
            ImageService imageService = ImageService.getInstance();
            
            // 创建测试文件列表 - 您可以替换为实际的图片文件路径
            System.out.println("请在控制台输入图片文件路径进行测试，或使用默认测试");
            
            // 模拟图片信息
            System.out.println("✓ 图片服务初始化成功");
            System.out.println("缓存大小: " + imageService.getCacheSize());
            
        } catch (Exception e) {
            System.err.println("✗ 图片服务测试失败: " + e.getMessage());
        }
    }
    
    private static void testTemplateService() {
        try {
            TemplateService templateService = TemplateService.getInstance();
            
            // 创建测试模板
            TextWatermarkConfig textConfig = new TextWatermarkConfig("测试水印");
            WatermarkTemplate template = new WatermarkTemplate("测试模板", "这是一个测试模板", textConfig);
            
            // 保存模板
            WatermarkTemplate savedTemplate = templateService.saveTemplate(template);
            System.out.println("✓ 模板保存成功，ID: " + savedTemplate.getId());
            
            // 获取所有模板
            List<WatermarkTemplate> templates = templateService.getAllTemplates();
            System.out.println("✓ 获取模板列表，数量: " + templates.size());
            
            // 删除测试模板
            if (savedTemplate.getId() != null) {
                boolean deleted = templateService.deleteTemplate(savedTemplate.getId());
                System.out.println("✓ 测试模板删除: " + (deleted ? "成功" : "失败"));
            }
            
        } catch (Exception e) {
            System.err.println("✗ 模板服务测试失败: " + e.getMessage());
        }
    }
    
    private static void testPathManager() {
        try {
            System.out.println("应用数据目录: " + PathManager.getAppDataDir());
            System.out.println("缩略图目录: " + PathManager.getThumbnailsDir());
            System.out.println("临时文件目录: " + PathManager.getTempDir());
            System.out.println("数据库路径: " + PathManager.getDatabasePath());
            
            // 测试文件扩展名识别
            String testFile = "test.jpg";
            System.out.println("文件 " + testFile + " 扩展名: " + PathManager.getFileExtension(testFile));
            System.out.println("是否支持的图片格式: " + PathManager.isSupportedImageFormat(testFile));
            
            System.out.println("✓ 路径管理器测试成功");
            
        } catch (Exception e) {
            System.err.println("✗ 路径管理器测试失败: " + e.getMessage());
        }
    }
}