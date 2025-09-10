import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'
import MoodLog from '../views/MoodLog.vue'
const routes = [
    {
        path: '/',
        name: 'MainPage',
        component: MainPage
    },
    {
        path: '/mood-log',
        name: 'MoodLog',
        component: MoodLog
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {

    const token = localStorage.getItem('auth_token') || localStorage.getItem('token')

    if (!token) {
        window.location.replace('/index.html')
        return
    }

    next()
})

export default router