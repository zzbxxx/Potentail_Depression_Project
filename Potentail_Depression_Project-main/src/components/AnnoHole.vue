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

            <ActionBar
              :article="article"
              :show-actions="{
                like: true,
                favorite: true,
                share: true,
                love: true,
                more: true
              }"
              :gap="8"
              @like="like"
              @favorite="favorite"
              @share="share"
              @love="love"
              @report="report"
              @dislikeArticle="dislikeArticle"
              @dislikeAuthor="dislikeAuthor"
              @toggleMenu="val => toggleMenu(article, val)"
            />
          </el-card>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ActionBar from '/src/components/article/ActionBar.vue'
import ArticleService from '../api/articleApi.js'

const menuVisible = reactive({})
const articles = ref([])

const toggleMenu = (article, val) => {
  const id = article.id
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false))
  menuVisible[id] = val
}

const closeAllMenu = () => {
  Object.keys(menuVisible).forEach(k => (menuVisible[k] = false))
}
onMounted(() => document.addEventListener('click', closeAllMenu))
onBeforeUnmount(() => document.removeEventListener('click', closeAllMenu))

const report = (article) => {
  ElMessage.warning(`已举报文章 ${article.id}`)
}
const dislikeArticle = (article) => {
  ElMessage.info(`将减少类似文章 ${article.id} 的推荐`)
}
const dislikeAuthor = (article) => {
  ElMessage.info(`将减少作者 ${article.nickname || '匿名'} 的内容`)
}

async function getInfo() {
  try {
    const res = await ArticleService.getAllArticles()
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
const router = useRouter()
const openDetail = (article) => {
  router.push({ name: 'DetailPage', params: { id: article.id } })
  ElMessage.info(`跳转到文章 ${article.id} 的详情页`)
}

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
}
const share = (article) => {}
const favorite = (article) => {}

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
.time {
  color: #999;
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
</style>