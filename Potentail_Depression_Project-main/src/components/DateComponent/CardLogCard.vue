<template>
  <article class="card" v-if="data">
    <header class="card-header">
      <img
        class="card-cover"
        src="/src/assets/image/FT.jpg"
        :alt="data.bookTitle || 'cover image'"
        loading="lazy"
      />
    </header>

    <section class="card-content">
      <p v-if="data && data.quoteText" class="desc">
        {{ data.quoteText }}
      </p>
      <div v-if="data && (data.bookTitle || data.author)" class="meta">
        <h3 v-if="data.bookTitle" class="book-title">《{{ data.bookTitle }}》</h3>
        <p v-if="data.author" class="subtitle">{{ data.author }}</p>
      </div>
    </section>

    <footer class="card-footer">
      <el-button
        type="primary"
        size="small"
        class="calm-btn"
        @click="$emit('primary')"
      >
        收藏起来
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

interface Props {
  date: string
  data: {
    id?: number
    quoteText?: string
    author?: string
    bookTitle?: string
    tags?: string[]
  } | null
}

defineProps<Props>()

defineEmits<{
  (e: 'primary'): void
  (e: 'close'): void
}>()
</script>

<style scoped>
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f9f1e7 0%, #ece1d8 48%); 
  color: #4a3c2f;
  border: 1px solid #e0d2c7; 
  max-width: 320px; 
  margin: 0 auto; 
  padding-bottom: 1rem;
}

.card-header {
  display: flex;
  justify-content: flex-start; 
  padding: 16px 20px 8px; 
  align-items: center;
}

.card-cover {
  width: 176px; 
  height: 220px; 
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08); 
  border: 1px solid rgba(255, 255, 255, 0.3); 
  margin: auto; 
}

.card-content {
  padding: 16px 20px; 
  text-align: left; 
}

.desc {
  margin: 0 0 16px 0;
  line-height: 1.6; 
  font-size: 16px; 
  color: #5a4a3f;
  max-height: 200px; 
  overflow-y: auto; 
  padding-right: 10px;
}

.meta {
  margin-top: 12px;
  text-align: left;
}

.book-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  letter-spacing: 0.2px;
  color: #5a4a3f;
}

.subtitle {
  margin: 0;
  color: #7a6650;
  font-size: 14px; 
}

.card-footer {
  display: flex;
  gap: 12px; 
  padding: 12px 20px 16px; 
  justify-content: flex-start;
}

.calm-btn.el-button {
  --el-color-primary: #8a6d3b;
  --el-button-bg-color: #f0e6d2;
  --el-button-text-color: #4a3c2f;
  --el-button-hover-bg-color: #e8d5b9;
  --el-button-active-bg-color: #d9c2a5;
  border: 1px solid #c4a87a;
  padding: 6px 12px; 
}

@media (max-width: 360px) {
  .card-header {
    padding: 12px 16px 6px;
  }
  .card-cover {
    width: 100px; 
    height: 133px;
  }
  .desc {
    font-size: 14px; 
    line-height: 1.5;
  }
  .book-title {
    font-size: 14px;
  }
  .subtitle {
    font-size: 12px;
  }
  .card-footer {
    padding: 8px 16px 12px;
  }
}
</style>