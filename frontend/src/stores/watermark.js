import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

export const useWatermarkStore = defineStore('watermark', () => {
  // 当前图片信息
  const currentImage = ref(null)
  const imagePreviewUrl = ref('')
  
  // 水印配置
  const watermarkConfig = reactive({
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
    scale: 1.0,
    outputPath: ''
  })

  // 预览配置
  const previewConfig = reactive({ ...watermarkConfig })
  
  // 当前选择的模板
  const currentTemplate = ref(null)

  // 设置当前图片
  const setCurrentImage = (imageInfo) => {
    console.log('设置当前图片:', imageInfo)
    
    // 安全检查imageInfo
    if (imageInfo && imageInfo.path && typeof imageInfo.path === 'string' && imageInfo.path.trim().length > 0) {
      currentImage.value = imageInfo
      
      const toFileUrl = (p) => {
        if (!p || typeof p !== 'string') return ''
        if (p.startsWith('file://')) return p
        const norm = p.replace(/\\/g, '/').replace(/^([A-Za-z]):/, '/$1:')
        return `file:///${norm.replace(/^\/+/, '')}`
      }
      
      const newUrl = toFileUrl(imageInfo.path)
      console.log('转换后的图片URL:', newUrl)
      
      // 强制触发响应式更新 - 先清空再设置
      imagePreviewUrl.value = ''
      
      // 使用nextTick确保DOM更新
      setTimeout(() => {
        imagePreviewUrl.value = newUrl
        console.log('响应式URL更新完成:', imagePreviewUrl.value)
        
        // 持久化存储到localStorage
        try {
          localStorage.setItem('currentImage', JSON.stringify(imageInfo))
          localStorage.setItem('imagePreviewUrl', newUrl)
          console.log('图片信息已保存到localStorage')
        } catch (e) {
          console.warn('无法保存图片信息到localStorage:', e)
        }
      }, 10)
    } else {
      // 安全地清理状态
      console.log('图片信息无效，清理状态:', imageInfo)
      currentImage.value = null
      imagePreviewUrl.value = ''
      
      // 清除localStorage
      try {
        localStorage.removeItem('currentImage')
        localStorage.removeItem('imagePreviewUrl')
      } catch (e) {
        console.warn('无法清除localStorage:', e)
      }
    }
  }

  // 更新水印配置
  const updateWatermarkConfig = (newConfig) => {
    console.log('更新水印配置:', newConfig)
    Object.assign(watermarkConfig, newConfig)
    Object.assign(previewConfig, newConfig)
  }

  // 重置水印配置
  const resetWatermarkConfig = () => {
    console.log('重置水印配置')
    const defaultConfig = {
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
      scale: 1.0,
      outputPath: ''
    }
    Object.assign(watermarkConfig, defaultConfig)
    Object.assign(previewConfig, defaultConfig)
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
  
  // 初始化store，恢复状态
  const initStore = () => {
    console.log('初始化watermark store')
    
    try {
      // 检查是否为新的应用会话
      const appStartTime = Date.now()
      const lastAppStartTime = localStorage.getItem('appStartTime')
      const sessionId = sessionStorage.getItem('sessionId')
      
      console.log('应用启动时间:', appStartTime)
      console.log('上次应用启动时间:', lastAppStartTime)
      console.log('当前会话 ID:', sessionId)
      
      // 如果没有会话 ID，说明是新的应用启动
      if (!sessionId) {
        console.log('检测到新的应用启动，清理旧状态')
        localStorage.removeItem('currentImage')
        localStorage.removeItem('imagePreviewUrl')
        localStorage.setItem('appStartTime', appStartTime.toString())
        sessionStorage.setItem('sessionId', appStartTime.toString())
        
        // 初始化为空状态
        currentImage.value = null
        imagePreviewUrl.value = ''
        console.log('新会话初始化完成')
        return
      }
      
      // 如果有会话 ID，说明是同一会话内的页面刷新/切换
      console.log('同一会话内的页面操作，尝试恢复状态')
      
      const savedImage = localStorage.getItem('currentImage')
      const savedPreviewUrl = localStorage.getItem('imagePreviewUrl')
      
      console.log('localStorage中的图片信息:', savedImage)
      console.log('localStorage中的预览URL:', savedPreviewUrl)
      
      if (savedImage && savedPreviewUrl) {
        try {
          const imageInfo = JSON.parse(savedImage)
          console.log('恢复图片状态:', imageInfo)
          
          // 安全检查恢复的数据
          if (imageInfo && imageInfo.path && typeof imageInfo.path === 'string' && imageInfo.path.length > 0) {
            currentImage.value = imageInfo
            imagePreviewUrl.value = savedPreviewUrl
            console.log('状态恢复完成 - 当前图片:', currentImage.value)
            console.log('状态恢复完成 - 预览URL:', imagePreviewUrl.value)
          } else {
            console.log('保存的图片信息无效，清理状态')
            currentImage.value = null
            imagePreviewUrl.value = ''
            localStorage.removeItem('currentImage')
            localStorage.removeItem('imagePreviewUrl')
          }
        } catch (parseError) {
          console.warn('解析保存的图片信息失败:', parseError)
          currentImage.value = null
          imagePreviewUrl.value = ''
          localStorage.removeItem('currentImage')
          localStorage.removeItem('imagePreviewUrl')
        }
      } else {
        console.log('没有找到保存的图片状态')
      }
    } catch (e) {
      console.warn('无法恢复图片状态:', e)
      // 直接清理状态，避免循环调用
      currentImage.value = null
      imagePreviewUrl.value = ''
      try {
        localStorage.removeItem('currentImage')
        localStorage.removeItem('imagePreviewUrl')
      } catch (cleanupError) {
        console.warn('清理状态时出错:', cleanupError)
      }
    }
  }
  
  // 清除状态
  const clearState = () => {
    console.log('清除水印store状态')
    currentImage.value = null
    imagePreviewUrl.value = ''
    try {
      localStorage.removeItem('currentImage')
      localStorage.removeItem('imagePreviewUrl')
    } catch (e) {
      console.warn('无法清除localStorage:', e)
    }
  }
  
  // 手动重置所有状态
  const resetAll = () => {
    console.log('重置所有状态')
    clearState()
    resetWatermarkConfig()
    // 清除会话信息
    try {
      sessionStorage.removeItem('sessionId')
      localStorage.removeItem('appStartTime')
    } catch (e) {
      console.warn('无法清除会话信息:', e)
    }
  }
  
  // 强制刷新图片预览
  const forceRefreshPreview = () => {
    console.log('强制刷新图片预览')
    if (currentImage.value && currentImage.value.path) {
      const currentUrl = imagePreviewUrl.value
      imagePreviewUrl.value = ''
      setTimeout(() => {
        imagePreviewUrl.value = currentUrl
        console.log('强制刷新完成:', currentUrl)
      }, 10)
    }
  }
  
  return {
    currentImage,
    imagePreviewUrl,
    watermarkConfig,
    previewConfig,
    currentTemplate,
    setCurrentImage,
    updateWatermarkConfig,
    resetWatermarkConfig,
    applyTemplate,
    initStore,
    clearState,
    resetAll,
    forceRefreshPreview
  }
})