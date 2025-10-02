<template>
  <div class="template-manager">
    <el-card shadow="hover">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>模板管理</h2>
        <div>
          <el-button type="primary" @click="showCreateDialog" icon="Plus">新建模板</el-button>
          <el-button @click="goBack" icon="Back">返回</el-button>
        </div>
      </div>

      <!-- 模板列表 -->
      <div style="margin-bottom: 10px;">
        <el-text type="info" size="small">当前共有 {{ appStore.templates.length }} 个模板</el-text>
        <el-button type="primary" size="small" @click="refreshTemplates" style="margin-left: 10px;">刷新</el-button>
      </div>
      <el-table :data="appStore.templates" style="width: 100%" v-loading="appStore.loading">
        <el-table-column prop="name" label="模板名称" min-width="150">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <span>{{ scope.row.name }}</span>
              <el-tag v-if="isDefaultTemplate(scope.row)" type="success" size="small" style="margin-left: 8px;">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.config?.type === 'TEXT' ? 'primary' : 'warning'" size="small">
              {{ scope.row.config?.type === 'TEXT' ? '文本' : '图片' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button-group size="small">
              <el-button type="primary" @click="applyTemplate(scope.row)" icon="Check">应用</el-button>
              <el-button @click="showEditDialog(scope.row)" icon="Edit">编辑</el-button>
              <el-button 
                type="success" 
                v-if="!isDefaultTemplate(scope.row)"
                @click="setDefaultTemplate(scope.row)"
                icon="Star"
              >设为默认</el-button>
              <el-button 
                type="info" 
                v-else
                @click="unsetDefaultTemplate()"
                icon="StarFilled"
              >取消默认</el-button>
              <el-button type="danger" @click="deleteTemplate(scope.row)" icon="Delete">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!appStore.loading && appStore.templates.length === 0" description="暂无模板">
        <el-button type="primary" @click="showCreateDialog">创建第一个模板</el-button>
      </el-empty>
    </el-card>

    <!-- 新建/编辑模板对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑模板' : '新建模板'" 
      width="600px"
      :modal="true"
      :append-to-body="true"
      :destroy-on-close="false"
      center
      @close="resetForm"
    >
      <el-form :model="templateForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input 
            v-model="templateForm.description" 
            type="textarea" 
            rows="3" 
            placeholder="请输入模板描述（可选）" 
          />
        </el-form-item>
        <el-form-item label="水印类型" prop="type">
          <el-radio-group v-model="templateForm.type" @change="onTypeChange">
            <el-radio label="TEXT">文本水印</el-radio>
            <el-radio label="IMAGE">图片水印</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 文本水印配置 -->
        <template v-if="templateForm.type === 'TEXT'">
          <el-form-item label="文本内容" prop="text">
            <el-input v-model="templateForm.text" placeholder="请输入水印文本" />
          </el-form-item>
          <el-form-item label="字体大小">
            <el-input-number v-model="templateForm.fontSize" :min="12" :max="200" />
          </el-form-item>
          <el-form-item label="字体颜色">
            <el-color-picker v-model="templateForm.fontColor" />
          </el-form-item>
        </template>
        
        <!-- 图片水印配置 -->
        <template v-if="templateForm.type === 'IMAGE'">
          <el-form-item label="图片路径" prop="imagePath">
            <el-input v-model="templateForm.imagePath" placeholder="请选择水印图片">
              <template #append>
                <el-button @click="selectWatermarkImage">选择图片</el-button>
              </template>
            </el-input>
          </el-form-item>
        </template>
        
        <!-- 通用配置 -->
        <el-form-item label="位置">
          <el-select v-model="templateForm.position" style="width: 200px;">
            <el-option label="左上" value="TOP_LEFT" />
            <el-option label="上中" value="TOP_CENTER" />
            <el-option label="右上" value="TOP_RIGHT" />
            <el-option label="左中" value="CENTER_LEFT" />
            <el-option label="正中" value="CENTER" />
            <el-option label="右中" value="CENTER_RIGHT" />
            <el-option label="左下" value="BOTTOM_LEFT" />
            <el-option label="下中" value="BOTTOM_CENTER" />
            <el-option label="右下" value="BOTTOM_RIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="透明度">
          <el-slider v-model="templateForm.opacity" :min="0" :max="1" :step="0.1" style="width: 200px;" />
          <span style="margin-left: 10px;">{{ Math.round(templateForm.opacity * 100) }}%</span>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="templateForm.isDefault" />
          <el-text type="info" size="small" style="margin-left: 10px;">启动时自动应用此模板</el-text>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTemplate">{{ isEdit ? '更新' : '保存' }}</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '../stores/app'
import { useWatermarkStore } from '../stores/watermark'
import { useRouter } from 'vue-router'

const router = useRouter()
const appStore = useAppStore()
const watermarkStore = useWatermarkStore()

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentTemplate = ref(null)

// 表单引用
const formRef = ref(null)

// 模板表单
const templateForm = reactive({
  name: '',
  description: '',
  type: 'TEXT',
  text: '水印文本',
  fontSize: 24,
  fontColor: '#FFFFFF',
  imagePath: '',
  position: 'BOTTOM_RIGHT',
  opacity: 0.8,
  isDefault: false
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 50, message: '模板名称长度为1-50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择水印类型', trigger: 'change' }
  ],
  text: [
    { required: true, message: '请输入水印文本', trigger: 'blur', validator: validateText }
  ],
  imagePath: [
    { required: true, message: '请选择水印图片', trigger: 'blur', validator: validateImagePath }
  ]
}

// 自定义验证函数
function validateText(rule, value, callback) {
  if (templateForm.type === 'TEXT' && (!value || value.trim() === '')) {
    callback(new Error('请输入水印文本'))
  } else {
    callback()
  }
}

function validateImagePath(rule, value, callback) {
  if (templateForm.type === 'IMAGE' && (!value || value.trim() === '')) {
    callback(new Error('请选择水印图片'))
  } else {
    callback()
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  console.log('TemplateManager 组件挂载')
  console.log('当前appStore.templates:', appStore.templates)
  console.log('JavaAPI可用性:', !!window.javaApi)
  console.log('getAllTemplates方法可用性:', typeof window.javaApi?.getAllTemplates)
  
  await loadTemplates()
  
  console.log('挂载完成后的模板数量:', appStore.templates.length)
})

// ==================== 模板操作方法 ====================

// 加载模板列表
const loadTemplates = async () => {
  try {
    await appStore.loadTemplates()
    console.log('模板列表加载完成:', appStore.templates)
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  }
}

// 手动刷新模板列表
const refreshTemplates = async () => {
  console.log('手动刷新模板列表...')
  await loadTemplates()
  ElMessage.success('模板列表已刷新')
}

// 显示新建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (template) => {
  isEdit.value = true
  currentTemplate.value = template
  
  // 填充表单数据
  templateForm.name = template.name
  templateForm.description = template.description || ''
  templateForm.type = template.config?.type || 'TEXT'
  templateForm.text = template.config?.text || '水印文本'
  templateForm.fontSize = template.config?.fontSize || 24
  templateForm.fontColor = template.config?.fontColor || '#FFFFFF'
  templateForm.imagePath = template.config?.imagePath || ''
  templateForm.position = template.config?.position || 'BOTTOM_RIGHT'
  templateForm.opacity = template.config?.opacity || 0.8
  templateForm.isDefault = isDefaultTemplate(template)
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  templateForm.name = ''
  templateForm.description = ''
  templateForm.type = 'TEXT'
  templateForm.text = '水印文本'
  templateForm.fontSize = 24
  templateForm.fontColor = '#FFFFFF'
  templateForm.imagePath = ''
  templateForm.position = 'BOTTOM_RIGHT'
  templateForm.opacity = 0.8
  templateForm.isDefault = false
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 水印类型改变时的处理
const onTypeChange = (type) => {
  console.log('水印类型改变:', type)
  if (type === 'TEXT') {
    templateForm.imagePath = ''
  } else {
    templateForm.text = ''
  }
}

// 选择水印图片
const selectWatermarkImage = async () => {
  try {
    const result = await window.javaApi.selectSingleFile()
    if (result && result.path) {
      templateForm.imagePath = result.path
      ElMessage.success('图片选择成功')
    }
  } catch (error) {
    console.error('选择图片失败:', error)
    ElMessage.error('选择图片失败')
  }
}

// 保存模板
const saveTemplate = async () => {
  if (!formRef.value) return
  
  try {
    // 验证表单
    await formRef.value.validate()
    
    // 构建配置对象
    const config = {
      type: templateForm.type,
      position: templateForm.position,
      opacity: templateForm.opacity,
      offsetX: 10,
      offsetY: 10,
      rotation: 0,
      scale: 1.0
    }
    
    if (templateForm.type === 'TEXT') {
      Object.assign(config, {
        text: templateForm.text,
        fontSize: templateForm.fontSize,
        fontFamily: 'Microsoft YaHei',
        fontColor: templateForm.fontColor,
        fontStyle: 'NORMAL'
      })
    } else {
      Object.assign(config, {
        imagePath: templateForm.imagePath
      })
    }
    
    // 构建模板数据
    const templateData = {
      name: templateForm.name.trim(),
      description: templateForm.description?.trim() || '',
      config: config
    }
    
    // 如果是编辑模式，添加ID
    if (isEdit.value && currentTemplate.value) {
      templateData.id = currentTemplate.value.id
    }
    
    console.log('保存模板数据:', templateData)
    
    // 调用后端API
    const result = await window.javaApi.saveWatermarkTemplate(JSON.stringify(templateData))
    console.log('保存结果:', result)
    
    // 处理结果
    const response = typeof result === 'string' ? JSON.parse(result) : result
    if (response.error) {
      throw new Error(response.message || '保存失败')
    }
    
    ElMessage.success(isEdit.value ? '模板更新成功' : '模板保存成功')
    dialogVisible.value = false
    
    // 刷新模板列表
    await loadTemplates()
    
    // 如果设置为默认模板
    if (templateForm.isDefault) {
      localStorage.setItem('defaultTemplate', JSON.stringify(config))
      // 取消其他默认模板
      localStorage.removeItem('defaultTemplateId')
      localStorage.setItem('defaultTemplateId', response.id || templateData.id)
      ElMessage.info('已设置为默认模板')
    }
    
  } catch (error) {
    console.error('保存模板失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('保存模板失败')
    }
  }
}

// 应用模板
const applyTemplate = async (template) => {
  try {
    watermarkStore.applyTemplate(template)
    
    // 提取输出路径并通过路由状态传递
    const config = typeof template.config === 'string' 
      ? JSON.parse(template.config) 
      : template.config
    
    const routeState = {
      appliedTemplate: template.name
    }
    
    // 如果模板中有输出路径，添加到路由状态中
    if (config.outputPath) {
      routeState.outputPath = config.outputPath
    }
    
    ElMessage.success(`已应用模板: ${template.name}`)
    
    // 导航回编辑页面，传递状态信息
    router.push({
      path: '/watermark',
      state: routeState
    })
  } catch (error) {
    console.error('应用模板失败:', error)
    ElMessage.error('应用模板失败')
  }
}

// 预览模板

// 删除模板
const deleteTemplate = async (template) => {
  try {
    console.log('开始删除模板:', template.name, '(ID:', template.id, ')')
    
    // 检查API可用性
    if (!window.javaApi || typeof window.javaApi.deleteTemplate !== 'function') {
      throw new Error('删除模板API不可用')
    }
    
    const result = await window.javaApi.deleteTemplate(template.id)
    console.log('删除模板原始结果:', result, typeof result)
    
    // 处理结果
    let success = false
    if (typeof result === 'string') {
      if (result === 'success') {
        success = true
      } else {
        try {
          const response = JSON.parse(result)
          success = !response.error
        } catch (e) {
          console.error('解析删除结果失败:', e)
          success = false
        }
      }
    } else if (typeof result === 'object' && result !== null) {
      success = !result.error
    }
    
    if (!success) {
      throw new Error('删除操作失败')
    }
    
    // 如果删除的是默认模板，清除默认设置
    if (isDefaultTemplate(template)) {
      localStorage.removeItem('defaultTemplate')
      localStorage.removeItem('defaultTemplateId')
    }
    
    ElMessage.success(`模板 "${template.name}" 删除成功`)
    await loadTemplates()
    
  } catch (error) {
    console.error('删除模板失败:', error)
    ElMessage.error('删除模板失败: ' + (error.message || error))
  }
}

// 设为默认模板
const setDefaultTemplate = async (template) => {
  localStorage.setItem('defaultTemplate', JSON.stringify(template.config))
  localStorage.setItem('defaultTemplateId', template.id.toString())
  ElMessage.success(`已将 "${template.name}" 设为默认模板`)
  // 刷新模板列表以更新显示
  await loadTemplates()
}

// 取消默认模板
const unsetDefaultTemplate = async () => {
  localStorage.removeItem('defaultTemplate')
  localStorage.removeItem('defaultTemplateId')
  ElMessage.success('已取消默认模板设置')
  // 刷新模板列表以更新显示
  await loadTemplates()
}

// 检查是否为默认模板
const isDefaultTemplate = (template) => {
  const defaultTemplateId = localStorage.getItem('defaultTemplateId')
  return defaultTemplateId && defaultTemplateId === template.id?.toString()
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// ==================== 工具方法 ====================

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch (error) {
    return dateString
  }
}

// 获取位置文本
const getPositionText = (position) => {
  const positionMap = {
    'TOP_LEFT': '左上',
    'TOP_CENTER': '上中',
    'TOP_RIGHT': '右上',
    'CENTER_LEFT': '左中',
    'CENTER': '正中',
    'CENTER_RIGHT': '右中',
    'BOTTOM_LEFT': '左下',
    'BOTTOM_CENTER': '下中',
    'BOTTOM_RIGHT': '右下',
    'CUSTOM': '自定义'
  }
  return positionMap[position] || position
}
</script>

<style scoped>
.template-manager {
  max-width: 1200px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 20px;
}

.config-details {
  font-size: 14px;
  line-height: 1.6;
}

.config-item {
  margin-bottom: 8px;
  padding: 4px 0;
}

.config-item strong {
  color: #303133;
  margin-right: 8px;
}

.el-button-group .el-button {
  margin-left: 0 !important;
}

.el-table .el-button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.el-table .el-button-group .el-button {
  margin: 0;
  font-size: 12px;
  padding: 4px 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 对话框样式优化 */
.el-dialog {
  margin-top: 8vh !important;
}

.el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
}
</style>