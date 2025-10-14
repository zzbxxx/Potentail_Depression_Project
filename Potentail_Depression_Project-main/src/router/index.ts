import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from '../views/AuthPage.vue'
import MainPage from '../views/MainPage.vue'
import MoodLog from '../views/MoodLog.vue'
import PersonalCenter from '../views/PersonalCenter.vue'
import NotFound from '../views/MessageCode/NotFound404.vue'
import Editror from '../views/Editor.vue'
import DetailPage from '../views/DetailPage.vue'
import StudyRoomPage from '../views/StudyRoomPage.vue'
import UserDetailPage from '../views/UserDetailPage.vue'
import AdminControlPage from '../views/AdminControlPage.vue'
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
        path: '/userDetail/:id',
        name: 'UserDetailPage',
        component: UserDetailPage,
        meta: { requiresAuth: true }
    },
    {
        path: '/editor',
        name: 'Editror',
        component: Editror,
        meta: { requiresAuth: true }
    },
    {
        path: '/mood-log',
        name: 'MoodLog',
        component: MoodLog,
        meta: { requiresAuth: true }
    },
    {
        path: '/personal-center',
        name: 'PersonalCenter',
        component: PersonalCenter,
        meta: { requiresAuth: true }
    },
    {
        path: '/admin-control',
        name: 'AdminControlPage',
        component: () => AdminControlPage,
    },
    {
        path: '/detail/:id',
        name: 'DetailPage',
        component: DetailPage,
        meta: { requiresAuth: true },
    },
    {
        path: '/study-room/:id',
        name: 'StudyRoomPage',
        component: StudyRoomPage,
        meta: { requiresAuth: true },
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