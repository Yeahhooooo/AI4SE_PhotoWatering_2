package com.watermark;

import com.watermark.controller.WebViewController;
import com.watermark.service.DatabaseService;
import com.watermark.util.PathManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 水印应用主类
 * JavaFX应用程序入口点
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WatermarkApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(WatermarkApplication.class);
    
    private static final String APP_TITLE = "水印图片处理应用";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // 初始化应用数据目录和数据库
        try {
            PathManager.initializeAppDirectories();
            DatabaseService.getInstance().initialize();
            logger.info("应用初始化完成");
        } catch (Exception e) {
            logger.error("应用初始化失败", e);
            throw e;
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载FXML布局文件
            FXMLLoader fxmlLoader = new FXMLLoader(
                WatermarkApplication.class.getResource("/fxml/main-view.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // 获取控制器并传递Stage引用
            WebViewController controller = fxmlLoader.getController();
            controller.setStage(primaryStage);
            
            // 设置窗口属性
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            
            // 设置窗口关闭事件
            primaryStage.setOnCloseRequest(event -> {
                logger.info("应用程序关闭");
                try {
                    // 清理临时文件
                    PathManager.cleanupTempFiles();
                    // 关闭数据库连接
                    DatabaseService.getInstance().close();
                } catch (Exception e) {
                    logger.error("应用程序关闭时清理资源失败", e);
                }
            });
            
            // 显示主窗口
            primaryStage.show();
            
            logger.info("JavaFX应用程序启动成功");
            
        } catch (IOException e) {
            logger.error("加载主界面失败", e);
            showErrorAndExit("应用程序启动失败", "无法加载主界面: " + e.getMessage());
        } catch (Exception e) {
            logger.error("应用程序启动失败", e);
            showErrorAndExit("应用程序启动失败", e.getMessage());
        }
    }
    
    /**
     * 显示错误信息并退出应用程序
     */
    private void showErrorAndExit(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    /**
     * 应用程序主入口方法
     */
    public static void main(String[] args) {
        logger.info("启动水印图片处理应用...");
        
        // 设置系统属性
        System.setProperty("prism.lcdtext", "false");
        
        // 启动JavaFX应用
        launch(args);
    }
}