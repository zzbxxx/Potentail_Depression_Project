<template>
  <div class="detail-background">
    <div class="detail-page">
      <el-skeleton v-if="isLoading" :rows="6" animated />
      <template v-else>
        <div class="header">
          <h1>文章详情</h1>
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
              love: true,
              more: true
            }"
            :gap="8"
            @like="like"
            @favorite="favorite"
            @share="share"
            @love="love"
            @report="report"
            @dislikeArticle="dislikeArticle"
            @dislikeAuthor="dislikeAuthor"
            @toggleMenu="val => toggleMenu(article, val)"
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


// 获取文章数据
const fetchArticle = async () => {
  if (!articleId) {
    ElMessage.error('无效的文章 ID');
    isLoading.value = false;
    return;
  }
  try {
    const response = await ArticleService.getArticleData(articleId);
    article.value = Array.isArray(response) ? response[0] : response;
    if (article.value) {
      article.value.likes = article.value.likes || 0;
      article.value.loves = article.value.loves || 0;
      article.value.liked = article.value.liked || false;
    }
  } catch (error) {
    console.error('获取文章失败:', error);
    ElMessage.error('获取文章失败');
  } finally {
    isLoading.value = false;
  }
};

const goBack = () => {
  router.go(-1);
};

// ActionBar 事件处理
const like = (article) => {
  if (!article.liked) {
    article.likes++;
    article.liked = true;
  } else {
    article.likes--;
    article.liked = false;
  }
};

const share = (article) => {
  ElMessage.success(`分享文章 ${article.id}`);
};

const love = (article) => {
  article.loves++;
  ElMessage.success('已喜欢~');
};

const report = (article) => {
  ElMessage.warning(`已举报文章 ${article.id}`);
  menuVisible[article.id] = false;
};

const dislikeArticle = (article) => {
  ElMessage.info(`将减少类似文章 ${article.id} 的推荐`);
  menuVisible[article.id] = false;
};

const dislikeAuthor = (article) => {
  ElMessage.info(`将减少作者 ${article.nickname || '匿名'} 的内容`);
  menuVisible[article.id] = false;
};

const toggleMenu = (article, val) => {
  const id = article.id;
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
  menuVisible[id] = val;
};

const closeAllMenu = () => {
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
};

// 页面加载和卸载
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