package com.watermark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.*;
import com.watermark.dto.WatermarkConfigData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.concurrent.CompletableFuture;
// 保持轻量依赖，暂不引入数据库服务，后续通过Maven集成完整后端

/**
 * 使用Java 8内置JavaFX的简化版启动器
 */
public class SimpleWebApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            System.out.println("开始启动Java 8版本的水印应用...");
            
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            // 启用JavaScript
            webEngine.setJavaScriptEnabled(true);
            System.out.println("JavaScript已启用");
            
            // 监听加载状态
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                System.out.println("WebEngine状态变更: " + oldState + " -> " + newState);
                
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("页面加载成功！");
                    
                    // 延迟执行页面检查和API注入
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000);
                            checkPageAndInjectAPI(webEngine);
                        } catch (Exception e) {
                            System.err.println("延迟执行异常: " + e.getMessage());
                        }
                    });
                    
                } else if (newState == Worker.State.FAILED) {
                    System.err.println("页面加载失败！");
                    Throwable exception = webEngine.getLoadWorker().getException();
                    if (exception != null) {
                        System.err.println("异常信息: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                } else if (newState == Worker.State.RUNNING) {
                    System.out.println("页面正在加载中...");
                } else if (newState == Worker.State.SCHEDULED) {
                    System.out.println("页面加载已安排");
                }
            });
            
            // 监听JavaScript错误和警告
            webEngine.setOnError(event -> {
                System.err.println("JavaScript错误: " + event.getMessage());
            });
            
            webEngine.setOnAlert(event -> {
                System.out.println("JavaScript警告: " + event.getData());
            });
            
            // 加载前端页面
            loadWebPage(webEngine);
            
            // 创建窗口
            Scene scene = new Scene(webView, 1200, 800);
            primaryStage.setTitle("水印应用 - Java 8版本");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("应用程序正在关闭...");
                Platform.exit();
            });
            
            primaryStage.show();
            System.out.println("应用程序窗口已显示");
            
        } catch (Exception e) {
            System.err.println("应用启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadWebPage(WebEngine webEngine) {
        // 按优先级尝试加载不同的HTML文件
        File simpleHtml = new File("target/classes/web/index_simple.html");
        File originalHtml = new File("target/classes/web/index.html");
        File fallbackHtml = new File("target/classes/web/index_fallback.html");
        
        File targetHtml = null;
        
        // 优先加载simple版本，然后是原始版本
        if (simpleHtml.exists() && simpleHtml.length() > 0) {
            targetHtml = simpleHtml;
        } else if (originalHtml.exists() && originalHtml.length() > 0) {
            targetHtml = originalHtml;
        } else if (fallbackHtml.exists() && fallbackHtml.length() > 0) {
            targetHtml = fallbackHtml;
        }
        
        if (targetHtml != null) {
            String url = targetHtml.toURI().toString();
            System.out.println("加载前端页面: " + url);
            System.out.println("HTML文件大小: " + targetHtml.length() + " bytes");
            System.out.println("使用的HTML文件: " + targetHtml.getName());
            
            // 检查资源文件
            checkResources();
            
            webEngine.load(url);
            
        } else {
            System.err.println("所有HTML文件都不存在或为空，加载内置测试页面");
            // 加载测试页面
            loadTestPage(webEngine);
        }
    }
    
    private void checkResources() {
        File assetsDir = new File("target/classes/web/assets");
        if (assetsDir.exists()) {
            System.out.println("Assets目录存在，包含 " + assetsDir.listFiles().length + " 个文件:");
            File[] files = assetsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
        } else {
            System.err.println("警告: Assets目录不存在!");
        }
    }
    
    private void checkPageAndInjectAPI(WebEngine webEngine) {
        try {
            System.out.println("开始页面检查和API注入...");
            
            // 检查页面基本信息
            Object title = webEngine.executeScript("document.title || 'No Title'");
            System.out.println("页面标题: " + title);
            
            Object bodyExists = webEngine.executeScript("document.body !== null");
            System.out.println("Body元素存在: " + bodyExists);
            
            if (Boolean.TRUE.equals(bodyExists)) {
                Object bodyContent = webEngine.executeScript("document.body.innerHTML.length || 0");
                System.out.println("Body内容长度: " + bodyContent);
            }
            
            // 检查App元素
            Object appExists = webEngine.executeScript("document.getElementById('app') !== null");
            System.out.println("App元素存在: " + appExists);
            
            if (Boolean.TRUE.equals(appExists)) {
                Object appContent = webEngine.executeScript("document.getElementById('app').innerHTML.length || 0");
                System.out.println("App元素内容长度: " + appContent);
            }
            
            // 检查Vue
            Object vueType = webEngine.executeScript("typeof Vue");
            System.out.println("Vue类型: " + vueType);
            
            // 注入JavaScript API
            System.out.println("注入Java API...");
            webEngine.executeScript("console.log('JavaFX WebView 准备就绪 - Java 8版本');");
            
            // 注入真实的API
            String apiScript = 
                "window.javaApi = {" +
                "  processImage: function(path, config) { " +
                "    console.log('处理图片:', path, config); " +
                "    try {" +
                "      return new Promise((resolve, reject) => {" +
                "        setTimeout(() => {" +
                "          try {" +
                "            var result = window.javaApp.processImage(path, config);" +
                "            resolve({success: true, outputPath: result});" +
                "          } catch (error) {" +
                "            reject(error);" +
                "          }" +
                "        }, 100);" +
                "      });" +
                "    } catch (error) {" +
                "      return Promise.reject(error);" +
                "    }" +
                "  }," +
                "  selectImage: function() { " +
                "    console.log('选择图片'); " +
                "    try {" +
                "      return new Promise((resolve, reject) => {" +
                "        setTimeout(() => {" +
                "          var path = window.javaApp.selectImage();" +
                "          if (path && path !== 'null') {" +
                "            resolve(path);" +
                "          } else {" +
                "            resolve(null);" +
                "          }" +
                "        }, 100);" +
                "      });" +
                "    } catch (error) {" +
                "      return Promise.resolve('mock_image.jpg');" +
                "    }" +
                "  }," +
                "  selectDirectory: function() { " +
                "    console.log('选择输出文件夹'); " +
                "    try {" +
                "      return new Promise((resolve, reject) => {" +
                "        setTimeout(() => {" +
                "          var path = window.javaApp.selectDirectory();" +
                "          if (path && path !== 'null') {" +
                "            resolve(path);" +
                "          } else {" +
                "            resolve(null);" +
                "          }" +
                "        }, 100);" +
                "      });" +
                "    } catch (error) {" +
                "      return Promise.resolve(null);" +
                "    }" +
                "  }," +
                "  getTemplates: function() { " +
                "    console.log('获取模板列表'); " +
                "    return Promise.resolve([" +
                "      {id: 1, name: '默认文本模板', type: 'TEXT'}," +
                "      {id: 2, name: '简单图片模板', type: 'IMAGE'}" +
                "    ]); " +
                "  }," +
                "  saveTemplate: function(template) { " +
                "    console.log('保存模板:', template); " +
                "    return Promise.resolve({success: true, id: Math.floor(Math.random() * 1000)}); " +
                "  }" +
                "};";
            
            webEngine.executeScript(apiScript);
            
            // 注入简化的桥接对象
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaApp", new SimpleBridge(primaryStage));
            
            // 触发准备就绪事件
            webEngine.executeScript("window.dispatchEvent(new Event('javafxReady'));");
            
            // 更新调试信息
            webEngine.executeScript("if (document.getElementById('debug-status')) { document.getElementById('debug-status').textContent = 'JavaFX API已注入'; }");
            
            System.out.println("JavaScript API注入完成！");
            
        } catch (Exception e) {
            System.err.println("页面检查或API注入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadTestPage(WebEngine webEngine) {
        System.out.println("加载内置测试页面...");
        
        String testHtml = "<!DOCTYPE html>" +
            "<html lang='zh-CN'>" +
            "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>水印应用测试</title>" +
                "<style>" +
                    "body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }" +
                    ".container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; }" +
                    "h1 { color: #409eff; text-align: center; }" +
                    "button { padding: 10px 20px; margin: 10px; font-size: 16px; cursor: pointer; }" +
                    ".result { margin-top: 20px; padding: 15px; background: #f0f9ff; border-radius: 5px; }" +
                    ".error { background: #fef2f2; color: #dc2626; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class='container'>" +
                    "<h1>🚀 水印应用测试页面</h1>" +
                    "<p>前端构建文件未找到，显示测试页面用于调试</p>" +
                    "<div>" +
                        "<button onclick='testJavaScript()'>测试JavaScript</button>" +
                        "<button onclick='testJavaAPI()'>测试Java API</button>" +
                        "<button onclick='clearResult()'>清除结果</button>" +
                    "</div>" +
                    "<div id='result' class='result' style='display:none;'></div>" +
                "</div>" +
                "<script>" +
                    "function testJavaScript() {" +
                        "showResult('JavaScript功能正常运行！', false);" +
                    "}" +
                    "function testJavaAPI() {" +
                        "if (window.javaApi) {" +
                            "showResult('Java API已成功注入并可用！', false);" +
                            "window.javaApi.getTemplates().then(function(templates) {" +
                                "showResult('Java API测试成功！获取到 ' + templates.length + ' 个模板', false);" +
                            "});" +
                        "} else {" +
                            "showResult('Java API未找到，可能还未注入', true);" +
                        "}" +
                    "}" +
                    "function showResult(message, isError) {" +
                        "var result = document.getElementById('result');" +
                        "result.textContent = message;" +
                        "result.className = 'result ' + (isError ? 'error' : '');" +
                        "result.style.display = 'block';" +
                    "}" +
                    "function clearResult() {" +
                        "document.getElementById('result').style.display = 'none';" +
                    "}" +
                    "console.log('测试页面已加载');" +
                "</script>" +
            "</body>" +
            "</html>";
        
        webEngine.loadContent(testHtml);
    }
    
    // 简单的桥接类
    public static class SimpleBridge {
        private final Stage stage;
        
        public SimpleBridge(Stage stage) {
            this.stage = stage;
        }
        
        public String selectImage() {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择图片文件");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
                    new FileChooser.ExtensionFilter("所有文件", "*.*")
                );
                
                File selectedFile = fileChooser.showOpenDialog(stage);
                String result = selectedFile != null ? selectedFile.getAbsolutePath() : null;
                System.out.println("选择的图片: " + result);
                return result;
                
            } catch (Exception e) {
                System.err.println("选择文件失败: " + e.getMessage());
                return null;
            }
        }
        
        public String selectDirectory() {
            try {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("选择输出文件夹");
                
                File selectedDirectory = directoryChooser.showDialog(stage);
                String result = selectedDirectory != null ? selectedDirectory.getAbsolutePath() : null;
                System.out.println("选择的输出文件夹: " + result);
                return result;
                
            } catch (Exception e) {
                System.err.println("选择文件夹失败: " + e.getMessage());
                return null;
            }
        }
        
    public String processImage(String imagePath, String configJson) {
            try {
                System.out.println("开始处理图片: " + imagePath);
                System.out.println("配置: " + configJson);
                
        // 实际处理图片
        return processImageWithWatermark(imagePath, configJson);
                
            } catch (Exception e) {
                System.err.println("图片处理失败: " + e.getMessage());
                throw new RuntimeException("图片处理失败: " + e.getMessage());
            }
        }
        // 简化版本暂不记录历史，后续通过完整后端服务对接
        
        private String processImageWithWatermark(String imagePath, String configJson) {
            try {
                // 解析配置
                WatermarkConfigData config = parseWatermarkConfig(configJson);
                BufferedImage originalImage = ImageIO.read(new File(imagePath));
                if (originalImage == null) {
                    throw new RuntimeException("无法读取图片文件: " + imagePath);
                }
                BufferedImage watermarkedImage;
                if ("IMAGE".equalsIgnoreCase(config.type) && config.imagePath != null && !config.imagePath.isEmpty()) {
                    watermarkedImage = addImageWatermark(originalImage, config);
                } else {
                    watermarkedImage = addTextWatermark(originalImage, config);
                }
                
                // 根据配置生成输出路径
                String outputPath;
                System.out.println("检查outputPath配置 - config.outputPath: '" + config.outputPath + "'");
                System.out.println("outputPath是否为空: " + (config.outputPath == null || config.outputPath.isEmpty()));
                
                if (config.outputPath != null && !config.outputPath.isEmpty()) {
                    // 使用用户指定的输出路径
                    // 将前端传来的正斜杠路径转换为系统适配的路径
                    String normalizedPath = config.outputPath.replace('/', File.separatorChar);
                    System.out.println("使用用户指定的输出路径: " + normalizedPath);
                    File inputFile = new File(imagePath);
                    String fileName = inputFile.getName();
                    String name = fileName;
                    String extension = "";
                    int dotIndex = fileName.lastIndexOf(".");
                    if (dotIndex > 0) {
                        extension = fileName.substring(dotIndex);
                        name = fileName.substring(0, dotIndex);
                    }
                    String watermarkedName = name + "_watermarked" + extension;
                    outputPath = new File(normalizedPath, watermarkedName).getAbsolutePath();
                    System.out.println("生成的完整输出路径: " + outputPath);
                } else {
                    // 使用默认输出路径
                    System.out.println("使用默认输出路径");
                    outputPath = generateOutputPath(imagePath);
                    System.out.println("默认输出路径: " + outputPath);
                }
                
                File outputFile = new File(outputPath);
                outputFile.getParentFile().mkdirs();
                String formatName = getImageFormat(imagePath);
                if ("png".equals(formatName)) {
                    BufferedImage rgbImage = new BufferedImage(
                        watermarkedImage.getWidth(), 
                        watermarkedImage.getHeight(), 
                        BufferedImage.TYPE_INT_RGB
                    );
                    Graphics2D g = rgbImage.createGraphics();
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                    g.drawImage(watermarkedImage, 0, 0, null);
                    g.dispose();
                    watermarkedImage = rgbImage;
                }
                boolean success = ImageIO.write(watermarkedImage, formatName, outputFile);
                if (!success) {
                    throw new RuntimeException("保存图片失败，可能不支持该格式: " + formatName);
                }
                System.out.println("图片处理完成: " + outputPath);
                System.out.println("文件大小: " + outputFile.length() + " bytes");
                return outputPath;
            } catch (Exception e) {
                System.err.println("图片处理异常: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("图片处理失败: " + e.getMessage());
            }
        }
        
        private BufferedImage addTextWatermark(BufferedImage originalImage, WatermarkConfigData config) {
            // 创建一个新的图片副本
            BufferedImage watermarkedImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            
            Graphics2D g2d = watermarkedImage.createGraphics();
            
            try {
                // 绘制原始图片
                g2d.drawImage(originalImage, 0, 0, null);
                
                // 设置抗锯齿
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // 设置字体
                Font font = new Font("Microsoft YaHei", Font.BOLD, config.fontSize);
                g2d.setFont(font);
                
                // 设置颜色和透明度
                Color color = parseColor(config.fontColor);
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.opacity);
                g2d.setComposite(alphaComposite);
                g2d.setColor(color);
                
                // 计算文字位置
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(config.text);
                int textHeight = fm.getHeight();
                
                int x, y;
                switch (config.position.toUpperCase()) {
                    case "TOP_LEFT":
                        x = 20;
                        y = textHeight + 20;
                        break;
                    case "TOP_RIGHT":
                        x = originalImage.getWidth() - textWidth - 20;
                        y = textHeight + 20;
                        break;
                    case "BOTTOM_LEFT":
                        x = 20;
                        y = originalImage.getHeight() - 20;
                        break;
                    case "CENTER":
                        x = (originalImage.getWidth() - textWidth) / 2;
                        y = (originalImage.getHeight() + textHeight) / 2;
                        break;
                    case "BOTTOM_RIGHT":
                    default:
                        x = originalImage.getWidth() - textWidth - 20;
                        y = originalImage.getHeight() - 20;
                        break;
                }
                
                // 绘制文字
                g2d.drawString(config.text, x, y);
                
                return watermarkedImage;
                
            } finally {
                g2d.dispose();
            }
        }
        
        private WatermarkConfigData parseWatermarkConfig(String configJson) {
            WatermarkConfigData config = new WatermarkConfigData();
            System.out.println("开始解析JSON配置: " + configJson);
            
            try {
                JSONObject jsonObject = JSON.parseObject(configJson);
                
                System.out.println("解析JSON对象:" + jsonObject.toString());

                config.type = jsonObject.getString("type");
                if (config.type == null) config.type = "TEXT";

                config.text = jsonObject.getString("text");
                if (config.text == null) config.text = "水印";

                config.position = jsonObject.getString("position");
                if (config.position == null) config.position = "BOTTOM_RIGHT";

                config.opacity = jsonObject.getFloatValue("opacity");
                if (config.opacity == 0) config.opacity = 0.7f;

                config.fontSize = jsonObject.getIntValue("fontSize");
                if (config.fontSize == 0) config.fontSize = 24;

                config.fontColor = jsonObject.getString("fontColor");
                if (config.fontColor == null) config.fontColor = "#FFFFFF";

                config.imagePath = jsonObject.getString("imagePath");
                if (config.imagePath == null) config.imagePath = "";

                config.outputPath = jsonObject.getString("outputPath");
                if (config.outputPath == null) config.outputPath = "";

                
                System.out.println("提取字段 type: '" + config.type + "'");
                System.out.println("提取字段 text: '" + config.text + "'");
                System.out.println("提取字段 position: '" + config.position + "'");
                System.out.println("提取字段 outputPath: '" + config.outputPath + "'");
                
                return config;
            } catch (Exception e) {
                System.err.println("JSON解析失败: " + e.getMessage());
                e.printStackTrace();
                // 返回默认配置
                config.type = "TEXT";
                config.text = "水印";
                config.position = "BOTTOM_RIGHT";
                config.opacity = 0.7f;
                config.fontSize = 24;
                config.fontColor = "#FFFFFF";
                config.imagePath = "";
                config.outputPath = "";
                return config;
            }
        }
        
        private Color parseColor(String colorStr) {
            try {
                if (colorStr.startsWith("#")) {
                    return Color.decode(colorStr);
                } else {
                    return Color.WHITE; // 默认白色
                }
            } catch (Exception e) {
                return Color.WHITE;
            }
        }
        
        private String generateOutputPath(String inputPath) {
            File inputFile = new File(inputPath);
            String name = inputFile.getName();
            String extension = "";
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = name.substring(dotIndex);
                name = name.substring(0, dotIndex);
            }
            
            // 输出到当前目录的output文件夹
            String outputDir = "output";
            new File(outputDir).mkdirs();
            
            return outputDir + File.separator + name + "_watermarked" + extension;
        }
        
        private String getImageFormat(String imagePath) {
            String extension = imagePath.toLowerCase();
            if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
                return "jpg";
            } else if (extension.endsWith(".png")) {
                return "png";
            } else if (extension.endsWith(".bmp")) {
                return "bmp";
            } else if (extension.endsWith(".gif")) {
                return "gif";
            } else {
                return "png"; // 默认PNG格式
            }
        }
        
        // 模板/历史/设置 API 将在完整后端接入后开放
        // 图片水印叠加
        private BufferedImage addImageWatermark(BufferedImage originalImage, WatermarkConfigData config) {
            try {
                BufferedImage watermarkImg = ImageIO.read(new File(config.imagePath));
                if (watermarkImg == null) {
                    throw new RuntimeException("无法读取水印图片: " + config.imagePath);
                }
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = watermarkedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, null);
                // 设置透明度
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.opacity);
                g2d.setComposite(alphaComposite);
                // 计算水印图片位置
                int wmWidth = watermarkImg.getWidth();
                int wmHeight = watermarkImg.getHeight();
                int x = 0, y = 0;
                switch (config.position.toUpperCase()) {
                    case "TOP_LEFT":
                        x = 20; y = 20; break;
                    case "TOP_RIGHT":
                        x = width - wmWidth - 20; y = 20; break;
                    case "BOTTOM_LEFT":
                        x = 20; y = height - wmHeight - 20; break;
                    case "CENTER":
                        x = (width - wmWidth) / 2; y = (height - wmHeight) / 2; break;
                    case "BOTTOM_RIGHT":
                    default:
                        x = width - wmWidth - 20; y = height - wmHeight - 20; break;
                }
                g2d.drawImage(watermarkImg, x, y, null);
                g2d.dispose();
                return watermarkedImage;
            } catch (Exception e) {
                throw new RuntimeException("图片水印处理失败: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("启动水印应用 (Java 8兼容版本)...");
        launch(args);
    }
}