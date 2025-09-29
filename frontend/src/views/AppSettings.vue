<template>
  <div class="app-settings">
    <el-card shadow="hover">
      <h2>应用设置</h2>
      <el-form :model="appStore.settings" label-width="120px">
        <el-form-item label="默认输出目录">
          <el-input v-model="appStore.settings.defaultOutputDir" />
        </el-form-item>
        <el-form-item label="默认图片质量">
          <el-input-number v-model="appStore.settings.defaultImageQuality" :min="10" :max="100" />
        </el-form-item>
        <el-form-item label="自动清理天数">
          <el-input-number v-model="appStore.settings.autoCleanupDays" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="主题">
          <el-select v-model="appStore.settings.theme">
            <el-option label="浅色" value="light" />
            <el-option label="深色" value="dark" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { useAppStore } from '../stores/app'
const appStore = useAppStore()

const saveSettings = async () => {
  const success = await appStore.saveSettings()
  if (success) {
    ElMessage.success('设置已保存')
  } else {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.app-settings {
  max-width: 600px;
  margin: 0 auto;
  margin-top: 20px;
}
</style>