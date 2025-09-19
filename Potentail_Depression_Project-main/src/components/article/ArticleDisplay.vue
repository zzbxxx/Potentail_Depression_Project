<template>
  <div class="article-display">
    <div v-if="article" class="article-content">
      <h2>{{ article.title || '无标题' }}</h2>
      <p class="meta"><strong>类型:</strong> {{ article.articleType || '未知' }}</p>
      <p class="meta"><strong>话题:</strong> {{ article.topics && article.topics.length ? article.topics.join(', ') : '无话题' }}</p>
      <p class="meta"><strong>状态:</strong> {{ article.status || '未知' }}</p>
      <div class="blocks">
        <div v-for="(block, index) in article.blocks" :key="index" class="block">
          <p v-if="block.type === 'text'" v-html="block.content" class="text-block"></p>
          <img v-if="block.type === 'image'" :src="block.content" alt="文章图片" class="article-image" />
          <a v-if="block.type === 'link'" :href="block.content" target="_blank" class="article-link">{{ block.content }}</a>
        </div>
      </div>
    </div>
    <div v-else class="loading">
      <el-skeleton :rows="6" animated />
    </div>
  </div>
</template>

<script setup>
import { ElSkeleton } from 'element-plus'
import { log } from 'three/src/nodes/TSL.js';
import { defineProps, defineEmits } from 'vue'
defineProps({
  article: {
    type: Object,
    default: null
  }
})

</script>

<style scoped>
.article-display {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.article-content {
  max-width: 700px;
  margin: 0 auto;
}
h2 {
  font-size: 24px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 20px;
}
.meta {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}
.blocks {
  margin: 20px 0;
}
.block {
  margin-bottom: 20px;
}
.text-block {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}
.article-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  display: block;
  margin: 0 auto;
}
.article-link {
  color: #175199;
  text-decoration: none;
  font-size: 16px;
}
.article-link:hover {
  text-decoration: underline;
}
.loading {
  margin-top: 20px;
}
</style>