<template>
  <div class="template-manager">
    <el-card shadow="hover">
      <h2>模板管理</h2>
      <el-table :data="appStore.templates" style="width: 100%">
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="applyTemplate(scope.row)">应用</el-button>
            <el-button size="small" type="danger" @click="deleteTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider />
      <el-form :model="newTemplate" label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="newTemplate.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="newTemplate.type">
            <el-option label="文本水印" value="TEXT" />
            <el-option label="图片水印" value="IMAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置JSON">
          <el-input type="textarea" v-model="newTemplate.config" rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveTemplate">保存模板</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAppStore } from '../stores/app'
import { useWatermarkStore } from '../stores/watermark'

const appStore = useAppStore()
const watermarkStore = useWatermarkStore()

const newTemplate = ref({
  name: '',
  type: 'TEXT',
  config: ''
})

const saveTemplate = async () => {
  if (!newTemplate.value.name || !newTemplate.value.config) {
    ElMessage.error('请填写模板名称和配置')
    return
  }
  try {
    await window.javaApi.saveTemplate(newTemplate.value)
    ElMessage.success('模板保存成功')
    appStore.loadTemplates()
  } catch (e) {
    ElMessage.error('保存失败：' + e.message)
  }
}

const applyTemplate = (template) => {
  watermarkStore.applyTemplate(template)
  ElMessage.success('模板已应用')
}

const deleteTemplate = async (template) => {
  // 这里可以调用后端删除接口
  ElMessage.info('删除功能待实现')
}
</script>

<style scoped>
.template-manager {
  max-width: 900px;
  margin: 0 auto;
  margin-top: 20px;
}
</style>