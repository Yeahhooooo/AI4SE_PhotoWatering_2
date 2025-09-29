import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

// JavaFX Bridge 检测和初始化
const initJavaFXBridge = () => {
  // 检查是否在JavaFX环境中
  if (window.javaApi) {
    console.log('JavaFX环境检测成功')
    return window.javaApi
  }
  
  // 开发环境模拟API
  if (import.meta.env.DEV) {
    console.log('开发环境：使用模拟API')
    window.javaApi = {
      // 模拟Java API方法
      processImage: (imagePath, config) => {
        console.log('模拟处理图片:', imagePath, config)
        return Promise.resolve({
          success: true,
          outputPath: '/mock/output/watermarked_image.jpg'
        })
      },
      selectImage: () => {
        console.log('模拟选择图片')
        return Promise.resolve('/mock/selected/image.jpg')
      },
      saveTemplate: (template) => {
        console.log('模拟保存模板:', template)
        return Promise.resolve({ success: true, id: Date.now() })
      },
      getTemplates: () => {
        console.log('模拟获取模板')
        return Promise.resolve([
          { id: 1, name: '默认文本水印', type: 'TEXT' },
          { id: 2, name: '公司Logo', type: 'IMAGE' }
        ])
      }
    }
  }
  
  return window.javaApi
}

const app = createApp(App)
const pinia = createPinia()

// 配置Element Plus
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)

// 全局提供JavaFX API
const javaApi = initJavaFXBridge()
app.config.globalProperties.$javaApi = javaApi
app.provide('javaApi', javaApi)

// 等待JavaFX环境准备就绪
const mountApp = () => {
  app.mount('#app')
}

if (window.javaApi || import.meta.env.DEV) {
  mountApp()
} else {
  // 监听JavaFX准备就绪事件
  window.addEventListener('javafxReady', () => {
    console.log('JavaFX环境准备就绪')
    const javaApi = initJavaFXBridge()
    app.config.globalProperties.$javaApi = javaApi
    app.provide('javaApi', javaApi)
    mountApp()
  })
  
  // 超时保护
  setTimeout(() => {
    console.warn('JavaFX环境初始化超时，使用模拟模式')
    mountApp()
  }, 3000)
}