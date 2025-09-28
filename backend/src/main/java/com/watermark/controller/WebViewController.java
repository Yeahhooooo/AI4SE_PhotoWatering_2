package com.watermark.controller;

import com.watermark.bridge.JavaScriptBridge;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Web视图控制器
 * 管理JavaFX WebView和Vue前端的集成
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class WebViewController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(WebViewController.class);
    
    @FXML
    private WebView webView;
    
    private WebEngine webEngine;
    private JavaScriptBridge jsBridge;
    private Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupWebView();
        loadVueApplication();
    }
    
    /**
     * 设置Stage引用（用于文件对话框等）
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        if (jsBridge != null) {
            jsBridge.setStage(stage);
        }
    }
    
    /**
     * 配置WebView
     */
    private void setupWebView() {
        webEngine = webView.getEngine();
        
        // 启用JavaScript
        webEngine.setJavaScriptEnabled(true);
        
        // 设置用户代理
        webEngine.setUserAgent("WatermarkApp/1.0 JavaFX WebView");
        
        // 监听页面加载状态
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                onPageLoadSuccess();
            } else if (newValue == Worker.State.FAILED) {
                onPageLoadFailed();
            }
        });
        
        // 监听JavaScript错误
        webEngine.setOnError(event -> {
            logger.error("WebView JavaScript错误: {}", event.getMessage());
        });
        
        // 监听页面标题变化
        webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                logger.debug("页面标题: {}", newValue);
            }
        });
        
        logger.info("WebView配置完成");
    }
    
    /**
     * 加载Vue应用程序
     */
    private void loadVueApplication() {
        try {
            // 从classpath加载Vue应用的index.html
            URL indexUrl = getClass().getResource("/web/index.html");
            
            if (indexUrl == null) {
                logger.error("未找到Vue应用资源文件: /web/index.html");
                showDevelopmentMessage();
                return;
            }
            
            String indexPath = indexUrl.toExternalForm();
            logger.info("加载Vue应用: {}", indexPath);
            
            webEngine.load(indexPath);
            
        } catch (Exception e) {
            logger.error("加载Vue应用失败", e);
            showDevelopmentMessage();
        }
    }
    
    /**
     * 页面加载成功回调
     */
    private void onPageLoadSuccess() {
        logger.info("Vue应用加载成功");
        
        try {
            // 创建JavaScript-Java桥接器
            jsBridge = new JavaScriptBridge();
            jsBridge.setStage(stage);
            
            // 将Java对象暴露给JavaScript
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApi", jsBridge);
            
            // 执行初始化JavaScript
            webEngine.executeScript(
                "console.log('JavaFX WebView initialized');" +
                "console.log('Java API available:', typeof window.javaApi);" +
                "" +
                "// 通知Vue应用JavaFX环境已准备就绪" +
                "if (window.Vue && window.Vue.config && window.Vue.config.globalProperties) {" +
                "    window.Vue.config.globalProperties.$javaApi = window.javaApi;" +
                "}" +
                "" +
                "// 触发自定义事件通知Vue应用" +
                "if (window.dispatchEvent) {" +
                "    window.dispatchEvent(new Event('javafxReady'));" +
                "}"
            );
            
            logger.info("JavaScript桥接器初始化完成");
            
        } catch (Exception e) {
            logger.error("初始化JavaScript桥接器失败", e);
        }
    }
    
    /**
     * 页面加载失败回调
     */
    private void onPageLoadFailed() {
        logger.error("Vue应用加载失败");
        showDevelopmentMessage();
    }
    
    /**
     * 显示开发提示信息
     */
    private void showDevelopmentMessage() {
        String developmentHtml = 
            "<!DOCTYPE html>" +
            "<html lang=\"zh-CN\">" +
            "<head>" +
            "    <meta charset=\"UTF-8\">" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "    <title>开发模式</title>" +
            "    <style>" +
            "        body {" +
            "            font-family: 'Microsoft YaHei', Arial, sans-serif;" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
            "            margin: 0;" +
            "            padding: 0;" +
            "            display: flex;" +
            "            justify-content: center;" +
            "            align-items: center;" +
            "            height: 100vh;" +
            "            color: white;" +
            "        }" +
            "        .container {" +
            "            text-align: center;" +
            "            background: rgba(255, 255, 255, 0.1);" +
            "            padding: 2rem;" +
            "            border-radius: 1rem;" +
            "            backdrop-filter: blur(10px);" +
            "            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);" +
            "        }" +
            "        h1 { font-size: 2.5rem; margin-bottom: 1rem; }" +
            "        p { font-size: 1.2rem; margin-bottom: 0.5rem; }" +
            "        .code {" +
            "            background: rgba(0, 0, 0, 0.3);" +
            "            padding: 1rem;" +
            "            border-radius: 0.5rem;" +
            "            font-family: 'Consolas', monospace;" +
            "            margin: 1rem 0;" +
            "        }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <h1>🚀 开发模式</h1>" +
            "        <p>Vue前端应用尚未构建</p>" +
            "        <p>请先在frontend目录执行以下命令：</p>" +
            "        <div class=\"code\">" +
            "            npm install<br>" +
            "            npm run build" +
            "        </div>" +
            "        <p>然后重新运行JavaFX应用</p>" +
            "    </div>" +
            "</body>" +
            "</html>";
        
        webEngine.loadContent(developmentHtml);
    }
    
    /**
     * 执行JavaScript代码
     */
    public Object executeScript(String script) {
        try {
            return webEngine.executeScript(script);
        } catch (Exception e) {
            logger.error("执行JavaScript失败: {}", script, e);
            return null;
        }
    }
    
    /**
     * 刷新页面
     */
    public void reload() {
        webEngine.reload();
    }
    
    /**
     * 获取当前页面URL
     */
    public String getCurrentUrl() {
        return webEngine.getLocation();
    }
}