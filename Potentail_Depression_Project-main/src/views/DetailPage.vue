<template>
  <div class="detail-background">
    <div class="detail-page">
      <div class="header">
        <h1>文章详情</h1>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>
      
      <!-- 作者信息组件 -->
      <AuthorInfo v-if="article" :author="{
        nickname: article.nickname,
        avatar: article.avatar,
        createdAt: article.createdAt
      }" />

      <!-- 文章展示组件 -->
      <ArticleDisplay :article="article" />

      <!-- 私密留言组件 -->
      <CommentSection :articleId="articleId" :authorId="article?.userId" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ArticleService from '/src/api/articleApi'
import ArticleDisplay from '/src/components/article/ArticleDisplay.vue'
import AuthorInfo from '/src/components/article/AuthorInfo.vue'
import CommentSection from '/src/components/article/CommentSection.vue'
import { ElMessage } from 'element-plus'

// 路由和参数
const router = useRouter()
const route = useRoute()
const articleId = route.params.id
const article = ref(null)

// 获取文章数据
const fetchArticle = async () => {
  if (!articleId) {
    ElMessage.error('无效的文章 ID')
    return
  }
  try {
    const response = await ArticleService.getArticleData(articleId)
    article.value = Array.isArray(response) ? response[0] : response
    console.log(article.value);
    
  } catch (error) {
    console.error('获取文章失败:', error)
    ElMessage.error('获取文章失败')
  }
}

// 返回主页面
const goBack = () => {
  router.push('/main')
}

// 页面加载时获取文章
onMounted(fetchArticle)
</script>

<style scoped>
.detail-background{
  margin: 0 auto;
  padding: 20px;
  height: 100vh; 
  overflow-y: auto; /* 启用垂直滚动 */
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%); 
  position: relative;
}
.detail-page {
  max-width: 800px;
  margin: auto;
}

/* 自定义滚动条样式 */
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
</style>