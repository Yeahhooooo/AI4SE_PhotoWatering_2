<template>
  <div id="app">
    <!-- 加载遮罩 -->
    <div v-if="appStore.loading" class="loading-overlay">
      <el-loading-directive 
        :loading="appStore.loading"
        element-loading-text="加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
      />
    </div>
    
    <!-- 主界面 -->
    <el-container class="app-container">
      <!-- 侧边栏导航 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h2>水印工具</h2>
        </div>
        
        <el-menu
          :default-active="$route.name"
          class="sidebar-menu"
          router
          unique-opened
        >
          <el-menu-item index="watermark">
            <el-icon><Picture /></el-icon>
            <span>水印编辑</span>
          </el-menu-item>
          
          <el-menu-item index="templates">
            <el-icon><Collection /></el-icon>
            <span>模板管理</span>
          </el-menu-item>
          
          <!-- <el-menu-item index="history">
            <el-icon><Clock /></el-icon>
            <span>处理历史</span>
          </el-menu-item> -->
          
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>应用设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 处理进度对话框 -->
    <el-dialog
      v-model="appStore.processing"
      title="处理中"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="processing-dialog">
        <el-progress
          :percentage="processingProgress"
          :status="processingStatus"
          :stroke-width="8"
        />
        <p class="processing-text">{{ processingText }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'
import { useAppStore } from './stores/app'
import { 
  Picture, 
  Collection, 
  Clock, 
  Setting 
} from '@element-plus/icons-vue'

const appStore = useAppStore()

// 处理进度相关
const processingProgress = ref(0)
const processingStatus = ref('')
const processingText = ref('正在处理图片...')

// 初始化JavaFX API
const initializeApp = async () => {
  // 设置JavaFX API
  if (window.javaApi) {
    appStore.setJavaApi(window.javaApi)
    
    // 加载初始数据
    try {
      await Promise.all([
        appStore.loadSettings(),
        appStore.loadTemplates()
      ])
    } catch (error) {
      console.error('初始化应用数据失败:', error)
    }
  }
}

// 全局提供处理进度控制
const updateProcessingProgress = (progress, text) => {
  processingProgress.value = progress
  processingText.value = text || '正在处理图片...'
  
  if (progress >= 100) {
    processingStatus.value = 'success'
    setTimeout(() => {
      appStore.setProcessing(false)
      processingProgress.value = 0
      processingStatus.value = ''
    }, 1000)
  }
}

provide('updateProcessingProgress', updateProcessingProgress)

onMounted(() => {
  initializeApp()
})
</script>

<style>
/* 全局样式重置 */
* {
  box-sizing: border-box;
}

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* 应用容器 */
.app-container {
  height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #409eff 0%, #337ecc 100%);
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.15);
  color: white;
  border-right: 3px solid white;
}

/* 主内容区域 */
.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 处理进度对话框 */
.processing-dialog {
  text-align: center;
  padding: 20px;
}

.processing-text {
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .sidebar {
    width: 60px !important;
  }
  
  .logo h2 {
    display: none;
  }
  
  .sidebar-menu .el-menu-item span {
    display: none;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8abb2;
}
</style>