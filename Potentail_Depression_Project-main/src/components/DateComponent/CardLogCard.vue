<template>
  <article class="card" v-if="data" :style="{ '--card-width': width, '--card-height': height }">
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
        @click="handleCardToCollection"
      >
        <el-icon :size="16">
          <Star :style="{ color: data.isFavorited ? '#fadb14' : '#606266' }" />
        </el-icon>
        <span v-if="!data.isFavorited">收藏</span>
        <span v-else>已收藏</span>
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { defineProps, watch } from 'vue';
import { Star } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
//@ts-ignore
import FavoriteService from '/src/api/favoriteApi';

interface Props {
  date: string;
  data: {
    id?: number;
    quoteText?: string;
    author?: string;
    bookTitle?: string;
    tags?: string[];
    isFavorited: boolean;
  } | null;
  width?: string;
  height?: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'removed'): void;
  (e: 'primary'): void;
  (e: 'close'): void;
}>();

const handleCardToCollection = async () => {
  if (!props.data || !props.data.id) {
    console.error('Invalid card data:', props.data);
    ElMessage.error('無效的卡片數據');
    return;
  }

  const favorite = {
    favoriteableId: props.data.id,
    favoriteableType: 'CARD',
  };

  try {
    if (props.data.isFavorited) {
      await ElMessageBox.confirm(
        '確定要取消收藏這張卡片嗎？',
        '確認取消收藏',
        {
          confirmButtonText: '確定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      );
      console.log('Removing favorite for card:', props.data.id);
      await FavoriteService.removeFavorite(favorite);
      ElMessage.warning('已取消收藏');
      emit('removed');
    } else {
      console.log('Adding favorite for card:', props.data.id);
      await FavoriteService.addFavorite(favorite);
      ElMessage.success('已收藏');
      emit('primary');
    }
  } catch (error) {
    console.error('Favorite operation failed:', error);
    ElMessage.error(props.data.isFavorited ? '取消收藏失敗' : '收藏失敗');
  }
};

watch(() => props.data, (newData) => {
  console.log('props.data updated:', newData);
}, { deep: true });

console.log('Received props.date:', props.date);
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
  width: var(--card-width, 320px); /* 默認寬度 320px */
  height: var(--card-height, auto); /* 默認高度 auto */
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
  width: 55%; 
  aspect-ratio: 4/5; /* 圖片比例適應 */
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
    width: 60%; 
    aspect-ratio: 4/5;
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