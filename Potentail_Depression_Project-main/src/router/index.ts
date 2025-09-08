import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue' // 其他页面组件

const routes = [
    {
        path: '/',
        name: 'MainPage',
        component: MainPage
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router