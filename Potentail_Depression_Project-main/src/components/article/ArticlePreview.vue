<template>
  <div class="article-preview-wrapper">
    <!-- 頭像與基本信息 -->
    <div class="article-header">
      <el-avatar
        v-if="article.avatar"
        :src="article.avatar"
        size="small"
        @click="handleUserClick(article.userId)"
      />
      <span class="nickname">{{ article.nickname || '匿名' }}</span>
      <el-tag type="info" size="small" class="ml-2">
        {{ article.articleType }}
      </el-tag>
      <el-tag
        v-for="t in article.topics"
        :key="t"
        type="success"
        size="small"
        class="ml-1"
      >
        {{ t }}
      </el-tag>
    </div>

    <!-- 標題 -->
    <h3 class="article-title">{{ article.title || '未命名标题' }}</h3>

    <!-- 預覽內容 -->
    <div class="article-preview">
      <div
        v-if="firstTextBlock(article)"
        class="preview-text"
      >
        {{ firstTextBlock(article) }}
      </div>
      <el-image
        v-if="firstImageBlock(article)"
        :src="firstImageBlock(article)"
        fit="cover"
        class="preview-img"
        lazy
      />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const firstTextBlock = (article) => {
  const block = article.blocks?.find(b => b.type === 'text')
  if (!block) return ''
  const plain = block.content.replace(/<[^>]+>/g, '')
  return plain.slice(0, 50) + (plain.length > 50 ? '...' : '')
}

const firstImageBlock = (article) => {
  const block = article.blocks?.find(b => b.type === 'image')
  return block?.content || ''
}

const handleUserClick = (userId) => {
  router.push(`/userDetail/${userId}`);
}
</script>

<style scoped>
.article-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
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
  max-height: 200px;
  border-radius: 4px;
}
</style>