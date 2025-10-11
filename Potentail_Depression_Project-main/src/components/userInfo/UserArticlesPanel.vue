<template>
  <el-card class="user-posts">
    <el-row type="flex" justify="space-between" align="middle" class="header-row">
      <el-col :span="12">
        <h3>用戶的文章 · {{ posts.length }}篇</h3>
      </el-col>
      <el-col :span="12" class="text-right">
        <el-button type="primary" class="more-btn" @click="viewMore">查看更多</el-button>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="posts-container">
      <el-col :span="8" v-for="post in posts" :key="post.id" :xs="24" :sm="12" :md="8">
        <el-card class="post-card" shadow="hover" @click="openDetail(post)">
          <div class="article-header">
            <el-avatar v-if="post.avatar" :src="post.avatar" size="small" />
            <span class="nickname">{{ post.nickname || '匿名' }}</span>
            <el-tag type="info" size="small" class="ml-2">{{ post.articleType }}</el-tag>
            <el-tag
              v-for="t in post.topics"
              :key="t"
              type="success"
              size="small"
              class="ml-1"
            >
              {{ t }}
            </el-tag>
          </div>
          <h4 class="article-title">{{ post.title || '未命名標題' }}</h4>
          <div class="article-preview">
            <div v-if="firstTextBlock(post)" class="preview-text">
              {{ firstTextBlock(post) }}
            </div>
            <el-image
              v-if="firstImageBlock(post)"
              :src="firstImageBlock(post)"
              fit="cover"
              class="preview-img"
              lazy
            />
          </div>
          <el-text class="date">發布於 {{ post.createdAt }}</el-text>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { defineProps } from 'vue';
import { useRouter } from 'vue-router';

// 定义 props
const props = defineProps({
  posts: {
    type: Array,
    required: true
  }
});

// 提取第一個文本塊
const firstTextBlock = (article) => {
  const block = article.blocks?.find(b => b.type === 'text');
  if (!block) return '';
  const plain = block.content.replace(/<[^>]+>/g, '');
  return plain.slice(0, 50) + (plain.length > 50 ? '...' : '');
};

// 提取第一個圖片塊
const firstImageBlock = (article) => {
  const block = article.blocks?.find(b => b.type === 'image');
  return block?.content || '';
};

// 查看更多
const router = useRouter();
const viewMore = () => {
  router.push('/user/posts');
};

// 打開文章詳情
const openDetail = (post) => {
  router.push({ name: "DetailPage", params: { id: post.id } });
};
</script>

<style scoped>
.user-posts {
  padding: 20px;
  max-height: calc(100vh - 100px); /* 設置最大高度，留出空間給 header 等 */
  overflow-y: auto; /* 啟用垂直滾動 */
}

.header-row {
  margin-bottom: 20px;
}
.posts-container{
  padding-bottom: 100px;
}

h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.text-right {
  text-align: right;
}

.post-card {
  margin-bottom: 20px;
  cursor: pointer; /* 提示可點擊 */
}

.article-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.nickname {
  font-size: 14px;
  color: #666;
}

.ml-1 {
  margin-left: 4px;
}

.ml-2 {
  margin-left: 8px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.article-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-text {
  font-size: 14px;
  color: #444;
}

.preview-img {
  width: 100%;
  max-height: 150px;
  border-radius: 4px;
}

.date {
  color: #888;
  font-size: 12px;
  margin-top: 8px;
  display: block;
}

.more-btn {
  padding: 8px 20px;
}

/* 確保滾動條美觀 */
.user-posts::-webkit-scrollbar {
  width: 8px;
}

.user-posts::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 4px;
}

.user-posts::-webkit-scrollbar-track {
  background: #f0f4f8;
}
</style>