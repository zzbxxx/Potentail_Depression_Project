<template>
  <div class="article-audit">
    <h1 class="page-title">文章审核 - {{ getStatusText }}</h1>
    <el-table
      :data="tableData"
      style="width: 100%"
      class="audit-table"
      :header-cell-style="{ background: '#e6f0fa', color: '#2C3E50' }"
    >
      <el-table-column prop="id" label="文章ID" width="100" />
      <el-table-column prop="title" label="标题" width="200">
        <template #default="{ row }">
          {{ row.title || '无标题' }}
        </template>
      </el-table-column>
      <el-table-column prop="articleType" label="类型" width="120" />
      <el-table-column prop="topics" label="话题" width="200">
        <template #default="{ row }">
          {{ row.topics.join(', ') }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column prop="nickname" label="作者昵称" width="150">
        <template #default="{ row }">
          {{ row.nickname || '匿名' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            class="deep-sea-btn"
            @click="viewDetail(row)"
          >
            查看详细
          </el-button>
          <el-button
            v-if="status === 'pending'"
            type="success"
            size="small"
            class="deep-sea-btn"
            @click="passArticle(row.id)"
          >
            通过
          </el-button>
          <el-button
            v-if="status === 'pending'"
            type="danger"
            size="small"
            class="deep-sea-btn"
            @click="rejectArticle(row.id)"
          >
            不通过
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next"
      :total="articles.length"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
    <!-- 文章详情弹窗 -->
    <el-dialog
      v-model="showDetail"
      title="文章详情"
      width="70%"
      class="article-dialog"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @close="closeDetail"
    >
      <ArticleDisplay v-if="selectedArticle" :article="selectedArticle" />
      <template #footer>
        <el-button @click="closeDetail">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ArticleService from '/src/api/articleApi.js'
import ArticleDisplay from '/src/components/article/ArticleDisplay.vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
    default: 'pending'
  }
})

const articles = ref([])
const router = useRouter()
const currentPage = ref(1)
const pageSize = ref(10)
const showDetail = ref(false)
const selectedArticle = ref(null)

// 获取文章数据
async function getInfo() {
  try {
    let res;
    if (props.status === 'approved') {
      res = await ArticleService.getApprovedArticles()
    } else if (props.status === 'rejected') {
      res = await ArticleService.getRejectedArticles() 
    } else {
      res = await ArticleService.getPendingArticles()
    }
    articles.value = res.map(a => ({
      ...a,
      likes: 0,
      loves: 0,
      liked: false
    }))
  } catch (e) {
    ElMessage.error(`获取${getStatusText}文章失败`)
    console.error(e)
  }
}

// 计算分页后的表格数据
const tableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return articles.value.slice(start, end)
})

// 状态文本
const getStatusText = computed(() => {
  switch (props.status) {
    case 'approved':
      return '通过'
    case 'rejected':
      return '未通过'
    case 'pending':
    default:
      return '待通过'
  }
})

// 每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 查看文章详情
const viewDetail = (row) => {
  selectedArticle.value = row
  showDetail.value = true
}

// 关闭详情
const closeDetail = () => {
  showDetail.value = false
  selectedArticle.value = null
}

// 通过文章
const passArticle = async (articleId) => {
  try {
    await ArticleService.updateArticleStatus(articleId, 'APPROVED')
    ElMessage.success('文章已通过')
    getInfo() 
  } catch (e) {
    ElMessage.error('通过文章失败')
    console.error(e)
  }
}

// 不通过文章
const rejectArticle = async (articleId) => {
  try {
    await ArticleService.updateArticleStatus(articleId, 'REJECTED')
    ElMessage.success('文章已拒绝')
    getInfo()
  } catch (e) {
    ElMessage.error('拒绝文章失败')
    console.error(e)
  }
}

onMounted(() => {
  getInfo()
})

watch(() => props.status, () => {
  getInfo()
})
</script>

<style scoped>
/* 保持原有样式不变 */
.article-audit {
  padding: 1rem;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #2C3E50;
  margin-bottom: 1.5rem;
  text-align: center;
}

.audit-table {
  background-color: #f9fbfd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.deep-sea-btn {
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  border-radius: 6px;
  transition: all 0.3s ease;
  margin-right: 8px;
}

.deep-sea-btn:hover {
  background: linear-gradient(135deg, #0d3b66 0%, #1a5f9c 100%);
  transform: translateY(-2px);
}

.pagination {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
}

.article-dialog {
  background-color: #f9fbfd;
  border-radius: 8px;
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .page-title {
    color: #E0E0E0;
  }

  .audit-table {
    background-color: #2c3e50;
  }

  .article-dialog {
    background-color: #2c3e50;
  }
}
</style>