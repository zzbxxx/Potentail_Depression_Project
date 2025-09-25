<template>
  <article class="card">
    <header class="card-header">
      <img
        v-if="cardData.cover"
        class="card-cover"
        :src="cardData.cover"
        :alt="cardData.title || 'cover image'"
        loading="lazy"
      />
    </header>

    <section class="card-content">
      <p v-if="cardData.desc" class="desc">
        {{ cardData.desc }}
      </p>
      <slot name="content"></slot>

      <div v-if="cardData.title || cardData.author" class="meta">
        <h3 v-if="cardData.title" class="book-title">《{{ cardData.title }}》</h3>
        <p v-if="cardData.author" class="subtitle">{{ cardData.author }}</p>
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
          <Star :style="{ color: isFavorited ? '#fadb14' : '#606266' }" />
        </el-icon>
        <span v-if="!isFavorited">收藏</span>
        <span v-else>已收藏</span>
      </el-button>
      <el-button size="small" class="ghost-btn" @click="$emit('close')">
        我先休息一下
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { onMounted, reactive , ref} from 'vue';
// @ts-ignore
import MoodApiService from '/src/api/moodApi.js';
import { ElMessage } from 'element-plus';
import { Star } from '@element-plus/icons-vue';
// @ts-ignore
import FavoriteService from '/src/api/favoriteApi';

const cardData = reactive({
  id: null as number | null,
  cover: '',
  title: '',
  author: '',
  desc: ''
});
const isFavorited = ref(false);

const checkFavorite = async () => {
  const favoriteableType = 'Card';
  try {
    const response = await FavoriteService.checkFavorite(cardData.id, favoriteableType);
    isFavorited.value = response;
  } catch (error) {
    console.error('檢查收藏狀態失敗:', error);
    ElMessage.error('檢查收藏狀態失敗');
  }
};

const handleCardToCollection = async () => {
  const favorite = {
      favoriteableId: cardData.id,
      favoriteableType: 'Card',
      category: '卡片收藏'
    };
    const removeFavorite = {
      favoriteableId: cardData.id,
      favoriteableType: 'Card',
    }
  
    if( isFavorited.value){
      try {
        await FavoriteService.removeFavorite(removeFavorite);
        isFavorited.value = false;
        ElMessage.warning('已取消收藏');
      } catch (error) {
        console.error('取消收藏失敗:', error);
        ElMessage.error('取消收藏失敗');
      }
    } else {
      try {
        await FavoriteService.addFavorite(favorite);
        isFavorited.value = true;
        ElMessage.success('已收藏');
      } catch (error) {
        console.error('收藏失敗:', error);
        ElMessage.error('收藏失敗');
      } 
    }
}

async function getCardInfo() {
    const { id ,bookTitle, author, quoteText } = await MoodApiService.getTodayCard();
    Object.assign(cardData, {
      id,
      cover: 'src/assets/image/FT.jpg',
      title: bookTitle,
      author,
      desc: quoteText
    });
}

onMounted(async() => {
  await getCardInfo();
  await checkFavorite();
});
</script>

<style scoped>
/* 样式保持不变 */
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f5e8c7 0%, #e8d5b9 48%);
  color: #4a3c2f;
  border: 1px solid #d9c2a5;
  position: absolute;
}

.card-header {
  display: grid;
  grid-template-columns: 100%;
  padding: 28px 24px 8px;
  align-items: center;
}
.card-cover {
  margin: 0 auto 4px;
  width: 78%;
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-content {
  padding: 8px 20px 16px;
}
.desc {
  margin: 0;
  line-height: 1.8;
  font-size: 15.5px;
  color: #4a3c2f;
}
.meta {
  margin-top: 14px;
}
.book-title {
  margin: 0 0 4px;
  font-size: 16.5px;
  letter-spacing: 0.2px;
}
.subtitle {
  margin: 0;
  color: #7a6650;
  font-size: 13.5px;
}

.card-footer {
  display: flex;
  gap: 10px;
  padding: 10px 16px 18px;
  justify-content: flex-end;
}

.calm-btn.el-button {
  --el-color-primary: #8a6d3b;
  --el-button-bg-color: #f0e6d2;
  --el-button-text-color: #4a3c2f;
  --el-button-hover-bg-color: #e8d5b9;
  --el-button-active-bg-color: #d9c2a5;
  border: 1px solid #c4a87a;
}
.ghost-btn.el-button {
  background: transparent;
  color: #7a6650;
  border: 1px solid #d9c2a5;
}
.ghost-btn.el-button:hover {
  border-color: #c4a87a;
  color: #4a3c2f;
}

@media (max-width: 360px) {
  .card-header { padding: 20px 16px 6px; }
  .card-cover { width: 84%; border-radius: 10px; }
  .desc { font-size: 15px; line-height: 1.75; }
}
</style>