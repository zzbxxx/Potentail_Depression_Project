<!-- AnnoHole.vue -->
<template>
  <div
    class="overlay"
    v-if="modelValue"
    @click.self="close"
  >
    <div class="anno-hole">
      <div class="header">
        <h2>樹洞</h2>
        <el-button type="danger" size="small" @click="close">关闭</el-button>
      </div>

      <el-scrollbar height="calc(100% - 50px)">
        <div class="article-list">
          <el-card
            v-for="article in articles"
            :key="article.id"
            shadow="hover"
            class="article-card"
          >
            <div class="article-header">
              <el-avatar
                v-if="article.avatar"
                :src="article.avatar"
                size="small"
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

            <h3 class="article-title">{{ article.title || '未命名标题' }}</h3>

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

            <div class="article-footer">
              <el-button text size="small" @click="openDetail(article)">
                查看详情 &gt;
              </el-button>
              <span class="time">{{ formatTime(article.createdAt) }}</span>
            </div>

            <div class="action-bar">
              <el-button size="small" @click="like(article)" :type="article.liked ? 'primary' : 'default'">
                赞同 {{ article.likes || 0 }}
              </el-button>
              <el-button size="small" text @click="favorite(article)">
                收藏
              </el-button>
              <el-button size="small" text @click="share(article)">
                分享
              </el-button>
              <el-button size="small" text @click="love(article)">
                喜欢 {{ article.loves || 0 }}
              </el-button>
              <div class="more-box">
                    <el-button size="small" text @click.stop="toggleMenu(article)">
                    <el-icon><MoreFilled /></el-icon>
                    </el-button>

                    <!-- 原生弹出层 -->
                    <div v-show="menuVisible[article.id]" class="more-menu" @click.stop>
                    <div class="more-item" @click="report(article)">举报</div>
                    <div class="more-item" @click="dislikeArticle(article)">不喜欢文章</div>
                    <div class="more-item" @click="dislikeAuthor(article)">不喜欢作者</div>
                    </div>
                </div>
            </div>
          </el-card>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted,reactive,onBeforeUnmount  } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import ArticleService from '../api/articleApi.js'
const menuVisible = reactive({}) 
const articles = ref([])

const toggleMenu = (article) => {
  const id = article.id
  // 先全部关掉，再切换当前
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false))
  menuVisible[id] = !menuVisible[id]
}

/* ---------- 点空白收拢 ---------- */
const closeAllMenu = () => {
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false))
}
onMounted(() => document.addEventListener('click', closeAllMenu))
onBeforeUnmount(() => document.removeEventListener('click', closeAllMenu))

/* ---------- 三项功能 ---------- */
const report = (article) => {
  ElMessage.warning(`已举报文章 ${article.id}`)
  menuVisible[article.id] = false
}
const dislikeArticle = (article) => {
  ElMessage.info(`将减少类似文章 ${article.id} 的推荐`)
  menuVisible[article.id] = false
}
const dislikeAuthor = (article) => {
  ElMessage.info(`将减少作者 ${article.nickname || '匿名'} 的内容`)
  menuVisible[article.id] = false
}

async function getInfo() {
  try {
    const res = await ArticleService.getAllArticles()
    // 给每篇文章加初始状态
    articles.value = res.map(a => ({
      ...a,
      likes: 0,
      loves: 0,
      liked: false
    }))
  } catch (e) {
    ElMessage.error('获取文章失败')
  }
}

onMounted(() => {
  getInfo()
})

const router = useRouter()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

watch(() => props.modelValue, (newVal) => {
  console.log('AnnoHole 显示状态:', newVal)
})

const close = () => {
  emit('update:modelValue', false)
}

const openDetail = (article) => {
  // router.push({ name: 'DetailPage', params: { id: article.id } })
  ElMessage.info(`跳转到文章 ${article.id} 的详情页`)
}

// 操作方法
const like = (article) => {
  if (!article.liked) {
    article.likes++
    article.liked = true
  } else {
    article.likes--
    article.liked = false
  }
}
const love = (article) => {
  article.loves++
  ElMessage.success('已喜欢~')
}
const share = (article) => {
  ElMessage.success(`分享文章 ${article.id}`)
}
const favorite = (article) => {
  ElMessage.success(`收藏文章 ${article.id}`)
}

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

const formatTime = (t) => {
  return new Date(t).toLocaleString()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 23000;
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
.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}
.action-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}
.time {
  color: #999;
}
.el-dropdown-link {
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  padding: 4px;
}
.el-dropdown-link:hover {
  color: #000;
  background-color: #f5f5f5;
  border-radius: 4px;
}
@media (max-width: 768px) {
  .anno-hole {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }
  .article-title {
    font-size: 16px;
  }
}

/* 更多按钮容器 */
.more-box {
  position: relative;
  display: inline-block;
}

/* 弹出菜单 */
.more-menu {
  position: absolute;
  right: 0;
  bottom: 100%;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
  padding: 4px 0;
  z-index: 10;
  white-space: nowrap;
}
.more-item {
  padding: 6px 16px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}
.more-item:hover {
  background: #f5f7fa;
}
</style>