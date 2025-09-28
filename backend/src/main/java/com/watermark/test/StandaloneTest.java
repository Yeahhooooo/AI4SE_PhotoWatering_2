package com.watermark.test;

import java.io.File;
import java.awt.Color;
import java.awt.Font;

/**
 * Standalone backend test - no dependencies
 */
public class StandaloneTest {
    
    public static void main(String[] args) {
        System.out.println("======== Standalone Backend Test ========");
        
        try {
            testBasicJava();
            testFileSystem();
            testAWTComponents();
            
            System.out.println("======== All Tests Passed! ========");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBasicJava() {
        System.out.println("\n--- Testing Basic Java Functionality ---");
        
        // Test string operations
        String testString = "Watermark Application";
        System.out.println("Test string: " + testString);
        System.out.println("String length: " + testString.length());
        System.out.println("String uppercase: " + testString.toUpperCase());
        
        // Test basic data types
        int width = 800;
        int height = 600;
        float opacity = 0.8f;
        
        System.out.println("Image dimensions: " + width + " x " + height);
        System.out.println("Opacity: " + opacity);
        
        System.out.println("+ Basic Java functionality working");
    }
    
    private static void testFileSystem() {
        System.out.println("\n--- Testing File System Operations ---");
        
        try {
            // Test getting user home directory
            String userHome = System.getProperty("user.home");
            System.out.println("User home directory: " + userHome);
            
            // Test creating a File object
            File homeDir = new File(userHome);
            System.out.println("Home directory exists: " + homeDir.exists());
            System.out.println("Home directory is directory: " + homeDir.isDirectory());
            
            // Test app data directory concept
            String appDataPath = userHome + File.separator + "WatermarkApp";
            File appDataDir = new File(appDataPath);
            System.out.println("App data path would be: " + appDataPath);
            
            System.out.println("+ File system operations working");
        } catch (Exception e) {
            System.out.println("- File system test failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testAWTComponents() {
        System.out.println("\n--- Testing AWT Components ---");
        
        try {
            // Test Font creation
            Font font = new Font("Arial", Font.BOLD, 24);
            System.out.println("Font name: " + font.getName());
            System.out.println("Font style: " + font.getStyle());
            System.out.println("Font size: " + font.getSize());
            
            // Test Color creation
            Color black = Color.BLACK;
            Color white = Color.WHITE;
            Color custom = new Color(255, 0, 0, 128); // Red with alpha
            
            System.out.println("Black color RGB: " + black.getRGB());
            System.out.println("White color RGB: " + white.getRGB());
            System.out.println("Custom color RGB: " + custom.getRGB());
            System.out.println("Custom color alpha: " + custom.getAlpha());
            
            System.out.println("+ AWT components working");
        } catch (Exception e) {
            System.out.println("- AWT components test failed: " + e.getMessage());
            throw e;
        }
    }
}