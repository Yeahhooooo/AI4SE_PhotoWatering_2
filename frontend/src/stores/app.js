import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 应用状态
  const loading = ref(false)
  const processing = ref(false)
  
  // 用户设置
  const settings = reactive({
    defaultOutputDir: '',
    defaultImageQuality: 90,
    autoCleanupDays: 7,
    showPreviewByDefault: true,
    theme: 'light'
  })
  
  // 模板列表
  const templates = ref([])
  
  // 处理历史
  const history = ref([])
  
  // JavaFX API实例
  const javaApi = ref(null)
  
  // 设置loading状态
  const setLoading = (state) => {
    loading.value = state
  }
  
  // 设置processing状态
  const setProcessing = (state) => {
    processing.value = state
  }
  
  // 更新设置
  const updateSettings = (newSettings) => {
    Object.assign(settings, newSettings)
  }
  
  // 设置JavaFX API
  const setJavaApi = (api) => {
    javaApi.value = api
  }
  
  // 加载模板
  const loadTemplates = async () => {
    if (!javaApi.value) return
    
    try {
      setLoading(true)
      const result = await javaApi.value.getTemplates()
      templates.value = result || []
    } catch (error) {
      console.error('加载模板失败:', error)
      templates.value = []
    } finally {
      setLoading(false)
    }
  }
  
  // 加载处理历史
  const loadHistory = async () => {
    if (!javaApi.value) return
    
    try {
      setLoading(true)
      const result = await javaApi.value.getProcessHistory()
      history.value = result || []
    } catch (error) {
      console.error('加载历史记录失败:', error)
      history.value = []
    } finally {
      setLoading(false)
    }
  }
  
  // 加载用户设置
  const loadSettings = async () => {
    if (!javaApi.value) return
    
    try {
      const result = await javaApi.value.getUserSettings()
      if (result) {
        updateSettings(result)
      }
    } catch (error) {
      console.error('加载用户设置失败:', error)
    }
  }
  
  // 保存用户设置
  const saveSettings = async () => {
    if (!javaApi.value) return
    
    try {
      await javaApi.value.saveUserSettings(settings)
      return true
    } catch (error) {
      console.error('保存用户设置失败:', error)
      return false
    }
  }
  
  return {
    // 状态
    loading,
    processing,
    settings,
    templates,
    history,
    javaApi,
    // 方法
    setLoading,
    setProcessing,
    updateSettings,
    setJavaApi,
    loadTemplates,
    loadHistory,
    loadSettings,
    saveSettings
  }
})