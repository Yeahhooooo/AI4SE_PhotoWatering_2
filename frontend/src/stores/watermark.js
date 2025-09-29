import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

export const useWatermarkStore = defineStore('watermark', () => {
  // 当前图片信息
  const currentImage = ref(null)
  const imagePreviewUrl = ref('')
  
  // 水印配置
  const watermarkConfig = reactive({
    type: 'TEXT', // TEXT 或 IMAGE
    // 文本水印配置
    text: '水印文本',
    fontSize: 24,
    fontFamily: 'Microsoft YaHei',
    fontColor: '#FFFFFF',
    fontStyle: 'NORMAL', // NORMAL, BOLD, ITALIC
  // 图像水印配置
  imagePath: '',
    // 通用配置
    position: 'BOTTOM_RIGHT', // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    offsetX: 10,
    offsetY: 10,
    opacity: 0.8,
    rotation: 0,
    scale: 1.0
  })
  
  // 预览配置
  const previewConfig = reactive({
    showPreview: true,
    previewScale: 'fit' // fit, actual, custom
  })
  
  // 当前模板
  const currentTemplate = ref(null)
  
  // 设置当前图片
  const setCurrentImage = (imageInfo) => {
    currentImage.value = imageInfo
    if (imageInfo && imageInfo.path) {
      // 在JavaFX环境中，图片路径需要转换为 file:///C:/... URL
      const toFileUrl = (p) => {
        if (!p) return ''
        if (p.startsWith('file://')) return p
        // 统一分隔符并添加 file:/// 前缀
        const norm = p.replace(/\\/g, '/').replace(/^([A-Za-z]):\//, '/$1:/')
        return `file:///${norm.replace(/^\/+/, '')}`
      }
      imagePreviewUrl.value = toFileUrl(imageInfo.path)
    }
  }
  
  // 更新水印配置
  const updateWatermarkConfig = (config) => {
    Object.assign(watermarkConfig, config)
  }
  
  // 重置水印配置
  const resetWatermarkConfig = () => {
    Object.assign(watermarkConfig, {
      type: 'TEXT',
      text: '水印文本',
      fontSize: 24,
      fontFamily: 'Microsoft YaHei',
      fontColor: '#FFFFFF',
      fontStyle: 'NORMAL',
      imagePath: '',
      position: 'BOTTOM_RIGHT',
      offsetX: 10,
      offsetY: 10,
      opacity: 0.8,
      rotation: 0,
      scale: 1.0
    })
  }
  
  // 应用模板
  const applyTemplate = (template) => {
    if (template && template.config) {
      const config = typeof template.config === 'string' 
        ? JSON.parse(template.config) 
        : template.config
      updateWatermarkConfig(config)
      currentTemplate.value = template
    }
  }
  
  return {
    // 状态
    currentImage,
    imagePreviewUrl,
    watermarkConfig,
    previewConfig,
    currentTemplate,
    // 方法
    setCurrentImage,
    updateWatermarkConfig,
    resetWatermarkConfig,
    applyTemplate
  }
})