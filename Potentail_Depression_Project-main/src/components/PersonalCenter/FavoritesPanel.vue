<template>
  <div class="favorites-container">
    <h3 v-if="activeTab === 'favorites-articles'">文章收藏</h3>
    <h3 v-else-if="activeTab === 'favorites-cards'">卡片收藏</h3>

    <!-- 文章收藏 -->
    <div v-if="activeTab === 'favorites-articles'">
      <el-row :gutter="20">
        <el-col
          :span="24"
          v-for="item in pagedArticles"
          :key="item.favoriteId"
        >
          <el-card @click="handleArticleClick(item.favoriteableId)" shadow="hover" class="article-card">
            <div class="article-header">
              <el-avatar :src="item.authorAvatar || 'https://via.placeholder.com/50'" size="small" />
              <span class="nickname">{{ item.authorNickname || '匿名' }}</span>
              <span class="time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <h4 class="title">{{ item.title }}</h4>
            <p class="preview">{{ extractPreview(item.contentPreview) }}</p>
          </el-card>
        </el-col>
      </el-row>
      
      <p v-if="!articleFavorites.length" class="empty">暂无收藏文章</p>

      <!-- 分页 -->
      <el-pagination
        v-if="articleFavorites.length"
        layout="total, sizes, prev, pager, next, jumper"
        :total="articleFavorites.length"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        v-model:current-page="articlePage"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </div>

    <!-- 卡片收藏 -->
    <div v-else-if="activeTab === 'favorites-cards'">
      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          v-for="item in pagedCards"
          :key="item.favoriteId"
        >
          <el-card shadow="hover" class="card-fav">
            <article class="card" @click="handleCardClick(item.favoriteableId)">
              <header class="card-header">
                <img
                  class="card-cover"
                  src="/src/assets/image/FT.jpg"
                  :alt="item.contentPreview || 'cover image'"
                  loading="lazy"
                />
              </header>
              <section class="card-content">
                <div class="meta">
                  <h3 v-if="item.contentPreview" class="book-title">《{{ item.contentPreview }}》</h3>
                  <p class="time">{{ formatTime(item.createdAt) }}</p>
                </div>
              </section>
              <footer class="card-footer">
                <el-button
                  type="primary"
                  size="small"
                  class="calm-btn"
                  @click.stop="handleCardToCollection(item.favoriteableId, item.favoriteableType, item.favoriteId)"
                >
                  <el-icon :size="16">
                    <Star :style="{ color: '#fadb14' }" />
                  </el-icon>
                  <span>取消收藏</span>
                </el-button>
                <el-button
                  size="small"
                  class="ghost-btn"
                  @click.stop="handleCardClick(item.favoriteableId)"
                >
                  查看詳情
                </el-button>
              </footer>
            </article>
          </el-card>
        </el-col>
      </el-row>
      <p v-if="!cardFavorites.length" class="empty">暂无收藏卡片</p>

      <!-- 分页 -->
      <el-pagination
        v-if="cardFavorites.length"
        layout="total, sizes, prev, pager, next, jumper"
        :total="cardFavorites.length"
        :page-sizes="[3, 6, 9, 12]"
        :page-size="pageSize"
        v-model:current-page="cardPage"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref, computed, onMounted, watch } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { Star } from "@element-plus/icons-vue"
import FavoriteService from "/src/api/favoriteApi"
import { useRouter } from "vue-router"
import MoodApiService from "/src/api/moodApi"
const router = useRouter()
const props = defineProps({
  activeTab: { type: String, required: true }
})

const articleFavorites = ref([])
const cardFavorites = ref([])

// 分页状态
const pageSize = ref(3) // 默認每頁 4 個卡片
const articlePage = ref(1)
const cardPage = ref(1)

// 计算分页数据
const pagedArticles = computed(() => {
  const start = (articlePage.value - 1) * pageSize.value
  return articleFavorites.value.slice(start, start + pageSize.value)
})
const pagedCards = computed(() => {
  const start = (cardPage.value - 1) * pageSize.value
  return cardFavorites.value.slice(start, start + pageSize.value)
})

const resetPage = () => {
  if (props.activeTab === 'favorites-articles') {
    articlePage.value = 1
  } else if (props.activeTab === 'favorites-cards') {
    cardPage.value = 1
  }
}

const handleArticleClick = (favoriteableId) => {
  router.push({ name: 'DetailPage', params: { id: favoriteableId } })
}

const handleCardClick = (favoriteableId) => {
  const contentId = favoriteableId
  
  const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
  const res = MoodApiService.getCardByCardId(userId, contentId)
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  articlePage.value = 1
  cardPage.value = 1
}

const handleCardToCollection = async (favoriteableId, favoriteableType, favoriteId) => {
  const favorite = {
    favoriteableId,
    favoriteableType,
    category: '卡片收藏'
  }

  try {
    // 彈出確認框
    await ElMessageBox.confirm(
      '確定要取消收藏此卡片嗎？',
      '取消收藏',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 執行取消收藏
    const response = await FavoriteService.removeFavorite(favorite)
    if (response.code === 0) {
      ElMessage.success('取消收藏成功')
      await favoriteList() // 刷新卡片收藏列表
    } else {
      ElMessage.error(response.message || '取消收藏失敗')
    }
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消操作')
    } else {
      console.error('取消收藏失敗:', error)
      ElMessage.error('取消收藏失敗')
    }
  }
}

// 监听 activeTab 变化，重置分页并加载数据
watch(() => props.activeTab, () => {
  resetPage()
  favoriteList()
})

// 获取收藏列表
const favoriteList = async () => {
  const userId = localStorage.getItem("userId") || localStorage.getItem("user_id")
  if (!userId) {
    ElMessage.error("未找到用戶 ID")
    return
  }

  try {
    if (props.activeTab === 'favorites-articles') {
      const type = "ARTICLE"
      const category = "文章收藏"
      const res = await FavoriteService.getFavoriteList(userId, type, category)
      articleFavorites.value = Array.isArray(res) ? res : []
    } else if (props.activeTab === 'favorites-cards') {
      const type = "CARD"
      const category = "卡片收藏"
      const res = await FavoriteService.getFavoriteList(userId, type, category)
      cardFavorites.value = Array.isArray(res) ? res : []
    }
  } catch (e) {
    console.error('获取收藏列表失败:', e)
    ElMessage.error("获取收藏列表失败")
  }
}

onMounted(() => {
  favoriteList()
})

// 过滤预览内容
const extractPreview = (content) => {
  if (!content) return ""
  try {
    const blocks = JSON.parse(content)
    if (!Array.isArray(blocks)) return content
    const textBlock = blocks.find(b => b.type === "text")
    if (textBlock) {
      const plain = textBlock.content.replace(/<[^>]+>/g, "")
      return plain.slice(0, 80) + (plain.length > 80 ? "..." : "")
    }
    return content.slice(0, 80) + (content.length > 80 ? "..." : "")
  } catch (e) {
    const plain = content.replace(/<[^>]+>/g, "")
    return plain.slice(0, 80) + (plain.length > 80 ? "..." : "")
  }
}

const formatTime = (t) => new Date(t).toLocaleString()
</script>

<style scoped>
.favorites-container {
  max-height: 86vh;
  overflow-y: auto;
  padding: 12px;
  box-sizing: border-box;
  width: 100%;
}

.article-card, .card-fav {
  border-radius: 6px;
  margin-bottom: 20px;
  padding: 10px;
  width: 100%;
  box-sizing: border-box;
}

.article-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.nickname { font-size: 14px; color: #666; }
.title { font-weight: 600; margin: 6px 0; }
.preview { font-size: 13px; color: #444; margin-bottom: 6px; }
.time { font-size: 12px; color: #999; margin-left: auto; }
.empty { color: #999; text-align: center; margin-top: 20px; }

.pagination {
  margin: 16px auto;
  display: flex;
  justify-content: center;
}

/* 卡片样式 */
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f5e8c7 0%, #e8d5b9 48%);
  color: #4a3c2f;
  border: 1px solid #d9c2a5;
  cursor: pointer;
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: grid;
  grid-template-columns: 100%;
  padding: 20px 16px 8px;
  align-items: center;
}

.card-cover {
  margin: 0 auto 4px;
  width: 100%;
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-content {
  padding: 8px 16px 16px;
}

.meta {
  margin-top: 8px;
}

.book-title {
  margin: 0 0 4px;
  font-size: 16px;
  letter-spacing: 0.2px;
  font-weight: 600;
}

.time {
  margin: 0;
  color: #7a6650;
  font-size: 12px;
}

.card-footer {
  display: flex;
  gap: 8px;
  padding: 8px 12px 12px;
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

@media (max-width: 768px) {
  .card-fav {
    margin-bottom: 16px;
  }
}

@media (max-width: 360px) {
  .card-header {
    padding: 16px 12px 6px;
  }
  .card-cover {
    width: 100%;
    border-radius: 10px;
  }
  .book-title {
    font-size: 14px;
  }
  .time {
    font-size: 11px;
  }
}
</style>