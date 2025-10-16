<template>
  <div class="article-manage">
    <h3>文章管理</h3>

    <el-row :gutter="20">
      <el-col
        :span="24"
        v-for="article in pagedArticles"
        :key="article.id"
      >
        <el-card
          shadow="hover"
          class="article-card"
          @click="openDetail(article)"
        >

          <ArticlePreview :article="article" />

          <div class="article-footer">
            <span class="time">{{ formatTime(article.createdAt) }}</span>

            <div class="actions">
              <el-button
                text
                type="primary"
                size="small"
                @click.stop="editArticle(article)"
              >
                編輯
              </el-button>

              <el-switch
                v-model="article.isPublicInFollow"
                active-text="公開"
                inactive-text="私密"
                inline-prompt
                @click.stop
                @change="toggleVisibility(article, $event)"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <p v-if="!articles.length" class="empty">暫無文章</p>

    <!-- 分頁 -->
    <el-pagination
      v-if="articles.length"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="articles.length"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { ElMessage } from "element-plus"
import ArticlePreview from "/src/components/article/ArticlePreview.vue"
import ArticleService from "/src/api/ArticleApi"

const router = useRouter()
const articles = ref([])

// 分頁控制
const currentPage = ref(1)
const pageSize = ref(10)
const pagedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return articles.value.slice(start, start + pageSize.value)
})

const getArticlesByUserId = async () => {
  const userId = localStorage.getItem("userId") || localStorage.getItem("user_id")
  if (!userId) {
    ElMessage.error("未找到用戶 ID")
    return
  }

  try {
    const response = await ArticleService.getArticlesByUserId(userId)
    articles.value = response
  } catch (e) {
    console.error("獲取文章失敗", e)
    ElMessage.error("獲取文章失敗")
  }
}

onMounted(() => {
  getArticlesByUserId()
})

// 操作方法
const openDetail = (article) => {
  router.push({ name: "DetailPage", params: { id: article.id } })
}

const editArticle = (article) => { 
  router.push({ name: "Editor", params: { id: article.id } })
}

const toggleVisibility = async (article, isPublicInFollow) => {
  const originalStatus = article.isPublicInFollow // 保存原始狀態
  article.isPublicInFollow = isPublicInFollow // 樂觀更新

  const userId = localStorage.getItem("userId") || localStorage.getItem("user_id")
  if (!userId) {
    ElMessage.error("未找到用戶 ID")
    article.isPublicInFollow = originalStatus // 恢復狀態
    return
  }

  try {
    const response = await ArticleService.updatePublicStatus(article.id, userId, isPublicInFollow)
    if (response.success) {
      ElMessage.success(`文章「${article.title}」現在是 ${isPublicInFollow ? "公開" : "私密"}`)
    } else {
      ElMessage.error(response.message || "切換公開狀態失敗")
      article.isPublicInFollow = originalStatus // 恢復狀態
    }
  } catch (e) {
    console.error("切換公開狀態失敗:", e)
    ElMessage.error("切換公開狀態失敗")
    article.isPublicInFollow = originalStatus // 恢復狀態
  }
}

// 分頁操作
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const formatTime = (t) => new Date(t).toLocaleString()
</script>

<style scoped>
.article-manage {
  padding: 16px;
  max-height: 80vh;
  overflow-y: auto;
}
.article-card {
  margin-bottom: 16px;
  border-radius: 8px;
}
.article-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.article-footer .actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.time {
  font-size: 12px;
  color: #999;
}
.empty {
  color: #999;
  text-align: center;
  margin-top: 20px;
}
.pagination {
  margin: 16px auto;
  display: flex;
  justify-content: center;
}
</style>