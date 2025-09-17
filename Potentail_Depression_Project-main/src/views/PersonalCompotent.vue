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
      <!-- 侧边栏 -->
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
          <el-menu-item index="emailUpload" @click="switchTab('emailUpload')">
            <el-icon><Upload /></el-icon>
            <span>邮箱上传</span>
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
      </el-aside>

      <!-- 手机端抽屉 -->
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
          <el-menu-item index="emailUpload" @click="switchTab('emailUpload')">
            <el-icon><Upload /></el-icon>
            <span>邮箱上传</span>
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
        <el-button icon="Menu" circle @click="drawerVisible = true" v-if="isMobile" class="mobile-menu-btn"></el-button>
        <el-card class="content-card" shadow="hover">
          <transition name="fade">
            <div v-if="activeTab === 'profile'">
              <h3>信息设置</h3>
              <el-form 
                :model="user" 
                :rules="formRules" 
                ref="profileForm" 
                label-width="120px"
              >
                <el-form-item label="用户名" prop="nickname">
                  <el-input v-model="user.nickname" placeholder="请输入用户名"></el-input>
                </el-form-item>
                <el-form-item label="绑定邮箱" prop="email">
                  <el-input v-model="user.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>
                <el-form-item label="头像">
                  <el-upload
                    action="#"
                    :show-file-list="false"
                    :auto-upload="false"
                    :on-change="handleAvatarChange"
                    :before-upload="beforeUpload"
                    :disabled="uploading"
                  >
                    <el-button type="primary" :loading="uploading">上传头像</el-button>
                  </el-upload>
                  <el-image 
                    v-if="previewAvatar || user.avatar" 
                    :src="previewAvatar || user.avatar" 
                    style="width: 50px; height: 50px; border-radius: 50%; margin-top: 10px;" 
                    fit="cover"
                    :preview-src-list="[previewAvatar || user.avatar]"
                  />
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
            <div v-if="activeTab === 'emailUpload'">
              <h3>邮箱上传</h3>
              <el-form label-width="120px">
                <el-form-item label="上传邮箱文件">
                  <el-upload
                    action="#"
                    :show-file-list="false"
                    :http-request="handleEmailUpload"
                    :before-upload="beforeEmailUpload"
                    :disabled="emailUploading"
                  >
                    <el-button type="primary" :loading="emailUploading">上传邮箱文件</el-button>
                  </el-upload>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="viewUploadedEmails">查看已上传邮箱</el-button>
                </el-form-item>
              </el-form>
            </div>
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
import { ref, onMounted ,onUnmounted} from 'vue';
import { ElMessage } from 'element-plus';
import { User, Message, Star, Bell, Menu, ChatDotSquare, Upload } from '@element-plus/icons-vue';
import EmailManagement from '/src/components/PersonalCenter/EmailManagement.vue';
import { useRouter } from 'vue-router';
import PersonalMessageApi from '/src/api/PersonalMessageApi';

const router = useRouter();
const isMobile = ref(window.innerWidth <= 768);
const drawerVisible = ref(false);
const activeMenu = ref('profile');
const activeTab = ref('profile');
const previewAvatar = ref(''); //本地預覽
const user = ref({ nickname: '用户示例', email: '', avatar: '' });
const favorites = ref([
  { id: 1, title: '收藏项1', image: 'https://via.placeholder.com/150' },
  { id: 2, title: '收藏项2', image: 'https://via.placeholder.com/150' },
]);

const formRules = ref({
  nickname: [
    { required: false, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(); 
        } else if (/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
          callback();
        } else {
          callback(new Error('请输入有效的邮箱地址'));
        }
      },
      trigger: ['blur', 'change']
    }
  ]
});
const feedbackContent = ref('');
const avatarFile = ref(null);
const emailFile = ref(null);
const uploading = ref(false);
const emailUploading = ref(false);

onMounted(() => {
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});

const switchTab = (tab) => {
  activeTab.value = tab;
  activeMenu.value = tab;
  drawerVisible.value = false;
};

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImage) {
    ElMessage.error('请上传图片文件');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB');
    return false;
  }
  return false;  // 修改：返回 false，阻止任何默認上傳行為（因為我們手動處理）
};

// 新增：處理文件選擇事件，生成本地預覽
const handleAvatarChange = (uploadFile) => {
  if (previewAvatar.value) {
    URL.revokeObjectURL(previewAvatar.value); 
  }
  avatarFile.value = uploadFile.raw; 
  previewAvatar.value = URL.createObjectURL(avatarFile.value);  // 生成本地 URL 用於預覽
};

const handleUpload = async () => {
  if (!avatarFile.value) return;
  uploading.value = true;
  try {
    const { url, publicUrl } = await PersonalMessageApi.getPicturePresignedUrl(avatarFile.value.name, avatarFile.value.type);
    console.log('Pre-signed Upload URL:', url);
    console.log('Access URL:', publicUrl);

    const uploadSuccess = await PersonalMessageApi.uploadFile(url, avatarFile.value);
    if (uploadSuccess) {
      user.value.avatar = publicUrl; 
    } else {
      throw new Error('上传到 Backblaze B2 失败');
    }
  } catch (error) {
    ElMessage.error(`上传失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('Upload error:', error);
    throw error; 
  } finally {
    uploading.value = false;
  }
};

// 修改：saveProfile 先上傳（如果有文件），再保存到數據庫
const saveProfile = async () => {
  try {
    if (avatarFile.value) {
      await handleUpload();  // 如果有新文件，先上傳
    }
    
    await PersonalMessageApi.putPersonalInfo(user.value);  // 上傳成功後保存到數據庫
    ElMessage.success('保存成功');
    // 清理預覽和文件
    if (previewAvatar.value) {
      URL.revokeObjectURL(previewAvatar.value);
      previewAvatar.value = '';
    }
    avatarFile.value = null;
  } catch (error) {
    ElMessage.error(`保存失败：${error.message || '请稍后重试，或联系支持团队'}`);
  }
};

onUnmounted(() => {
  if (previewAvatar.value) {
    URL.revokeObjectURL(previewAvatar.value);
  }
});

const handleEmailUpload = async () => {
  if (!emailFile.value) return;
  emailUploading.value = true;
  try {
    const { url, publicUrl } = await PersonalMessageApi.getEmailPresignedUrl(emailFile.value.name, emailFile.value.type);
    console.log('Pre-signed Email Upload URL:', url);
    console.log('Access URL:', publicUrl);

    const uploadSuccess = await PersonalMessageApi.uploadFile(url, emailFile.value);
    if (uploadSuccess) {
      ElMessage.success('邮箱文件上传成功！');
    } else {
      throw new Error('上传邮箱文件到 Backblaze B2 失败');
    }
  } catch (error) {
    ElMessage.error(`上传失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('Email Upload error:', error);
  } finally {
    emailUploading.value = false;
  }
};

const viewUploadedEmails = async () => {
  try {
    const files = await PersonalMessageApi.listUploadedEmails();
    ElMessage.success(`已上传 ${files.length} 个邮箱文件`);
    console.log('Uploaded email files:', files);
  } catch (error) {
    ElMessage.error(`获取失败：${error.message || '请稍后重试，或联系支持团队'}`);
    console.error('List email files error:', error);
  }
};

const refreshDownloadUrl = async (fileKey) => {
  try {
    const url = await PersonalMessageApi.getPicturePresignedDownloadUrl(fileKey);
    user.value.avatar = url;
    console.log('Refreshed Download URL:', url);
  } catch (error) {
    console.error('Refresh download URL error:', error);
  }
};

setInterval(() => {
  if (user.value.avatar && user.value.avatar.includes('X-Amz-Expires')) {
    const fileKey = user.value.avatar.split('/avatars/')[1].split('?')[0];
    refreshDownloadUrl(fileKey);
  }
}, 3600 * 1000);


const logout = () => {
  router.push('/main');
};

const submitFeedback = () => {
  if (feedbackContent.value.trim()) {
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
  background-color: #f0f4f8;
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
  transition: opacity 0.3s ease;
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
@media (max-width: 768px) {
  .header h2 {
    font-size: 20px;
  }
  .main-content {
    padding: 10px;
  }
}
</style>
