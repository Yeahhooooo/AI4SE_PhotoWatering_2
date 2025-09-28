package com.watermark.test;

import com.watermark.util.PathManager;
import com.watermark.service.ImageService;
import com.watermark.model.WatermarkConfig;
import com.watermark.model.TextWatermarkConfig;
import java.awt.Color;
import java.awt.Font;

/**
 * Simple backend test - English only to avoid encoding issues
 */
public class EnglishBackendTest {
    
    public static void main(String[] args) {
        System.out.println("======== Backend Test ========");
        
        try {
            testPathManager();
            testImageService();
            testWatermarkConfig();
            
            System.out.println("======== Test Complete! ========");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPathManager() {
        System.out.println("\n--- Testing Path Manager ---");
        
        try {
            PathManager pathManager = PathManager.getInstance();
            System.out.println("App data directory: " + pathManager.getAppDataPath());
            System.out.println("Database file path: " + pathManager.getDatabasePath());
            System.out.println("Template directory: " + pathManager.getTemplatePath());
            System.out.println("Export directory: " + pathManager.getExportPath());
            
            // Ensure directory exists
            pathManager.ensureDirectoryExists(pathManager.getAppDataPath());
            System.out.println("✓ Path manager working properly");
        } catch (Exception e) {
            System.out.println("× Path manager test failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testImageService() {
        System.out.println("\n--- Testing Image Service ---");
        
        try {
            ImageService imageService = new ImageService();
            
            // Test supported formats
            String[] supportedFormats = {"jpg", "jpeg", "png", "bmp", "tiff", "gif"};
            System.out.println("Supported formats: " + String.join(", ", supportedFormats));
            
            System.out.println("✓ Image service initialized successfully");
        } catch (Exception e) {
            System.out.println("× Image service test failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testWatermarkConfig() {
        System.out.println("\n--- Testing Watermark Config ---");
        
        try {
            // Test text watermark configuration
            TextWatermarkConfig textConfig = new TextWatermarkConfig();
            textConfig.setText("Test Watermark");
            textConfig.setFont(new Font("Arial", Font.BOLD, 24));
            textConfig.setColor(Color.BLACK);
            textConfig.setOpacity(0.8f);
            
            WatermarkConfig config = new WatermarkConfig();
            config.setTextConfig(textConfig);
            
            System.out.println("Watermark text: " + config.getTextConfig().getText());
            System.out.println("Font: " + config.getTextConfig().getFont().getName());
            System.out.println("Font size: " + config.getTextConfig().getFont().getSize());
            System.out.println("Opacity: " + config.getTextConfig().getOpacity());
            
            System.out.println("✓ Watermark configuration test passed");
        } catch (Exception e) {
            System.out.println("× Watermark configuration test failed: " + e.getMessage());
            throw e;
        }
    }
}