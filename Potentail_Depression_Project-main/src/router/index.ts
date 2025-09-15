import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from '../views/AuthPage.vue'
import MainPage from '../views/MainPage.vue'
import MoodLog from '../views/MoodLog.vue'
import EmailComponent from '../views/EmailComponent.vue'
import NotFound from '../views/MessageCode/NotFound404.vue'

// @ts-ignore
import { useAudioStore } from '../stores/audio'

const routes = [
    {
        path: '/',
        name: 'Auth',
        component: AuthPage,
    },
    {
        path: '/main',
        name: 'MainPage',
        component: MainPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/mood-log',
        name: 'MoodLog',
        component: MoodLog,
        meta: { requiresAuth: true }
    },
    {
        path: '/email-compotent',
        name: 'EmailComponent',
        component: EmailComponent,
        meta: { requiresAuth: true }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFound
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }
    }
})

// router/index.js
router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('auth_token') || localStorage.getItem('token')

    if (to.meta.requiresAuth && !token) {
        return next({ name: 'Auth' })
    }

    // 只有在离开Auth页面时才停止音频
    if (from.name === 'Auth' && to.name !== 'Auth') {
        const audioStore = useAudioStore()
        audioStore.stopAll()
        console.log('Cleaned audio via store')
    }

    next()
})
export default router