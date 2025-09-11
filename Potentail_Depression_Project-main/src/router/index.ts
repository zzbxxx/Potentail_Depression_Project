import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'
import MoodLog from '../views/MoodLog.vue'

const routes = [
    {
        path: '/main-page.html',
        name: 'MainPage',
        component: MainPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/',
        redirect: '/main-page.html'  // 根路径重定向到主页
    },
    {
        path: '/mood-log',
        name: 'MoodLog',
        component: MoodLog,
        meta: { requiresAuth: true }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/MessageCode/NotFound404.vue')  // 如果有 NotFound.vue
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from) => {
    const token = localStorage.getItem('auth_token') || localStorage.getItem('token')

    if (to.meta.requiresAuth && !token) {
        window.location.replace('/index.html')
        return false
    }

    return true
})

export default router