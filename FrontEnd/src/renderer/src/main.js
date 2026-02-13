import { createApp } from 'vue'
import App from './App.vue'
// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 2. 引入 Pinia
import { createPinia } from 'pinia'

// 3. 引入 Router
import router from './router' 

const app = createApp(App)

// 注册 Element Plus
app.use(ElementPlus)
// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册 Pinia
app.use(createPinia())

app.use(router)

app.mount('#app')
