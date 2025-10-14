<template>
  <div class="overlay" v-if="modelValue" @click.self="close">
    <div class="anno-hole">
      <div class="header">
        <h2>樹洞</h2>
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文章..."
            clearable
            @input="handleArticleSearch"
            @clear="resetFilters"
            style="width: 10rem;"
          />
          <el-select
            v-model="selectedTopic"
            multiple
            placeholder="選擇話題"
            class="custom-select"
            @change="handleTopicFilter"
          >
            <el-option
              v-for="item in topicOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <el-button type="danger" size="small" @click="close">關閉</el-button>
        </div>
      </div>

      <el-scrollbar height="calc(100% - 50px)">
        <div class="article-list">
          <el-card
            v-for="article in filteredArticles"
            :key="article.id"
            shadow="hover"
            class="article-card"
          >
            <ArticlePreview :article="article" />

            <div class="article-footer">
              <el-button text size="small" @click="openDetail(article)">
                查看詳情 &gt;
              </el-button>
              <span class="time">{{ formatTime(article.createdAt) }}</span>
            </div>

            <ActionBar
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
          </el-card>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, reactive, onBeforeUnmount, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import ActionBar from '/src/components/article/ActionBar.vue';
import ArticleService from '../api/articleApi.js';
import ArticlePreview from '/src/components/article/ArticlePreview.vue';

const menuVisible = reactive({});
const articles = ref([]);
const originalArticles = ref([]);
const searchQuery = ref('');
const selectedTopic = ref([]);
const topicOptions = ref(['美食', '旅遊', '學習', '生活', '科技']);

const filteredArticles = computed(() => {
  let result = [...originalArticles.value];

  if (selectedTopic.value.length > 0) {
    result = result.filter(article =>
      article.topics?.some(topic => selectedTopic.value.includes(topic))
    );
  }

  if (searchQuery.value) {
    const query = searchQuery.value.trim().toLowerCase();
    result = result.filter(article => {
      const titleMatch = article.title?.toLowerCase().includes(query);
      const previewMatch = firstTextBlock(article).toLowerCase().includes(query);
      return titleMatch || previewMatch;
    });
  }

  return result;
});

const handleToggleMenu = (article, val) => {
  const id = article.id;
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
  menuVisible[id] = val;
};

const closeAllMenu = () => {
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false));
};

onMounted(() => document.addEventListener('click', closeAllMenu));
onBeforeUnmount(() => document.removeEventListener('click', closeAllMenu));

const handleArticleReport = (article) => {
  ElMessage.warning(`已舉報文章 ${article.id}`);
};

const handleArticleDislike = (article) => {
  ElMessage.info(`將減少類似文章 ${article.id} 的推薦`);
};

const handleArticleDislikeAuthor = (article) => {
  ElMessage.info(`將減少作者 ${article.nickname || '匿名'} 的內容`);
};

async function getInfo() {
  try {
    const res = await ArticleService.getApprovedArticles();
    originalArticles.value = res.map(a => ({
      ...a,
      likes: a.likes || 0,
      liked: a.liked || false
    }));
    articles.value = [...originalArticles.value];
  } catch (e) {
    ElMessage.error('獲取文章失敗');
  }
}

function handleArticleSearch() {
  // 搜索邏輯已移到 computed 中
}

function handleTopicFilter() {
  // 過濾邏輯已移到 computed 中
}

function resetFilters() {
  searchQuery.value = '';
  selectedTopic.value = [];
  articles.value = [...originalArticles.value];
}

const handleArticleLike = (updatedArticle) => {
  const index = articles.value.findIndex(a => a.id === updatedArticle.id);
  if (index !== -1) {
    articles.value[index].liked = updatedArticle.liked;
    articles.value[index].likes = updatedArticle.likes;
  }
  const originalIndex = originalArticles.value.findIndex(a => a.id === updatedArticle.id);
  if (originalIndex !== -1) {
    originalArticles.value[originalIndex].liked = updatedArticle.liked;
    originalArticles.value[originalIndex].likes = updatedArticle.likes;
  }
};

const handleArticleShare = (article) => {
  ElMessage.success(`分享文章 ${article.id}`);
};

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

// 監聽 modelValue，當組件顯示時觸發 getInfo
watch(() => props.modelValue, (newVal) => {
  console.log('AnnoHole 顯示狀態:', newVal);
  if (newVal) {
    getInfo(); // 僅在組件顯示時調用 getInfo
  }
});

const close = () => {
  emit('update:modelValue', false);
};

const router = useRouter();
const openDetail = (article) => {
  router.push({ name: 'DetailPage', params: { id: article.id } });
  ElMessage.info(`跳轉到文章 ${article.id} 的詳情頁`);
};

const firstTextBlock = (article) => {
  const block = article.blocks?.find(b => b.type === 'text');
  if (!block) return '';
  const plain = block.content.replace(/<[^>]+>/g, '');
  return plain.slice(0, 50) + (plain.length > 50 ? '...' : '');
};

const formatTime = (t) => {
  return new Date(t).toLocaleString();
};
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 2000;
}
.anno-hole {
  background-color: #fff;
  width: 90%;
  max-width: 800px;
  height: 90vh;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #eee;
}
.article-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.article-card {
  border-radius: 8px;
}
.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}
.time {
  color: #999;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.custom-select {
  width: 10rem;
}
@media (max-width: 768px) {
  .anno-hole {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }
  .custom-select {
    width: 8rem;
  }
}
</style>
