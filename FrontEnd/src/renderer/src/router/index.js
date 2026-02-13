import { createRouter, createWebHashHistory } from 'vue-router'

// 简单的路由配置
const routes = [
  {
    path: '/',
    redirect: '/login' // 默认跳转到登录页
  },
  {
    path: '/login',
    name: 'Login',
    // Login.vue 组件所处位置，..表示上一级目录，@表示src目录
    component: () => import('../views/Login.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue')
  }
]

const router = createRouter({
  // Electron 中建议使用 Hash 模式
  history: createWebHashHistory(),
  routes
})

export default router
