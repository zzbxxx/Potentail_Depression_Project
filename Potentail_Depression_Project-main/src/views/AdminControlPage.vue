<template>
  <el-container class="admin-control">
    <!-- 头部 -->
    <el-header class="header">
      <el-row justify="space-between" align="middle">
        <el-col :span="12">
          <h2>管理中心</h2>
        </el-col>
        <el-col :span="12" class="text-right">
          <el-button type="info" plain @click="logout">退出</el-button>
        </el-col>
      </el-row>
    </el-header>

    <!-- 主体 -->
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar" v-if="!isMobile">
        <el-menu
          :default-active="activeMenu"
          background-color="#f0f4f8"
          text-color="#606266"
          active-text-color="#409EFF"
        >
          <el-sub-menu index="article-audit">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>文章审核</span>
            </template>
            <el-menu-item index="pending" @click="switchTab('pending')">待通过</el-menu-item>
            <el-menu-item index="approved" @click="switchTab('approved')">通过</el-menu-item>
            <el-menu-item index="rejected" @click="switchTab('rejected')">未通过</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="user-management" @click="switchTab('user-management')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="card-management" @click="switchTab('card-management')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>卡片管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 手机端抽屉 -->
      <el-drawer
        v-model="drawerVisible"
        title="管理菜单"
        :direction="isMobile ? 'ltr' : 'rtl'"
        size="70%"
        v-if="isMobile"
      >
        <el-menu
          :default-active="activeMenu"
          background-color="#f0f4f8"
          text-color="#606266"
          active-text-color="#409EFF"
        >
          <el-sub-menu index="article-audit">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>文章审核</span>
            </template>
            <el-menu-item index="pending" @click="switchTab('pending')">待通过</el-menu-item>
            <el-menu-item index="approved" @click="switchTab('approved')">通过</el-menu-item>
            <el-menu-item index="rejected" @click="switchTab('rejected')">未通过</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="user-management" @click="switchTab('user-management')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="card-management" @click="switchTab('card-management')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>卡片管理</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <el-button icon="Menu" circle @click="drawerVisible = true" v-if="isMobile" class="mobile-menu-btn"></el-button>
        <el-card class="content-card" shadow="hover">
          <transition name="fade">
            <div>
              <ArticleAudit v-if="activeTab === 'pending'" :status="'pending'" />
              <ArticleAudit v-if="activeTab === 'approved'" :status="'approved'" />
              <ArticleAudit v-if="activeTab === 'rejected'" :status="'rejected'" />
            </div>
          </transition>
          <transition name="fade">
            <div v-if="activeTab === 'user-management'">
              <h3>用户管理</h3>
              <el-empty description="暂无用户管理功能"></el-empty>
            </div>
          </transition>
          <transition name="fade">
            <div v-if="activeTab === 'feedback'">
              <h3>反馈管理</h3>
              <el-empty description="暂无反馈管理功能"></el-empty>
            </div>
          </transition>
          <transition name="fade">
            <div v-if="activeTab === 'card-management'">
              <h3>卡片管理</h3>
              <el-empty description="暂无卡片管理功能"></el-empty>
            </div>
          </transition>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElIcon } from 'element-plus';
import { Document, User, ChatDotSquare, Menu } from '@element-plus/icons-vue';
import ArticleAudit from '/src/components/audit/ArticleAudit.vue';

const router = useRouter();
const route = useRoute();
const isMobile = ref(window.innerWidth <= 768);
const drawerVisible = ref(false);
const activeMenu = ref('pending');
const activeTab = ref('pending');

// 有效標籤列表
const validTabs = [
  'pending',
  'approved',
  'rejected',
  'user-management',
  'feedback',
  'card-management',
];

// 初始化標籤狀態
const initTabStatus = () => {
  const tabFromQuery = route.query.tab;
  const tabFromStorage = localStorage.getItem('AdminControlPage-activeTab');
  const defaultTab = 'pending';

  if (tabFromQuery && validTabs.includes(tabFromQuery)) {
    activeTab.value = tabFromQuery;
    activeMenu.value = tabFromQuery === 'pending' || tabFromQuery === 'approved' || tabFromQuery === 'rejected' ? tabFromQuery : tabFromQuery;
    localStorage.setItem('AdminControlPage-activeTab', tabFromQuery);
  } else if (tabFromStorage && validTabs.includes(tabFromStorage)) {
    activeTab.value = tabFromStorage;
    activeMenu.value = tabFromStorage === 'pending' || tabFromStorage === 'approved' || tabFromStorage === 'rejected' ? tabFromStorage : tabFromStorage;
  } else {
    activeTab.value = defaultTab;
    activeMenu.value = defaultTab;
    localStorage.setItem('AdminControlPage-activeTab', defaultTab);
  }
};

// 切換標籤
const switchTab = (tab) => {
  if (validTabs.includes(tab)) {
    activeTab.value = tab;
    activeMenu.value = tab === 'pending' || tab === 'approved' || tab === 'rejected' ? tab : tab;
    localStorage.setItem('AdminControlPage-activeTab', tab);
    router.replace({ name: 'AdminControlPage', query: { tab } });
  } else {
    ElMessage.error('无效的选项卡');
  }
};

// 退出
const logout = () => {
  localStorage.removeItem('AdminControlPage-activeTab');
  router.push('/main'); // 跳轉到主頁
};

// 監聽窗口大小變化
onMounted(() => {
  initTabStatus();
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});
</script>

<style scoped>
.admin-control {
  height: 100vh;
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%);
  color: #606266;
}

.header {
  background-color: #ffffff;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #409EFF;
}

.sidebar {
  background-color: #f0f4f8;
  border-right: 1px solid #e0e0e0;
}

.main-content {
  padding: 20px;
}

.content-card {
  background-color: #f9fbfd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.mobile-menu-btn {
  margin-bottom: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.text-right {
  text-align: right;
}

@media (max-width: 768px) {
  .header h2 {
    font-size: 20px;
  }
  .main-content {
    padding: 10px;
  }
}

@media (prefers-color-scheme: dark) {
  .admin-control {
    background: linear-gradient(135deg, rgba(30, 35, 41, 0.95) 0%, rgba(44, 62, 80, 0.95) 100%);
    color: #E0E0E0;
  }

  .header {
    background-color: #2c3e50;
    border-bottom: 1px solid #444;
  }

  .header h2 {
    color: #E0E0E0;
  }

  .sidebar {
    background-color: #2c3e50;
    border-right: 1px solid #444;
  }

  .content-card {
    background-color: #2c3e50;
  }
}
</style>
