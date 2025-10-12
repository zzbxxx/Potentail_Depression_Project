<template>
  <div class="detail-background">
    <div class="detail-page">
      <el-skeleton v-if="isLoading" :rows="6" animated />
      <template v-else>
        <div class="header">
          <h1>文章詳情</h1>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
        
        <AuthorInfo v-if="article" :author="{
          nickname: article.nickname,
          avatar: article.avatar,
          createdAt: article.createdAt,
          authorId: article.userId
        }" />
        
        <ArticleDisplay v-if="article" :article="article" />
        
        <div class="Action-Bar-Background">
          <ActionBar
            v-if="article"
            :article="article"
            :show-actions="{
              like: true,
              favorite: true,
              share: true,
              more: true
            }"
            :gap="8"
            @like="handleArticleLike"
            @favorite="handleArticleFavorite"
            @share="handleArticleShare"
            @report="handleArticleReport"
            @dislikeArticle="handleArticleDislike"
            @dislikeAuthor="handleArticleDislikeAuthor"
            @toggleMenu="val => handleToggleMenu(article, val)"
          />
        </div>
        
        <ReplySection :articleId="articleId" :authorId="article?.userId" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import ArticleService from '/src/api/articleApi';
import ArticleDisplay from '/src/components/article/ArticleDisplay.vue';
import AuthorInfo from '/src/components/article/AuthorInfo.vue';
import ReplySection from '/src/components/article/ReplySection.vue';
import ActionBar from '/src/components/article/ActionBar.vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const articleId = route.params.id;
const article = ref(null);
const menuVisible = reactive({});
const isLoading = ref(true);

const fetchArticle = async () => {
  if (!articleId) {
    ElMessage.error('無效的文章 ID');
    isLoading.value = false;
    return;
  }
  try {
    const response = await ArticleService.getArticleData(articleId);
    article.value = Array.isArray(response) ? response[0] : response;
    if (article.value) {
      article.value.likes = article.value.likes || 0;
      article.value.liked = article.value.liked || false;
    }
  } catch (error) {
    console.error('獲取文章失敗:', error);
    ElMessage.error('獲取文章失敗');
  } finally {
    isLoading.value = false;
  }
};

const goBack = () => {
  router.go(-1);
};

const handleArticleLike = (updatedArticle) => {
  article.value.liked = updatedArticle.liked;
  article.value.likes = updatedArticle.likes;
};


const handleArticleShare = (article) => {
  ElMessage.success(`分享文章 ${article.id}`);
};

const handleArticleReport = (article) => {
  ElMessage.warning(`已舉報文章 ${article.id}`);
  menuVisible[article.id] = false;
};

const handleArticleDislike = (article) => {
  ElMessage.info(`將減少類似文章 ${article.id} 的推薦`);
  menuVisible[article.id] = false;
};

const handleArticleDislikeAuthor = (article) => {
  ElMessage.info(`將減少作者 ${article.nickname || '匿名'} 的內容`);
  menuVisible[article.id] = false;
};

const handleToggleMenu = (article, val) => {
  const id = article.id;
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
  menuVisible[id] = val;
};

const closeAllMenu = () => {
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
};

onMounted(() => {
  document.addEventListener('click', closeAllMenu);
  fetchArticle();
});
onBeforeUnmount(() => document.removeEventListener('click', closeAllMenu));
</script>

<style scoped>
.detail-background {
  margin: 0 auto;
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%);
  position: relative;
}
.detail-page {
  max-width: 800px;
  margin: auto;
}
.detail-page::-webkit-scrollbar {
  width: 8px;
}
.detail-page::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
.detail-page::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}
.detail-page::-webkit-scrollbar-thumb:hover {
  background: #555;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.8);
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
h1 {
  font-size: 28px;
  font-weight: bold;
  color: #1a1a1a;
}
.Action-Bar-Background {
  background-color: #ffffff;
  border-radius: 8px;
  margin-top: 1rem;
  padding-left: 1rem;
  padding-bottom: 0.5rem;
}
</style>
