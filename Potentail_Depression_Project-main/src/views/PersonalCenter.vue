<template>
  <el-container class="personal-center">
    <!-- 头部 -->
    <el-header class="header">
      <el-row justify="space-between" align="middle">
        <el-col :span="12">
          <h2>个人中心</h2>
        </el-col>
        <el-col :span="12" class="text-right">
          <el-button type="info" plain @click="logout">退出</el-button>
        </el-col>
      </el-row>
    </el-header>

    <!-- 主体 -->
    <el-container>
      <el-aside width="200px" class="sidebar" v-if="!isMobile">
        <el-menu :default-active="activeMenu" background-color="#f0f4f8" text-color="#606266" active-text-color="#409EFF">
          <el-menu-item index="profile" @click="switchTab('profile')">
            <el-icon><User /></el-icon>
            <span>信息设置</span>
          </el-menu-item>
          <el-sub-menu index="follow">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>關注列表</span>
            </template>
            <el-menu-item index="follower" @click="switchTab('follower')">
              關注的人
            </el-menu-item>
            <el-menu-item index="followed" @click="switchTab('followed')">
              關注我的
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="articles-manage" @click="switchTab('articles-manage')">
            <el-icon><User /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-sub-menu index="notifications">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>通知</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="notification-badge" />
            </template>
            <el-menu-item index="notifications-unread" @click="switchTab('notifications-unread')">
              未读通知
            </el-menu-item>
            <el-menu-item index="notifications-read" @click="switchTab('notifications-read')">
              已读通知
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="favorites">
            <template #title>
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </template>
            <el-menu-item index="favorites-articles" @click="switchTab('favorites-articles')">
              文章收藏
            </el-menu-item>
            <el-menu-item index="favorites-cards" @click="switchTab('favorites-cards')">
              卡片收藏
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="email" @click="switchTab('email')">
            <el-icon><Message /></el-icon>
            <span>邮箱管理</span>
          </el-menu-item>
          <el-menu-item index="emailUpload" @click="switchTab('emailUpload')">
            <el-icon><Upload /></el-icon>
            <span>邮箱上传</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>建议与反馈</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 手机端抽屉 -->
      <el-drawer v-model="drawerVisible" title="菜单" :direction="isMobile ? 'ltr' : 'rtl'" size="70%" v-if="isMobile">
        <el-menu :default-active="activeMenu" background-color="#f0f4f8" text-color="#606266" active-text-color="#409EFF">
          <el-menu-item index="profile" @click="switchTab('profile')">
            <el-icon><User /></el-icon>
            <span>信息设置</span>
          </el-menu-item>
          <el-sub-menu index="follow">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>關注列表</span>
            </template>
            <el-menu-item index="follower" @click="switchTab('follower')">
              關注的人
            </el-menu-item>
            <el-menu-item index="followed" @click="switchTab('followed')">
              關注我的
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="articles-manage" @click="switchTab('articles-manage')">
            <el-icon><User /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-sub-menu index="notifications">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>通知</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="notification-badge" />
            </template>
            <el-menu-item index="notifications-unread" @click="switchTab('notifications-unread')">
              未读通知
            </el-menu-item>
            <el-menu-item index="notifications-read" @click="switchTab('notifications-read')">
              已读通知
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="favorites">
            <template #title>
              <el-icon><Star /></el-icon>
              <span>收藏</span>
            </template>
            <el-menu-item index="favorites-articles" @click="switchTab('favorites-articles')">
              文章收藏
            </el-menu-item>
            <el-menu-item index="favorites-cards" @click="switchTab('favorites-cards')">
              卡片收藏
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="email" @click="switchTab('email')">
            <el-icon><Message /></el-icon>
            <span>邮箱管理</span>
          </el-menu-item>
          <el-menu-item index="emailUpload" @click="switchTab('emailUpload')">
            <el-icon><Upload /></el-icon>
            <span>邮箱上传</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>建议与反馈</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <el-button icon="Menu" circle @click="drawerVisible = true" v-if="isMobile" class="mobile-menu-btn"></el-button>
        <el-card class="content-card" shadow="hover">
          <transition name="fade">
            <ProfileSettings 
              v-if="activeTab === 'profile'" 
              :user="user" 
              @update:user="handleUserUpdate"
              @save-success="fetchUserInfo"
            />
          </transition>
          <transition name="fade">
            <div>
              <transition name="fade">
                <div v-if="activeTab === 'follower' || activeTab === 'followed'">
                  <FollowDetail :active-tab="activeTab" />
                </div>
              </transition>
            </div>
          </transition>
          <transition name="fade">
            <div v-if="activeTab === 'articles-manage'">
              <ArticleManagement />
            </div>
          </transition>
          <transition name="fade">
            <EmailManagement v-if="activeTab === 'email'" />
          </transition>
          <transition name="fade">
            <EmailUpload v-if="activeTab === 'emailUpload'" />
          </transition>
          <transition name="fade">
            <FavoritesPanel 
              v-if="activeTab === 'favorites-articles' || activeTab === 'favorites-cards'" 
              :active-tab="activeTab"
            />
          </transition>
          <transition name="fade">
            <Feedback v-if="activeTab === 'feedback'" />
          </transition>
          <transition name="fade">
            <NotificationPanel 
              v-if="activeTab === 'notifications-unread' && user" 
              :user-id="user.id" 
              filter="unread" 
            />
          </transition>
          <transition name="fade">
            <NotificationPanel 
              v-if="activeTab === 'notifications-read' && user" 
              :user-id="user.id" 
              filter="read" 
            />
          </transition>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Message, Star, Bell, Menu, ChatDotSquare, Upload } from '@element-plus/icons-vue';
import EmailManagement from '/src/components/PersonalCenter/EmailManagement.vue';
import NotificationPanel from '/src/components/PersonalCenter/NotificationPanel.vue';
import FavoritesPanel from '/src/components/PersonalCenter/FavoritesPanel.vue';
import { useRouter, useRoute } from 'vue-router';
import PersonalMessageApi from '/src/api/PersonalMessageApi';
import { useNotificationStore } from '/src/stores/notification';
import NotificationService from '/src/api/notificationApi';
import ProfileSettings from '/src/components/PersonalCenter/ProfileSettings.vue';
import EmailUpload from '/src/components/PersonalCenter/EmailUpload.vue';
import Feedback from '/src/components/PersonalCenter/Feedback.vue';
import ArticleManagement from '/src/components/PersonalCenter/ArticleManagement.vue';
import FollowDetail from '/src/components/PersonalCenter/FollowDetail.vue'
const router = useRouter();
const route = useRoute();
const isMobile = ref(window.innerWidth <= 768);
const drawerVisible = ref(false);
const activeMenu = ref('profile');
const activeTab = ref('profile');
const previewAvatar = ref('');
const user = ref(null);
const notificationStore = useNotificationStore();

const formRules = ref({
  nickname: [
    { required: false, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (rule, value, callback) => {
        if (!value) callback();
        else if (/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) callback();
        else callback(new Error('请输入有效的邮箱地址'));
      },
      trigger: ['blur', 'change']
    }
  ],
});
const feedbackContent = ref('');
const avatarFile = ref(null);
const emailFile = ref(null);
const uploading = ref(false);
const emailUploading = ref(false);

const unreadCount = computed(() => notificationStore.unreadCount);

// 有效標籤列表，防止無效標籤
const validTabs = [
  'profile',
  'notifications-unread',
  'notifications-read',
  'favorites-articles',
  'favorites-cards',
  'email',
  'emailUpload',
  'feedback',
  'follower', 
  'followed',
  'articles-manage'
];


const handleUserUpdate = (updatedUser) => {
  user.value = updatedUser;
};

// 初始化標籤狀態
const initTabState = () => {
  // 從路由參數獲取 tab，如果不存在則從 localStorage 獲取，否則預設為 'profile'
  const tabFromQuery = route.query.tab;
  const tabFromStorage = localStorage.getItem('personalCenterActiveTab');
  const defaultTab = 'profile';

  if (tabFromQuery && validTabs.includes(tabFromQuery)) {
    activeTab.value = tabFromQuery;
    activeMenu.value = tabFromQuery;
    localStorage.setItem('personalCenterActiveTab', tabFromQuery);
  } else if (tabFromStorage && validTabs.includes(tabFromStorage)) {
    activeTab.value = tabFromStorage;
    activeMenu.value = tabFromStorage;
  } else {
    activeTab.value = defaultTab;
    activeMenu.value = defaultTab;
    localStorage.setItem('personalCenterActiveTab', defaultTab);
  }
};

const fetchUserInfo = async () => {
  try {
    const data = await PersonalMessageApi.getPersonalInfo();
    user.value = data;
    if (data?.id) {
      try {
        const response = await NotificationService.getUnreadNotifications(data.id);
        if (Array.isArray(response)) {
          notificationStore.updateUnreadCount(response.length);
        } else {
          throw new Error('未读通知数据格式错误');
        }
      } catch (error) {
        console.error('获取未读通知失败:', error);
        ElMessage.error(`获取未读通知失败: ${error.message}`);
      }
    } else {
      console.error('用户信息中缺少 user.id');
      ElMessage.error('无法获取用户ID');
    }
  } catch (e) {
    console.error('获取用户信息失败:', e);
    ElMessage.error('获取用户信息失败');
  }
};

onMounted(() => {
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
  initTabState();
  fetchUserInfo();
});

const switchTab = (tab, markAsRead = false) => {
  if (validTabs.includes(tab)) {
    activeTab.value = tab;
    activeMenu.value = tab;
    drawerVisible.value = false;
    localStorage.setItem('personalCenterActiveTab', tab); // 儲存當前標籤
    // 更新路由參數，保持同步
    router.replace({ name: 'PersonalCenter', query: { tab } });
    if (markAsRead && tab === 'notifications-unread' && user.value) {
      notificationStore.markAllAsRead();
    }
  }
};

const logout = () => {
  localStorage.removeItem('personalCenterActiveTab'); // 退出時清除標籤狀態
  router.push('/main');
};

const submitFeedback = () => {
  if (feedbackContent.value.trim()) {
    ElMessage.success('反馈提交成功，感谢您的建议！');
    feedbackContent.value = '';
  } else ElMessage.warning('请填写反馈内容');
};
</script>

<style scoped>
.personal-center { height: 100vh; background-color: #f0f4f8; color: #606266; }
.header { background-color: #ffffff; padding: 20px; border-bottom: 1px solid #e0e0e0; }
.header h2 { margin: 0; font-size: 24px; color: #409EFF; }
.sidebar { background-color: #f0f4f8; border-right: 1px solid #e0e0e0; }
.main-content { padding: 20px; }
.content-card { background-color: #ffffff; border-radius: 8px; }
.mobile-menu-btn { margin-bottom: 20px; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.text-right { text-align: right; }
.encourage { font-size: 14px; color: #909399; margin-bottom: 20px; }
.notification-badge { margin-left: 10px; margin-bottom: 2rem;}
@media (max-width: 768px) {
  .header h2 { font-size: 20px; }
  .main-content { padding: 10px; }
}
</style>
