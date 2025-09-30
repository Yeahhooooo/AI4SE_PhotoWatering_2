<template>
  <div class="watermark-editor">
    <el-card shadow="hover" class="editor-card">
      <h2>水印编辑</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <!-- 图片选择与预览 -->
          <div style="margin-bottom: 10px;">
            <el-button type="primary" @click="selectSingleImages">选择图片</el-button>
            <el-button type="success" @click="selectImageFolder" style="margin-left: 10px;">选择文件夹</el-button>
            <el-button type="warning" @click="clearAll" style="margin-left: 10px;">清除所有</el-button>
          </div>
          <!-- 已上传图片列表 -->
          <div v-if="uploadedImages.length > 0" class="uploaded-image-list">
            <h4>已上传图片（{{ uploadedImages.length }}张，点击切换预览）</h4>
            <el-scrollbar height="120px">
              <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                <div v-for="img in uploadedImages" :key="img.path" class="uploaded-image-item" @click="selectImageForPreview(img)">
                  <img :src="toFileUrl(img.path)" alt="" style="width: 60px; height: 60px; object-fit: cover; border: 2px solid #409eff; cursor: pointer;" />
                  <div style="font-size: 12px; text-align: center; max-width: 60px; overflow: hidden; text-overflow: ellipsis;">{{ img.name }}</div>
                </div>
              </div>
            </el-scrollbar>
          </div>
          <!-- 主预览窗口 -->
          <div v-if="watermarkStore.imagePreviewUrl" class="interactive-preview-container">
            <div class="preview-toolbar">
              <el-button-group size="small">
                <el-button @click="resetZoom" icon="Refresh">重置</el-button>
                <el-button @click="zoomIn" icon="ZoomIn">放大</el-button>
                <el-button @click="zoomOut" icon="ZoomOut">缩小</el-button>
              </el-button-group>
              <span class="zoom-info">{{ Math.round(previewState.scale * 100) }}%</span>
            </div>
            <div 
              ref="previewContainer" 
              class="interactive-preview" 
              @wheel="handleWheel"
              @mousedown="handlePreviewMouseDown"
              @mousemove="handlePreviewMouseMove"
              @mouseup="handlePreviewMouseUp"
              @mouseleave="handlePreviewMouseUp"
            >
              <div 
                class="preview-content"
                :style="{
                  transform: `translate(${previewState.offsetX}px, ${previewState.offsetY}px) scale(${previewState.scale})`,
                  transformOrigin: 'top left'
                }"
              >
                <div class="preview-image-container" :style="{ position: 'relative', display: 'inline-block' }">
                  <img 
                    ref="previewImage"
                    :src="watermarkStore.imagePreviewUrl" 
                    alt="预览" 
                    @load="onImageLoad" 
                    @error="onImageError"
                    :style="{ display: 'block', maxWidth: '100%', height: 'auto' }"
                  />
                  <!-- 水印预览层 -->
                  <div 
                    v-if="showWatermarkPreview"
                    class="watermark-overlay"
                    :style="watermarkOverlayStyle"
                    @mousedown="handleWatermarkMouseDown"
                  >
                    <div 
                      class="watermark-content"
                      :style="watermarkContentStyle"
                    >
                      {{ watermarkConfig.type === 'TEXT' ? watermarkConfig.text : '图片水印' }}
                    </div>
                    <!-- 拖拽控制点 -->
                    <div class="drag-handles">
                      <div class="drag-handle drag-handle-center"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="preview-info">
              <p>{{ watermarkStore.currentImage?.path || '未选择图片' }}</p>
              <p v-if="showWatermarkPreview">水印位置: {{ watermarkConfig.position }} | 透明度: {{ Math.round(watermarkConfig.opacity * 100) }}%</p>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <!-- 水印配置表单 -->
          <el-form :model="watermarkConfig" label-width="100px">
            <el-form-item label="类型">
              <el-radio-group v-model="watermarkConfig.type">
                <el-radio label="TEXT">文本水印</el-radio>
                <el-radio label="IMAGE">图片水印</el-radio>
              </el-radio-group>
            </el-form-item>
            <template v-if="watermarkConfig.type === 'TEXT'">
              <el-form-item label="文本内容">
                <el-input v-model="watermarkConfig.text" />
              </el-form-item>
              <el-form-item label="字体大小">
                <el-input-number v-model="watermarkConfig.fontSize" :min="10" :max="100" />
              </el-form-item>
              <el-form-item label="字体颜色">
                <el-color-picker v-model="watermarkConfig.fontColor" />
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="水印图片">
                <el-button @click="selectWatermarkImage">选择水印图片</el-button>
                <div v-if="watermarkConfig.imagePath" class="image-preview">
                  <img :src="toFileUrl(watermarkConfig.imagePath)" alt="水印预览" />
                </div>
              </el-form-item>
              <el-form-item label="水印宽度">
                <el-input-number v-model="watermarkConfig.watermarkWidth" :min="10" :max="1000" />
              </el-form-item>
              <el-form-item label="水印高度">
                <el-input-number v-model="watermarkConfig.watermarkHeight" :min="10" :max="1000" />
              </el-form-item>
            </template>
            <el-form-item label="位置">
              <el-select v-model="watermarkConfig.position">
                <el-option label="左上" value="TOP_LEFT" />
                <el-option label="上中" value="TOP_CENTER" />
                <el-option label="右上" value="TOP_RIGHT" />
                <el-option label="左中" value="MIDDLE_LEFT" />
                <el-option label="居中" value="MIDDLE_CENTER" />
                <el-option label="右中" value="MIDDLE_RIGHT" />
                <el-option label="左下" value="BOTTOM_LEFT" />
                <el-option label="下中" value="BOTTOM_CENTER" />
                <el-option label="右下" value="BOTTOM_RIGHT" />
                <el-option label="自定义位置" value="CUSTOM" />
              </el-select>
              <el-text v-if="watermarkConfig.position === 'CUSTOM'" size="small" type="info" style="margin-left: 8px;">
                精确坐标: ({{ watermarkConfig.offsetX }}, {{ watermarkConfig.offsetY }})
              </el-text>
            </el-form-item>
            <el-form-item label="透明度">
              <el-slider v-model="watermarkConfig.opacity" :min="0" :max="1" :step="0.01" />
            </el-form-item>
            <el-form-item label="旋转角度">
              <el-input-number v-model="watermarkConfig.rotation" :min="0" :max="360" />
            </el-form-item>
            <el-form-item label="照片缩放比例">
              <el-input-number v-model="watermarkConfig.scale" :min="0.1" :max="5" :step="0.1" />
            </el-form-item>
            
            <!-- 文件命名规则配置 -->
            <el-divider content-position="left">导出设置</el-divider>
            <el-form-item label="文件命名规则">
              <el-radio-group v-model="outputConfig.namingRule">
                <el-radio label="original">保留原文件名</el-radio>
                <el-radio label="prefix">添加前缀</el-radio>
                <el-radio label="suffix">添加后缀</el-radio>
                <el-radio label="custom">自定义前缀+后缀</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="outputConfig.namingRule === 'prefix' || outputConfig.namingRule === 'custom'" label="文件前缀">
              <el-input v-model="outputConfig.filePrefix" placeholder="例如: wm_" style="width: 200px;" />
            </el-form-item>
            <el-form-item v-if="outputConfig.namingRule === 'suffix' || outputConfig.namingRule === 'custom'" label="文件后缀">
              <el-input v-model="outputConfig.fileSuffix" placeholder="例如: _watermarked" style="width: 200px;" />
            </el-form-item>
            <el-form-item v-if="outputConfig.namingRule !== 'original'" label="命名预览">
              <el-tag type="info">{{ getFileNamePreview() }}</el-tag>
            </el-form-item>
            
            <el-form-item label="输出路径">
              <el-input v-model="outputPath" placeholder="选择输出文件夹" readonly>
                <template #append>
                  <el-button @click="selectOutputPath">选择文件夹</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="handleProcessAll" :disabled="uploadedImages.length === 0" size="large">
                {{ uploadedImages.length <= 1 ? '处理图片' : `批量处理 (${uploadedImages.length}张)` }}
              </el-button>
              <el-button @click="resetConfig" style="margin-left: 10px;">重置配置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWatermarkStore } from '../stores/watermark'
import { useAppStore } from '../stores/app'

const uploadedImages = ref([])

// 选择单个或多个图片（支持多选）
const selectSingleImages = async () => {
  try {
    console.log('开始选择图片文件')
    console.log('window.javaApi:', window.javaApi)
    console.log('selectMultipleImages方法存在:', typeof window.javaApi?.selectMultipleImages === 'function')
    
    if (!window.javaApi?.selectMultipleImages) {
      ElMessage.error('文件选择功能未就绪')
      return
    }

    console.log('调用selectMultipleImages（支持单选和多选）')
    const imagePathsStr = await window.javaApi.selectMultipleImages()
    console.log('返回的文件路径字符串:', imagePathsStr)
    
    if (imagePathsStr) {
      const imagePaths = JSON.parse(imagePathsStr)
      console.log('解析后的文件路径数组:', imagePaths)
      
      if (Array.isArray(imagePaths) && imagePaths.length > 0) {
        const newImages = imagePaths.map(p => ({ path: p, name: p.split(/[\\\/]/).pop() }))
        uploadedImages.value = [...uploadedImages.value, ...newImages]
        console.log('更新后的uploadedImages:', uploadedImages.value)
        
        ElMessage.success(`已添加 ${imagePaths.length} 张图片`)
        
        // 自动预览最后添加的图片
        if (newImages.length > 0) {
          selectImageForPreview(newImages[newImages.length - 1])
        }
      } else {
        ElMessage.warning('没有选择图片文件')
      }
    } else {
      console.log('用户取消选择文件')
    }
  } catch (error) {
    console.error('选择图片文件失败:', error)
    ElMessage.error('选择图片文件失败: ' + (error.message || error.toString()))
  }
}

// 选择文件夹中的所有图片
const selectImageFolder = async () => {
  try {
    console.log('开始选择文件夹')
    console.log('window.javaApi:', window.javaApi)
    console.log('selectDirectory方法存在:', typeof window.javaApi?.selectDirectory === 'function')
    console.log('listImagesInDirectory方法存在:', typeof window.javaApi?.listImagesInDirectory === 'function')
    
    if (!window.javaApi?.selectDirectory || !window.javaApi?.listImagesInDirectory) {
      ElMessage.error('文件夹选择功能未就绪')
      return
    }

    console.log('调用selectDirectory')
    const folderPath = await window.javaApi.selectDirectory()
    console.log('选择的文件夹路径:', folderPath)
    
    if (folderPath) {
      console.log('调用listImagesInDirectory')
      const imagesStr = await window.javaApi.listImagesInDirectory(folderPath)
      console.log('返回的图片字符串:', imagesStr)
      
      if (imagesStr) {
        const images = JSON.parse(imagesStr)
        console.log('解析后的图片数组:', images)
        
        if (Array.isArray(images) && images.length > 0) {
          const newImages = images.map(p => ({ path: p, name: p.split(/[\\\/]/).pop() }))
          uploadedImages.value = [...uploadedImages.value, ...newImages]
          console.log('更新后的uploadedImages:', uploadedImages.value)
          
          ElMessage.success(`已从文件夹添加 ${images.length} 张图片`)
          
          // 自动预览第一张新添加的图片
          if (newImages.length > 0) {
            selectImageForPreview(newImages[0])
          }
        } else {
          ElMessage.warning('该文件夹下没有图片文件')
        }
      } else {
        ElMessage.warning('未能获取文件夹中的图片')
      }
    } else {
      console.log('用户取消选择文件夹')
    }
  } catch (error) {
    console.error('选择文件夹失败:', error)
    ElMessage.error('选择文件夹失败: ' + (error.message || error.toString()))
  }
}

// 选择预览图片
const selectImageForPreview = (img) => {
  watermarkStore.setCurrentImage(img)
}

// 统一的处理方法
const handleProcessAll = async () => {
  console.log('=== 开始处理图片 ===')
  console.log('已上传图片数量:', uploadedImages.value.length)
  console.log('输出路径:', outputPath.value)
  
  // 验证前置条件
  if (!window.javaApi) {
    ElMessage.error('JavaFX API 未就绪，请刷新页面重试')
    return
  }
  
  if (uploadedImages.value.length === 0) {
    ElMessage.error('请先上传图片')
    return
  }
  
  if (!outputPath.value) {
    ElMessage.error('请先选择输出路径')
    return
  }
  
  // 验证水印图片
  const config = { ...watermarkConfig }
  if (config.type === 'IMAGE' && !config.imagePath) {
    ElMessage.error('请先选择水印图片')
    return
  }
  
  appStore.setProcessing(true)
  
  try {
    if (uploadedImages.value.length === 1) {
      // 单张图片处理
      await processSingleImage(uploadedImages.value[0])
    } else {
      // 批量处理
      await processBatchImages()
    }
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败: ' + (error.message || error.toString()))
  } finally {
    appStore.setProcessing(false)
  }
}

// 处理单张图片
const processSingleImage = async (imageInfo) => {
  console.log('处理单张图片:', imageInfo)
  
  const config = { ...watermarkConfig }
  if (outputPath.value) {
    config.outputPath = outputPath.value.replace(/\\/g, '/')
  }
  if (config.imagePath) {
    config.imagePath = config.imagePath.replace(/\\/g, '/')
  }
  
  // 添加输出配置
  config.outputConfig = { ...outputConfig.value }
  
  const configJson = JSON.stringify(config)
  console.log('单张图片处理配置:', configJson)
  
  ElMessage.info('开始处理图片，请稍候...')
  
  const result = await window.javaApi.processImage(imageInfo.path, configJson)
  console.log('单张图片处理结果:', result)
  
  if (result && result.success !== false) {
    ElMessage.success('图片处理完成！输出路径: ' + (result.outputPath || result))
  } else {
    ElMessage.error('图片处理失败: ' + (result.message || '未知错误'))
  }
}

// 批量处理图片
const processBatchImages = async () => {
  console.log('批量处理图片，数量:', uploadedImages.value.length)
  
  const config = { ...watermarkConfig }
  if (config.imagePath) {
    config.imagePath = config.imagePath.replace(/\\/g, '/')
  }
  
  // 添加输出配置
  config.outputConfig = { ...outputConfig.value }
  
  const imagePaths = uploadedImages.value.map(img => img.path)
  const imagePathsJson = JSON.stringify(imagePaths)
  const configJson = JSON.stringify(config)
  const outputDirectory = outputPath.value.replace(/\\/g, '/')
  
  console.log('批量处理参数:')
  console.log('- 图片路径列表:', imagePathsJson)
  console.log('- 配置JSON:', configJson)
  console.log('- 输出目录:', outputDirectory)
  
  if (typeof window.javaApi.batchProcessImageList !== 'function') {
    ElMessage.error('批量处理API不可用')
    return
  }
  
  ElMessage.info(`开始批量处理 ${imagePaths.length} 张图片，请稍候...`)
  
  const result = await window.javaApi.batchProcessImageList(imagePathsJson, configJson, outputDirectory)
  console.log('批量处理结果:', result)
  
  if (result) {
    try {
      const resultObj = typeof result === 'string' ? JSON.parse(result) : result
      
      if (resultObj.success) {
        const successCount = resultObj.successCount || 0
        const failureCount = resultObj.failureCount || 0
        
        if (failureCount === 0) {
          ElMessage.success(`批量处理完成！成功处理 ${successCount} 张图片`)
        } else {
          ElMessage.warning(`批量处理完成！成功 ${successCount} 张，失败 ${failureCount} 张`)
        }
      } else {
        ElMessage.error('批量处理失败: ' + (resultObj.message || '未知错误'))
      }
    } catch (parseError) {
      console.warn('解析结果失败，使用原始结果:', parseError)
      ElMessage.success('批量处理完成！')
    }
  } else {
    ElMessage.error('批量处理失败: 未收到处理结果')
  }
}

const watermarkStore = useWatermarkStore()
const appStore = useAppStore()

// 使用computed或直接使用store，不要解构
const watermarkConfig = watermarkStore.watermarkConfig
const outputPath = ref('')

// 输出配置
const outputConfig = ref({
  namingRule: 'suffix', // original, prefix, suffix, custom
  filePrefix: 'wm_',
  fileSuffix: '_watermarked'
})

// 预览状态管理
const previewState = ref({
  scale: 1.0,        // 缩放比例
  offsetX: 0,        // X轴偏移
  offsetY: 0,        // Y轴偏移
  isDragging: false, // 是否正在拖拽预览
  dragStartX: 0,     // 拖拽起始X
  dragStartY: 0,     // 拖拽起始Y
  dragStartOffsetX: 0, // 拖拽起始偏移X
  dragStartOffsetY: 0  // 拖拽起始偏移Y
})

// 水印拖拽状态
const watermarkDragState = ref({
  isDragging: false,  // 是否正在拖拽水印
  dragStartX: 0,      // 拖拽起始X
  dragStartY: 0,      // 拖拽起始Y
  watermarkX: 0,      // 水印X坐标
  watermarkY: 0,      // 水印Y坐标
  offsetX: 0,         // 鼠标相对于水印的X偏移
  offsetY: 0          // 鼠标相对于水印的Y偏移
})

// 预览相关的DOM引用
const previewContainer = ref(null)
const previewImage = ref(null)

// 是否显示水印预览
const showWatermarkPreview = computed(() => {
  return watermarkStore.imagePreviewUrl && 
         (watermarkConfig.text || watermarkConfig.imagePath)
})

// 水印覆盖层样式
const watermarkOverlayStyle = computed(() => {
  if (!previewImage.value) return {}
  
  const position = watermarkConfig.position || 'BOTTOM_RIGHT'
  const imgRect = previewImage.value.getBoundingClientRect()
  const imgNaturalWidth = previewImage.value.naturalWidth
  const imgNaturalHeight = previewImage.value.naturalHeight
  const imgDisplayWidth = previewImage.value.offsetWidth
  const imgDisplayHeight = previewImage.value.offsetHeight
  
  // 计算缩放比例
  const scaleRatio = Math.min(imgDisplayWidth / imgNaturalWidth, imgDisplayHeight / imgNaturalHeight)
  
  // 估算水印尺寸（用于对齐计算）
  const fontSize = watermarkConfig.fontSize || 24
  const text = watermarkConfig.text || '水印'
  const estimatedWatermarkWidth = text.length * fontSize * 0.7 + 24 // 大致估算宽度
  const estimatedWatermarkHeight = fontSize + 16 // 大致估算高度
  
  // 根据位置计算水印坐标
  let x = 20, y = 20
  const padding = 20
  
  switch (position) {
    case 'TOP_LEFT':
      x = padding
      y = padding
      break
    case 'TOP_CENTER':
      x = (imgDisplayWidth - estimatedWatermarkWidth) / 2
      y = padding
      break
    case 'TOP_RIGHT':
      x = imgDisplayWidth - estimatedWatermarkWidth - padding
      y = padding
      break
    case 'MIDDLE_LEFT':
      x = padding
      y = (imgDisplayHeight - estimatedWatermarkHeight) / 2
      break
    case 'MIDDLE_CENTER':
    case 'CENTER':
      x = (imgDisplayWidth - estimatedWatermarkWidth) / 2
      y = (imgDisplayHeight - estimatedWatermarkHeight) / 2
      break
    case 'MIDDLE_RIGHT':
      x = imgDisplayWidth - estimatedWatermarkWidth - padding
      y = (imgDisplayHeight - estimatedWatermarkHeight) / 2
      break
    case 'BOTTOM_LEFT':
      x = padding
      y = imgDisplayHeight - estimatedWatermarkHeight - padding
      break
    case 'BOTTOM_CENTER':
      x = (imgDisplayWidth - estimatedWatermarkWidth) / 2
      y = imgDisplayHeight - estimatedWatermarkHeight - padding
      break
    case 'BOTTOM_RIGHT':
      x = imgDisplayWidth - estimatedWatermarkWidth - padding
      y = imgDisplayHeight - estimatedWatermarkHeight - padding
      break
    case 'CUSTOM':
      // 自定义位置：将后端的offsetX/offsetY转换为显示坐标
      if (imgNaturalWidth && imgNaturalHeight && imgDisplayWidth && imgDisplayHeight) {
        const scaleX = imgDisplayWidth / imgNaturalWidth
        const scaleY = imgDisplayHeight / imgNaturalHeight
        x = (watermarkConfig.offsetX || 0) * scaleX
        y = (watermarkConfig.offsetY || 0) * scaleY
      }
      break
    default:
      x = imgDisplayWidth - estimatedWatermarkWidth - padding
      y = imgDisplayHeight - estimatedWatermarkHeight - padding
      break
  }
  
  // 如果正在拖拽，优先使用拖拽位置
  if (watermarkDragState.value.isDragging || 
      (position === 'CUSTOM' && (watermarkDragState.value.watermarkX !== 0 || watermarkDragState.value.watermarkY !== 0))) {
    x = watermarkDragState.value.watermarkX
    y = watermarkDragState.value.watermarkY
  }
  
  return {
    position: 'absolute',
    left: `${x}px`,
    top: `${y}px`,
    cursor: 'move',
    userSelect: 'none',
    zIndex: 10
  }
})

// 水印内容样式
const watermarkContentStyle = computed(() => {
  const fontSize = watermarkConfig.fontSize || 24
  const fontColor = watermarkConfig.fontColor || '#FFFFFF'
  const opacity = watermarkConfig.opacity || 0.7
  const rotation = watermarkConfig.rotation || 0
  
  return {
    fontSize: `${fontSize}px`,
    color: fontColor,
    opacity: opacity,
    transform: `rotate(${rotation}deg)`,
    fontWeight: 'bold',
    textShadow: '2px 2px 4px rgba(0,0,0,0.5)',
    padding: '8px 12px',
    border: '2px dashed rgba(255,255,255,0.5)',
    borderRadius: '4px',
    backgroundColor: 'rgba(0,0,0,0.1)',
    minWidth: '60px',
    textAlign: 'center'
  }
})

// ==================== 预览交互方法 ====================

// 重置缩放和位置
const resetZoom = () => {
  previewState.value.scale = 1.0
  previewState.value.offsetX = 0
  previewState.value.offsetY = 0
}

// 放大
const zoomIn = () => {
  previewState.value.scale = Math.min(previewState.value.scale * 1.2, 5.0)
}

// 缩小
const zoomOut = () => {
  previewState.value.scale = Math.max(previewState.value.scale / 1.2, 0.1)
}

// 鼠标滚轮缩放（节流处理）
let wheelTimeout = null
const handleWheel = (event) => {
  event.preventDefault()
  
  if (wheelTimeout) return
  wheelTimeout = setTimeout(() => {
    wheelTimeout = null
  }, 16) // 约60fps
  
  const delta = event.deltaY > 0 ? 0.9 : 1.1
  previewState.value.scale = Math.max(0.1, Math.min(5.0, previewState.value.scale * delta))
}

// 预览区域鼠标按下
const handlePreviewMouseDown = (event) => {
  if (event.target.closest('.watermark-overlay')) return // 不处理水印区域的事件
  
  previewState.value.isDragging = true
  previewState.value.dragStartX = event.clientX
  previewState.value.dragStartY = event.clientY
  previewState.value.dragStartOffsetX = previewState.value.offsetX
  previewState.value.dragStartOffsetY = previewState.value.offsetY
  event.preventDefault()
}

// 预览区域鼠标移动
const handlePreviewMouseMove = (event) => {
  if (previewState.value.isDragging) {
    const deltaX = event.clientX - previewState.value.dragStartX
    const deltaY = event.clientY - previewState.value.dragStartY
    previewState.value.offsetX = previewState.value.dragStartOffsetX + deltaX
    previewState.value.offsetY = previewState.value.dragStartOffsetY + deltaY
  }
}

// 预览区域鼠标抬起
const handlePreviewMouseUp = () => {
  previewState.value.isDragging = false
}

// 水印鼠标按下
const handleWatermarkMouseDown = (event) => {
  
  watermarkDragState.value.isDragging = true
  watermarkDragState.value.dragStartX = event.clientX
  watermarkDragState.value.dragStartY = event.clientY
  
  // 获取图片容器的位置信息
  if (!previewImage.value) return
  
  const imgRect = previewImage.value.getBoundingClientRect()
  const watermarkElement = event.currentTarget
  const watermarkRect = watermarkElement.getBoundingClientRect()
  
  // 计算相对于图片的位置（不考虑缩放和偏移）
  watermarkDragState.value.watermarkX = watermarkRect.left - imgRect.left
  watermarkDragState.value.watermarkY = watermarkRect.top - imgRect.top
  
  // 记录鼠标相对于水印的偏移
  watermarkDragState.value.offsetX = event.clientX - watermarkRect.left
  watermarkDragState.value.offsetY = event.clientY - watermarkRect.top
  
  event.preventDefault()
  event.stopPropagation()
  
  // 添加全局鼠标事件监听
  document.addEventListener('mousemove', handleWatermarkMouseMove)
  document.addEventListener('mouseup', handleWatermarkMouseUp)
}

// 防抖函数用于优化水印位置更新
let updatePositionTimeout = null
const debouncedUpdateWatermarkPosition = () => {
  if (updatePositionTimeout) {
    clearTimeout(updatePositionTimeout)
  }
  updatePositionTimeout = setTimeout(() => {
    updateWatermarkPosition()
  }, 16) // 60fps
}

// 水印鼠标移动
const handleWatermarkMouseMove = (event) => {
  if (!watermarkDragState.value.isDragging || !previewImage.value) return
  
  // 获取图片位置信息
  const imgRect = previewImage.value.getBoundingClientRect()
  
  // 计算鼠标相对于图片的位置
  const mouseX = event.clientX - imgRect.left - watermarkDragState.value.offsetX
  const mouseY = event.clientY - imgRect.top - watermarkDragState.value.offsetY
  
  // 限制在图片范围内
  const imgWidth = imgRect.width
  const imgHeight = imgRect.height
  
  watermarkDragState.value.watermarkX = Math.max(0, Math.min(mouseX, imgWidth - 60)) // 60是水印大概宽度
  watermarkDragState.value.watermarkY = Math.max(0, Math.min(mouseY, imgHeight - 30)) // 30是水印大概高度
  
  // 使用防抖处理位置更新
  debouncedUpdateWatermarkPosition()
  
  event.preventDefault()
}

// 水印鼠标抬起
const handleWatermarkMouseUp = () => {
  watermarkDragState.value.isDragging = false
  
  // 移除全局事件监听
  document.removeEventListener('mousemove', handleWatermarkMouseMove)
  document.removeEventListener('mouseup', handleWatermarkMouseUp)
}

// 根据拖拽位置更新水印position配置
const updateWatermarkPosition = () => {
  if (!previewImage.value) return
  
  // 拖拽时使用精确位置模式
  watermarkConfig.position = 'CUSTOM'
  
  // 计算相对于图片的精确坐标
  const imgRect = previewImage.value.getBoundingClientRect()
  const imgNaturalWidth = previewImage.value.naturalWidth
  const imgNaturalHeight = previewImage.value.naturalHeight
  
  // 计算显示尺寸与实际尺寸的比例
  const scaleX = imgNaturalWidth / imgRect.width
  const scaleY = imgNaturalHeight / imgRect.height
  
  // 将显示坐标转换为实际图片坐标
  watermarkConfig.offsetX = Math.round(watermarkDragState.value.watermarkX * scaleX)
  watermarkConfig.offsetY = Math.round(watermarkDragState.value.watermarkY * scaleY)
  
  // 调试信息（开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log('更新精确位置:', {
      displayCoords: { x: watermarkDragState.value.watermarkX, y: watermarkDragState.value.watermarkY },
      actualCoords: { x: watermarkConfig.offsetX, y: watermarkConfig.offsetY }
    })
  }
}

// 生成文件名预览
const getFileNamePreview = () => {
  const sampleName = '示例图片.jpg'
  const baseName = sampleName.substring(0, sampleName.lastIndexOf('.'))
  const extension = sampleName.substring(sampleName.lastIndexOf('.'))
  
  switch (outputConfig.value.namingRule) {
    case 'original':
      return sampleName
    case 'prefix':
      return (outputConfig.value.filePrefix || '') + sampleName
    case 'suffix':
      return baseName + (outputConfig.value.fileSuffix || '') + extension
    case 'custom':
      return (outputConfig.value.filePrefix || '') + baseName + (outputConfig.value.fileSuffix || '') + extension
    default:
      return sampleName
  }
}

// 安全地获取响应式属性
const getImagePreviewUrl = () => {
  return watermarkStore.imagePreviewUrl || ''
}

const getCurrentImage = () => {
  return watermarkStore.currentImage || null
}

// 组件挂载时初始化store并恢复状态
onMounted(() => {
  console.log('WatermarkEditor 组件挂载')
  
  // 初始化store
  watermarkStore.initStore()
  
  // 输出当前状态
  console.log('恢复后的当前图片:', watermarkStore.currentImage)
  console.log('恢复后的预览URL:', watermarkStore.imagePreviewUrl)
  
  // 监听图片状态变化
  watch(() => watermarkStore.currentImage, (newVal, oldVal) => {
    console.log('当前图片状态变化:', oldVal, '->', newVal)
    // 重置水印拖拽状态
    watermarkDragState.value.watermarkX = 0
    watermarkDragState.value.watermarkY = 0
  }, { deep: true })
  
  watch(() => watermarkStore.imagePreviewUrl, (newVal, oldVal) => {
    console.log('图片预览URL变化:', oldVal, '->', newVal)
    // 重置预览状态
    resetZoom()
  })
  
  // 监听水印配置的关键属性变化
  watch([() => watermarkConfig.text, () => watermarkConfig.fontSize, () => watermarkConfig.fontColor, () => watermarkConfig.opacity, () => watermarkConfig.rotation], () => {
    // 水印配置变化，实时预览更新
  })
  
  // 监听position变化，仅在选择预设位置时重置拖拽状态
  watch(() => watermarkConfig.position, (newPosition) => {
    // 只有当选择的是非自定义位置时，才重置拖拽状态
    if (newPosition !== 'CUSTOM') {
      watermarkDragState.value.watermarkX = 0
      watermarkDragState.value.watermarkY = 0
    }
  })
})

// 选择输出路径
const selectOutputPath = async () => {
  try {
    console.log('选择输出路径...')
    if (window.javaApi?.selectDirectory) {
      const path = await window.javaApi.selectDirectory()
      if (path) {
        outputPath.value = path
        ElMessage.success(`已选择输出路径：${path}`)
        console.log('输出路径设置为:', path)
      } else {
        console.log('未选择输出路径')
      }
    } else {
      ElMessage.warning('目录选择功能未就绪')
      console.error('window.javaApi.selectDirectory 方法不可用')
    }
  } catch (error) {
    console.error('选择输出路径错误:', error)
    ElMessage.error('选择输出路径失败')
  }
}

// 图片加载成功回调
const onImageLoad = () => {
  console.log('图片加载成功:', watermarkStore.imagePreviewUrl)
}

// 图片加载失败回调
const onImageError = (event) => {
  console.error('图片加载失败:', event, watermarkStore.imagePreviewUrl)
  ElMessage.warning('图片预览加载失败')
}

// JavaFX文件选择：原图
const selectBaseImage = async () => {
  try {
    console.log('开始选择图片...')
    console.log('window.javaApi:', window.javaApi)
    
    if (!window.javaApi) {
      ElMessage.error('JavaFX API 未就绪')
      return
    }
    
    if (typeof window.javaApi.selectImage !== 'function') {
      ElMessage.error('selectImage 方法不可用')
      return
    }
    
    console.log('调用 JavaFX 文件选择对话框')
    const path = await window.javaApi.selectImage()
    console.log('API返回的原始路径:', path, typeof path)
    
    // 更严格的路径验证
    if (path && path !== 'null' && path !== 'undefined' && typeof path === 'string' && path.trim().length > 0) {
      const trimmedPath = path.trim()
      console.log('处理后的路径:', trimmedPath)
      
      // 创建图片信息对象
      const imageInfo = { path: trimmedPath }
      console.log('创建的imageInfo:', imageInfo)
      
      // 调用store方法设置图片
      watermarkStore.setCurrentImage(imageInfo)
      
      // 使用nextTick确保响应式更新在DOM更新后执行
      await nextTick()
      
      // 等待store中的异步URL设置完成
      setTimeout(async () => {
        // 验证状态是否正确设置
        console.log('设置后的currentImage:', watermarkStore.currentImage)
        console.log('设置后的imagePreviewUrl:', watermarkStore.imagePreviewUrl)
        
        if (watermarkStore.currentImage && watermarkStore.imagePreviewUrl) {
          ElMessage.success('图片选择成功')
          
          // 强制触发Vue重新渲染
          await nextTick()
          
          console.log('Vue响应式更新完成')
        } else {
          console.error('图片状态设置失败')
          ElMessage.error('图片状态设置失败')
        }
      }, 50)
    } else {
      console.log('无效的文件路径:', path)
      ElMessage.info('未选择图片或路径无效')
    }
  } catch (error) {
    console.error('选择图片失败:', error)
    ElMessage.error('选择图片失败: ' + (error.message || error))
  }
}
// JavaFX文件选择：水印图片
const selectWatermarkImage = async () => {
  try {
    if (window.javaApi?.selectImage) {
      console.log('调用 JavaFX 文件选择对话框（水印图片）')
      const path = await window.javaApi.selectImage()
      console.log('选择的水印图片路径:', path)
      if (path) {
        watermarkConfig.imagePath = path
        ElMessage.success('水印图片选择成功')
      } else {
        ElMessage.info('未选择水印图片')
      }
    } else {
      ElMessage.warning('JavaFX API 未就绪，请稍后重试')
    }
  } catch (error) {
    console.error('选择水印图片失败:', error)
    ElMessage.error('选择水印图片失败: ' + error.message)
  }
}

const toFileUrl = (p) => {
  if (!p) return ''
  if (p.startsWith('file://')) return p
  const norm = p.replace(/\\/g, '/').replace(/^([A-Za-z]):\//, '/$1:/')
  return `file:///${norm.replace(/^\/+/, '')}`
}

const handleProcess = async () => {
  console.log('=== 开始处理图片 ===')
  console.log('watermarkStore.currentImage:', watermarkStore.currentImage)
  console.log('watermarkStore.imagePreviewUrl:', watermarkStore.imagePreviewUrl)
  console.log('outputPath.value:', outputPath.value)
  console.log('window.javaApi:', window.javaApi)
  
  // localStorage检查
  try {
    const savedImage = localStorage.getItem('currentImage')
    const savedUrl = localStorage.getItem('imagePreviewUrl')
    console.log('localStorage中的currentImage:', savedImage)
    console.log('localStorage中的imagePreviewUrl:', savedUrl)
  } catch (e) {
    console.warn('无法读取localStorage:', e)
  }
  
  // 验证前置条件
  if (!window.javaApi) {
    ElMessage.error('JavaFX API 未就绪，请刷新页面重试')
    return
  }
  
  // 检查图片状态 - 安全地从 store 获取图片信息
  let imageInfo = watermarkStore.currentImage
  
  // 如果 store 中没有图片信息，尝试从 localStorage 恢复
  if (!imageInfo || !imageInfo.path) {
    try {
      const savedImage = localStorage.getItem('currentImage')
      if (savedImage) {
        imageInfo = JSON.parse(savedImage)
        console.log('从 localStorage 恢复的图片信息:', imageInfo)
        // 更新 store 状态
        watermarkStore.setCurrentImage(imageInfo)
      }
    } catch (e) {
      console.warn('无法从 localStorage 恢复图片信息:', e)
    }
  }
  
  // 最终验证
  if (!imageInfo || !imageInfo.path) {
    console.error('图片验证失败 - imageInfo:', imageInfo)
    ElMessage.error('请先选择要处理的图片')
    return
  }
  
  console.log('图片验证成功，开始处理...')
  
  appStore.setProcessing(true)
  
  try {
    const config = { ...watermarkConfig }
    
    // 如果设置了输出路径，则添加到配置中
    if (outputPath.value) {
      config.outputPath = outputPath.value
    }
    
    console.log('处理配置:', config)
    
    // 验证水印图片
    if (config.type === 'IMAGE' && !config.imagePath) {
      ElMessage.error('请先选择水印图片')
      return
    }
    
    // 使用验证通过的imageInfo中的路径
    const imagePath = imageInfo.path
    console.log('使用的图片路径:', imagePath)
    
    if (!imagePath) {
      ElMessage.error('图片路径无效，请重新选择图片')
      return
    }
    
    ElMessage.info('开始处理图片，请稍候...')
    console.log('调用processImage API，图片路径:', imagePath)
    console.log('配置参数:', config)
    
    // 创建配置副本并处理路径字符串，避免过度转义
    const configForBackend = { ...config }
    if (configForBackend.outputPath) {
      // 将反斜杠转换为正斜杠，避免JSON转义问题
      configForBackend.outputPath = configForBackend.outputPath.replace(/\\/g, '/')
      console.log('处理后的输出路径:', configForBackend.outputPath)
    }
    if (configForBackend.imagePath) {
      // 同样处理水印图片路径
      configForBackend.imagePath = configForBackend.imagePath.replace(/\\/g, '/')
    }
    
    const configJson = JSON.stringify(configForBackend)
    console.log('配置JSON:', configJson)
    
    // 后端API检查
    if (typeof window.javaApi.processImage !== 'function') {
      console.error('processImage API 不可用')
      ElMessage.error('processImage API 不可用')
      return
    }
    
    // 后端接口需要两个参数：路径和配置JSON字符串
    const result = await window.javaApi.processImage(imagePath, configJson)
    console.log('处理结果:', result)
    
    if (result && result.success !== false) {
      ElMessage.success('图片处理完成！输出路径: ' + (result.outputPath || result))
    } else {
      ElMessage.error('图片处理失败: ' + (result.message || '未知错误'))
    }
    
  } catch (error) {
    console.error('处理图片时出错:', error)
    ElMessage.error('处理失败: ' + (error.message || error.toString()))
  } finally {
    appStore.setProcessing(false)
  }
}

// 批量处理图片
const handleBatchProcess = async () => {
  console.log('=== 开始批量处理图片 ===')
  console.log('批量图片数量:', batchImages.value.length)
  console.log('outputPath.value:', outputPath.value)
  
  // 验证前置条件
  if (!window.javaApi) {
    ElMessage.error('JavaFX API 未就绪，请刷新页面重试')
    return
  }
  
  if (batchImages.value.length === 0) {
    ElMessage.error('请先批量上传图片')
    return
  }
  
  if (!outputPath.value) {
    ElMessage.error('请先选择输出路径')
    return
  }
  
  // 验证水印图片
  const config = { ...watermarkConfig }
  if (config.type === 'IMAGE' && !config.imagePath) {
    ElMessage.error('请先选择水印图片')
    return
  }
  
  appStore.setProcessing(true)
  
  try {
    // 获取所有图片路径
    const imagePaths = batchImages.value.map(img => img.path)
    
    // 创建配置副本并处理路径字符串
    const configForBackend = { ...config }
    if (configForBackend.outputPath) {
      configForBackend.outputPath = configForBackend.outputPath.replace(/\\/g, '/')
    }
    if (configForBackend.imagePath) {
      configForBackend.imagePath = configForBackend.imagePath.replace(/\\/g, '/')
    }
    
    const imagePathsJson = JSON.stringify(imagePaths)
    const configJson = JSON.stringify(configForBackend)
    const outputDirectory = outputPath.value.replace(/\\/g, '/')
    
    console.log('批量处理参数:')
    console.log('- 图片路径列表:', imagePathsJson)
    console.log('- 配置JSON:', configJson)
    console.log('- 输出目录:', outputDirectory)
    
    // 后端API检查
    if (typeof window.javaApi.batchProcessImageList !== 'function') {
      console.error('batchProcessImageList API 不可用')
      ElMessage.error('批量处理API不可用')
      return
    }
    
    ElMessage.info(`开始批量处理 ${imagePaths.length} 张图片，请稍候...`)
    
    // 调用后端批量处理API
    const result = await window.javaApi.batchProcessImageList(imagePathsJson, configJson, outputDirectory)
    console.log('批量处理结果:', result)
    
    if (result) {
      try {
        const resultObj = typeof result === 'string' ? JSON.parse(result) : result
        
        if (resultObj.success) {
          const successCount = resultObj.successCount || 0
          const failureCount = resultObj.failureCount || 0
          const total = resultObj.total || imagePaths.length
          
          if (failureCount === 0) {
            ElMessage.success(`批量处理完成！成功处理 ${successCount} 张图片`)
          } else {
            ElMessage.warning(`批量处理完成！成功 ${successCount} 张，失败 ${failureCount} 张`)
          }
        } else {
          ElMessage.error('批量处理失败: ' + (resultObj.message || '未知错误'))
        }
      } catch (parseError) {
        console.warn('解析结果失败，使用原始结果:', parseError)
        ElMessage.success('批量处理完成！')
      }
    } else {
      ElMessage.error('批量处理失败: 未收到处理结果')
    }
    
  } catch (error) {
    console.error('批量处理图片时出错:', error)
    ElMessage.error('批量处理失败: ' + (error.message || error.toString()))
  } finally {
    appStore.setProcessing(false)
  }
}

const resetConfig = () => {
  watermarkStore.resetWatermarkConfig()
}

// 清除所有状态
const clearAll = () => {
  watermarkStore.resetAll()
  uploadedImages.value = []
  outputPath.value = ''
  ElMessage.success('已清除所有状态')
  console.log('用户手动清除所有状态')
}
</script>

<style scoped>
.watermark-editor {
  max-width: 1200px;
  margin: 0 auto;
}
.editor-card {
  margin-top: 20px;
}
.image-uploader {
  margin-bottom: 20px;
}

/* 交互式预览容器 */
.interactive-preview-container {
  margin-top: 10px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
  overflow: hidden;
}

.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.zoom-info {
  font-size: 12px;
  color: #666;
  font-weight: bold;
}

.interactive-preview {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
  cursor: grab;
  background: 
    radial-gradient(circle, #ccc 1px, transparent 1px);
  background-size: 20px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.interactive-preview:active {
  cursor: grabbing;
}

.preview-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.preview-image-container {
  position: relative;
  display: inline-block;
}

/* 水印覆盖层 */
.watermark-overlay {
  position: absolute;
  cursor: move;
  user-select: none;
  z-index: 10;
  transition: all 0.1s ease;
}

.watermark-overlay:hover {
  transform: scale(1.02);
}

.watermark-content {
  pointer-events: none;
  white-space: nowrap;
}

/* 拖拽控制点 */
.drag-handles {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.drag-handle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #409eff;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.drag-handle-center {
  top: -4px;
  left: -4px;
}

.preview-info {
  padding: 8px 12px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #666;
}

.preview-info p {
  margin: 2px 0;
}

/* 兼容原有样式 */
.image-preview {
  margin-top: 10px;
  border: 1px solid #eee;
  padding: 10px;
  background: #fafafa;
  text-align: center;
}
.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}
</style>