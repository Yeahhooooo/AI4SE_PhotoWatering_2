package com.watermark.service;

import com.watermark.model.ImageInfo;
import com.watermark.util.PathManager;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片服务
 * 处理图片文件的导入、缩略图生成等操作
 * 
 * @author Watermark Team
 * @version 1.0.0
 */
public class ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    
    private static ImageService instance;
    private final ConcurrentHashMap<String, ImageInfo> imageCache = new ConcurrentHashMap<>();
    
    private ImageService() {
        // 私有构造函数，单例模式
    }
    
    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }
    
    /**
     * 处理图片文件列表
     */
    public List<ImageInfo> processImageFiles(List<File> files) {
        List<ImageInfo> imageInfos = new ArrayList<>();
        
        for (File file : files) {
            try {
                if (PathManager.isSupportedImageFormat(file.getName())) {
                    ImageInfo imageInfo = processImageFile(file);
                    if (imageInfo != null) {
                        imageInfos.add(imageInfo);
                        imageCache.put(imageInfo.getId(), imageInfo);
                    }
                } else {
                    logger.warn("不支持的图片格式: {}", file.getName());
                }
            } catch (Exception e) {
                logger.error("处理图片文件失败: {}", file.getAbsolutePath(), e);
            }
        }
        
        logger.info("成功处理 {} 个图片文件", imageInfos.size());
        return imageInfos;
    }
    
    /**
     * 处理单个图片文件
     */
    private ImageInfo processImageFile(File file) throws IOException {
        // 读取图片信息
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            logger.warn("无法读取图片: {}", file.getAbsolutePath());
            return null;
        }
        
        // 创建ImageInfo对象
        ImageInfo imageInfo = new ImageInfo(file.getName(), file.getAbsolutePath());
        imageInfo.setFileSize(file.length());
        imageInfo.setWidth(image.getWidth());
        imageInfo.setHeight(image.getHeight());
        imageInfo.setFormat(PathManager.getFileExtension(file.getName()).toUpperCase());
        imageInfo.setHasAlphaChannel(image.getColorModel().hasAlpha());
        
        // 生成缩略图
        String thumbnailPath = generateThumbnail(file, imageInfo.getId());
        imageInfo.setThumbnailPath(thumbnailPath);
        
        logger.debug("处理图片完成: {}", imageInfo);
        return imageInfo;
    }
    
    /**
     * 处理图片文件夹
     */
    public List<ImageInfo> processImageFolder(File directory) {
        List<File> imageFiles = new ArrayList<>();
        collectImageFiles(directory, imageFiles);
        
        logger.info("在文件夹 {} 中找到 {} 个图片文件", directory.getAbsolutePath(), imageFiles.size());
        return processImageFiles(imageFiles);
    }
    
    /**
     * 递归收集图片文件
     */
    private void collectImageFiles(File directory, List<File> imageFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectImageFiles(file, imageFiles);
                } else if (PathManager.isSupportedImageFormat(file.getName())) {
                    imageFiles.add(file);
                }
            }
        }
    }
    
    /**
     * 生成缩略图
     */
    private String generateThumbnail(File originalFile, String imageId) {
        try {
            String thumbnailPath = PathManager.generateThumbnailPath(originalFile.getAbsolutePath());
            
            // 检查缩略图是否已存在且是最新的
            File thumbnailFile = new File(thumbnailPath);
            if (thumbnailFile.exists() && 
                thumbnailFile.lastModified() > originalFile.lastModified()) {
                logger.debug("使用现有缩略图: {}", thumbnailPath);
                return thumbnailPath;
            }
            
            // 生成150x150的缩略图，保持宽高比
            Thumbnails.of(originalFile)
                      .size(150, 150)
                      .outputFormat("jpg")
                      .outputQuality(0.8)
                      .toFile(thumbnailFile);
            
            logger.debug("生成缩略图: {}", thumbnailPath);
            return thumbnailPath;
            
        } catch (IOException e) {
            logger.error("生成缩略图失败: {}", originalFile.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * 根据ID获取图片信息
     */
    public ImageInfo getImageInfo(String imageId) {
        return imageCache.get(imageId);
    }
    
    /**
     * 读取图片文件
     */
    public BufferedImage loadImage(String imagePath) throws IOException {
        return ImageIO.read(new File(imagePath));
    }
    
    /**
     * 读取图片文件（根据ImageInfo）
     */
    public BufferedImage loadImage(ImageInfo imageInfo) throws IOException {
        return loadImage(imageInfo.getFilePath());
    }
    
    /**
     * 清空图片缓存
     */
    public void clearCache() {
        imageCache.clear();
        logger.info("图片缓存已清空");
    }
    
    /**
     * 获取缓存的图片数量
     */
    public int getCacheSize() {
        return imageCache.size();
    }
    
    /**
     * 获取所有缓存的图片信息
     */
    public List<ImageInfo> getAllCachedImages() {
        return new ArrayList<>(imageCache.values());
    }
}