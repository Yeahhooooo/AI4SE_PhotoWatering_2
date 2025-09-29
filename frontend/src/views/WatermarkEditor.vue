<template>
  <div class="watermark-editor">
    <el-card shadow="hover" class="editor-card">
      <h2>水印编辑</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <!-- 图片选择与预览 -->
          <el-button type="primary" @click="selectBaseImage">选择图片</el-button>
          <div v-if="imagePreviewUrl" class="image-preview">
            <img :src="imagePreviewUrl" alt="预览" />
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
            </template>
            <el-form-item label="位置">
              <el-select v-model="watermarkConfig.position">
                <el-option label="左上" value="TOP_LEFT" />
                <el-option label="右上" value="TOP_RIGHT" />
                <el-option label="左下" value="BOTTOM_LEFT" />
                <el-option label="右下" value="BOTTOM_RIGHT" />
                <el-option label="居中" value="CENTER" />
              </el-select>
            </el-form-item>
            <el-form-item label="透明度">
              <el-slider v-model="watermarkConfig.opacity" :min="0" :max="1" :step="0.01" />
            </el-form-item>
            <el-form-item label="旋转角度">
              <el-input-number v-model="watermarkConfig.rotation" :min="0" :max="360" />
            </el-form-item>
            <el-form-item label="缩放比例">
              <el-input-number v-model="watermarkConfig.scale" :min="0.1" :max="5" :step="0.1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleProcess">处理图片</el-button>
              <el-button @click="resetConfig">重置配置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useWatermarkStore } from '../stores/watermark'
import { useAppStore } from '../stores/app'

const watermarkStore = useWatermarkStore()
const appStore = useAppStore()

const watermarkConfig = watermarkStore.watermarkConfig
const imagePreviewUrl = watermarkStore.imagePreviewUrl

// JavaFX文件选择：原图
const selectBaseImage = async () => {
  if (window.javaApi?.selectImage) {
    const path = await window.javaApi.selectImage()
    if (path) {
      watermarkStore.setCurrentImage({ path })
    }
  } else {
    ElMessage.warning('非JavaFX环境，使用浏览器文件选择')
  }
}
// JavaFX文件选择：水印图片
const selectWatermarkImage = async () => {
  if (window.javaApi?.selectImage) {
    const path = await window.javaApi.selectImage()
    if (path) {
      watermarkConfig.imagePath = path
    }
  }
}

const toFileUrl = (p) => {
  if (!p) return ''
  if (p.startsWith('file://')) return p
  const norm = p.replace(/\\/g, '/').replace(/^([A-Za-z]):\//, '/$1:/')
  return `file:///${norm.replace(/^\/+/, '')}`
}

const handleProcess = async () => {
  appStore.setProcessing(true)
  // 处理图片时传递真实文件路径（去掉 file:/// 前缀）
  if (window.javaApi && imagePreviewUrl) {
    try {
      const config = { ...watermarkConfig }
      if (config.type === 'IMAGE' && !config.imagePath) {
        ElMessage.error('请先选择水印图片')
        appStore.setProcessing(false)
        return
      }
      const toFsPath = (url) => url.replace(/^file:\/\//, '').replace(/^\//, '').replace(/\//g, '\\')
      const basePath = toFsPath(imagePreviewUrl)
      await window.javaApi.processImage(basePath, config)
      ElMessage.success('图片处理完成！')
    } catch (e) {
      ElMessage.error('处理失败：' + e.message)
    } finally {
      appStore.setProcessing(false)
    }
  }
}

const resetConfig = () => {
  watermarkStore.resetWatermarkConfig()
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