import { createApp } from 'vue'
import { createPinia } from 'pinia'  // 添加这行
import App from './App.vue'
import router from './router'

import 'element-plus/dist/index.css'
import ElementPlus from 'element-plus'
import './style.css'

const app = createApp(App)
const pinia = createPinia()  // 创建 Pinia 实例

app.use(router)
app.use(pinia)  // 使用 Pinia
app.use(ElementPlus)
app.mount('#app')