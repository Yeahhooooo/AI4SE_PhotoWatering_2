import { createRouter, createWebHashHistory } from 'vue-router'

// 使用Hash模式，适配JavaFX WebView
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/watermark'
    },
    {
      path: '/watermark',
      name: 'watermark',
      component: () => import('../views/WatermarkEditor.vue')
    },
    {
      path: '/templates',
      name: 'templates', 
      component: () => import('../views/TemplateManager.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/AppSettings.vue')
    }
  ]
})

export default router