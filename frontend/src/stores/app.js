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
    if (!window.javaApi) {
      console.warn('JavaAPI未就绪，无法加载模板')
      console.log('window.javaApi:', window.javaApi)
      return
    }
    
    try {
      setLoading(true)
      console.log('开始加载模板列表...')
      console.log('getAllTemplates方法可用性:', typeof window.javaApi.getAllTemplates)
      
      // 检查getAllTemplates方法是否存在
      if (typeof window.javaApi.getAllTemplates !== 'function') {
        console.error('getAllTemplates方法不存在')
        templates.value = []
        return
      }
      
      // 调用后端获取模板
      const result = await window.javaApi.getAllTemplates()
      console.log('模板加载原始结果:', result)
      
      // 处理结果
      let templateList = []
      if (typeof result === 'string') {
        try {
          const parsed = JSON.parse(result)
          templateList = Array.isArray(parsed) ? parsed : []
        } catch (e) {
          console.error('解析模板JSON失败:', e)
          templateList = []
        }
      } else if (Array.isArray(result)) {
        templateList = result
      }
      
      templates.value = templateList
      console.log('模板加载完成:', templateList.length, '个模板')
      
    } catch (error) {
      console.error('加载模板失败:', error)
      templates.value = []
    } finally {
      setLoading(false)
    }
  }
  
  // 加载处理历史
  const loadHistory = async () => {
    if (!window.javaApi) return
    
    try {
      setLoading(true)
      const result = await window.javaApi.getProcessHistory()
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