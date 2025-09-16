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
      <!-- 侧边栏（电脑端显示，手机端隐藏并用抽屉替换） -->
      <el-aside width="200px" class="sidebar" v-if="!isMobile">
        <el-menu :default-active="activeMenu" background-color="#f0f4f8" text-color="#606266" active-text-color="#409EFF">
          <el-menu-item index="profile" @click="switchTab('profile')">
            <el-icon><User /></el-icon>
            <span>信息设置</span>
          </el-menu-item>
          <el-menu-item index="email" @click="switchTab('email')">
            <el-icon><Message /></el-icon>
            <span>邮箱管理</span>
          </el-menu-item>
          <el-menu-item index="favorites" @click="switchTab('favorites')">
            <el-icon><Star /></el-icon>
            <span>收藏</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>建议与反馈</span>
          </el-menu-item>
          <!-- 预留其他模块 -->
          <el-menu-item index="notifications" @click="switchTab('notifications')">
            <el-icon><Bell /></el-icon>
            <span>通知</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 手机端抽屉菜单 -->
      <el-drawer v-model="drawerVisible" title="菜单" :direction="isMobile ? 'ltr' : 'rtl'" size="70%" v-if="isMobile">
        <el-menu :default-active="activeMenu" background-color="#f0f4f8" text-color="#606266" active-text-color="#409EFF">
          <el-menu-item index="profile" @click="switchTab('profile')">
            <el-icon><User /></el-icon>
            <span>信息设置</span>
          </el-menu-item>
          <el-menu-item index="email" @click="switchTab('email')">
            <el-icon><Message /></el-icon>
            <span>邮箱管理</span>
          </el-menu-item>
          <el-menu-item index="favorites" @click="switchTab('favorites')">
            <el-icon><Star /></el-icon>
            <span>收藏</span>
          </el-menu-item>
          <el-menu-item index="feedback" @click="switchTab('feedback')">
            <el-icon><ChatDotSquare /></el-icon>
            <span>建议与反馈</span>
          </el-menu-item>
          <el-menu-item index="notifications" @click="switchTab('notifications')">
            <el-icon><Bell /></el-icon>
            <span>通知</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <!-- 手机端菜单按钮 -->
        <el-button icon="Menu" circle @click="drawerVisible = true" v-if="isMobile" class="mobile-menu-btn"></el-button>

        <el-card class="content-card" shadow="hover">
          <transition name="fade">
            <div v-if="activeTab === 'profile'">
              <h3>信息设置</h3>
              <el-form label-width="120px">
                <el-form-item label="用户名">
                  <el-input v-model="user.name" placeholder="请输入用户名"></el-input>
                </el-form-item>
                <el-form-item label="头像">
                  <el-upload action="#" :show-file-list="false">
                    <el-button type="primary">上传头像</el-button>
                  </el-upload>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>

          <transition name="fade">
            <EmailManagement v-if="activeTab === 'email'" />
          </transition>

          <transition name="fade">
            <div v-if="activeTab === 'favorites'">
              <h3>收藏</h3>
              <el-row :gutter="20">
                <el-col :span="8" v-for="item in favorites" :key="item.id">
                  <el-card :body-style="{ padding: '10px' }">
                    <img :src="item.image" class="image" />
                    <div style="padding: 14px;">
                      <span>{{ item.title }}</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </transition>

          <transition name="fade">
            <div v-if="activeTab === 'feedback'">
              <h3>建议与反馈</h3>
              <p class="encourage">您的反馈对我们很重要，感谢分享您的想法。</p>
              <el-form label-width="120px">
                <el-form-item label="反馈内容">
                  <el-input type="textarea" v-model="feedbackContent" placeholder="请输入您的建议或反馈" :rows="5"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitFeedback">提交</el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>

          <transition name="fade">
            <div v-if="activeTab === 'notifications'">
              <h3>通知</h3>
              <el-empty description="暂无通知，保持平静。"></el-empty>
            </div>
          </transition>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { User, Message, Star, Bell, Menu, ChatDotSquare } from '@element-plus/icons-vue';
import EmailManagement from '/src/components/PersonalCenter/EmailManagement.vue'; // 导入邮箱组件
import { useRouter } from 'vue-router';
const router = useRouter()
// 响应式判断
const isMobile = ref(window.innerWidth <= 768);
const drawerVisible = ref(false);

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});

// 菜单和 tab 切换
const activeMenu = ref('profile');
const activeTab = ref('profile');
const switchTab = (tab) => {
  activeTab.value = tab;
  activeMenu.value = tab;
  drawerVisible.value = false; // 关闭抽屉
};

// 示例数据
const user = ref({ name: '用户示例' });
const favorites = ref([
  { id: 1, title: '收藏项1', image: 'https://via.placeholder.com/150' },
  { id: 2, title: '收藏项2', image: 'https://via.placeholder.com/150' },
]);
const feedbackContent = ref(''); // 反馈内容

// 方法示例
const saveProfile = () => {
  // 保存逻辑
  ElMessage.success('保存成功');
};
const logout = () => {
  router.push('/main'); 
};
const submitFeedback = () => {
  if (feedbackContent.value.trim()) {
    // 提交逻辑（例如调用 API）
    ElMessage.success('反馈提交成功，感谢您的建议！');
    feedbackContent.value = '';
  } else {
    ElMessage.warning('请填写反馈内容');
  }
};
</script>

<style scoped>
.personal-center {
  height: 100vh;
  background-color: #f0f4f8; /* 柔和浅蓝灰背景 */
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
  color: #409EFF; /* 柔和蓝 */
}

.sidebar {
  background-color: #f0f4f8;
  border-right: 1px solid #e0e0e0;
}

.main-content {
  padding: 20px;
}

.content-card {
  background-color: #ffffff;
  border-radius: 8px;
}

.mobile-menu-btn {
  margin-bottom: 20px;
}

.image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease; /* 平滑过渡 */
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.text-right {
  text-align: right;
}

.encourage {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

/* 响应式媒体查询 */
@media (max-width: 768px) {
  .header h2 {
    font-size: 20px;
  }
  .main-content {
    padding: 10px;
  }
}
</style>