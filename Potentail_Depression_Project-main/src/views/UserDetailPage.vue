<template>
  <div class="user-profile-container">
    <!-- 用户信息部分 -->
    <user-info :user="user" />
    <!-- 文章列表部分 -->
    <user-posts :posts="posts" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import UserInfo from '/src/components/userInfo/UserInfoPanel.vue';
import UserPosts from '/src/components/userInfo/UserArticlesPanel.vue';
import PersonalMessageApi from '/src/api/PersonalMessageApi';
import ArticleService from '/src/api/ArticleApi';

// 获取路由参数
const route = useRoute();
const personId = route.params.id;

// 初始化 user 数据
const user = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: '',
  phone: '',
  createdAt: '',
  lastIpLocation: ''
});

// 初始化文章数据
const posts = ref([]);

// 获取用户数据
const fetchUserData = async () => {
  if (!personId) {
    ElMessage.error('用戶ID無效，無法載入資料');
    return;
  }
  try {
    const response = await PersonalMessageApi.getPersonalInfo(personId);
    user.value = {
      id: response.id,
      username: response.username || '',
      nickname: response.nickname || '',
      email: response.email || '',
      avatar: response.avatar || '',
      bio: response.bio || '',
      phone: response.phone || '',
      createdAt: response.createdAt || '',
      lastIpLocation: response.lastIpLocation || ''
    };
  } catch (error) {
    ElMessage.error(`載入用戶資料失敗：${error.message || '請稍後重試'}`);

  }
};

// 获取用户文章数据
const getArticlesByUserId = async () => {
  if (!personId) {
    ElMessage.error('未找到用戶 ID');
    return;
  }
  try {
    const response = await ArticleService.getArticlesByUserId(personId);
    posts.value = response.map(article => ({
      id: article.id,
      title: article.title,
      articleType: article.articleType,
      topics: article.topics,
      blocks: article.blocks,
      nickname: article.nickname,
      avatar: article.avatar,
      createdAt: article.createdAt,
      userId: article.userId,
      isPublicInFollow: article.isPublicInFollow
    }));
  } catch (e) {
    console.error('獲取文章失敗', e);
    ElMessage.error('獲取文章失敗');
  }
};

onMounted(() => {
  fetchUserData();
  getArticlesByUserId();
});
</script>

<style scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}
</style>