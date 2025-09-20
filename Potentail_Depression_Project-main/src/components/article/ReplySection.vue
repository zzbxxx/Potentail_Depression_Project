<template>
  <div class="comment-section">
    <h3>私密留言</h3>
    <p class="intro-text">这里是与作者的私密交流空间，你的留言只有作者能看到，安心分享你的想法吧！</p>
    
    <!-- 提交评论 -->
    <div class="comment-form">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="4"
        placeholder="写下你的支持或想法，只有作者能看到"
        maxlength="500"
        show-word-limit
      />
      <el-button
        type="primary"
        :disabled="!newComment.trim() || submitting"
        @click="submitComment"
        class="submit-btn"
      >
        {{ submitting ? '发送中...' : '发送留言' }}
      </el-button>
    </div>

    <!-- 评论列表（仅作者可见） -->
    <div v-if="isAuthor" class="comment-list">
      <el-card v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <el-avatar :size="40" :src="comment.avatar || defaultAvatar" />
          <span class="nickname">{{ comment.nickname || '匿名' }}</span>
          <span class="created-at">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <p class="comment-content">{{ comment.content }}</p>
      </el-card>
      <p v-if="!comments.length" class="no-comments">暂无留言</p>
    </div>
    <p v-else class="non-author-tip">你的留言已发送，只有作者能看到所有留言内容。</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// import CommentService from '/src/api/commentApi'

const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  },
  authorId: { 
    type: [String, Number],
    required: true
  }
})

const newComment = ref('')
const comments = ref([])
const submitting = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png'
const isAuthor = ref(false)

// 假数据
const mockComments = [
  {
    id: 1,
    articleId: props.articleId,
    userId: 55,
    nickname: '支持者A',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png',
    content: '你的文章让我感到很温暖，谢谢分享！',
    createdAt: '2025-09-18T20:15:07'
  },
  {
    id: 2,
    articleId: props.articleId,
    userId: 55,
    nickname: '支持者B',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png',
    content: '希望你一切都好，有什么想聊的随时说哦！',
    createdAt: '2025-09-18T20:30:00'
  }
]

// 检查当前用户是否为作者
const checkIsAuthor = () => {
  const currentUserId = localStorage.getItem('user_id')
  isAuthor.value = currentUserId && currentUserId == props.authorId
}

// 获取评论列表（仅作者可见）
const fetchComments = async () => {
  if (!isAuthor.value) {
    comments.value = [] // 非作者不显示评论
    return
  }
  try {
    // 暂时使用假数据
    comments.value = mockComments
    // 实际后端接口调用（注释掉，待后端实现）
    /*
    const response = await CommentService.getPrivateComments(props.articleId, localStorage.getItem('user_id'))
    comments.value = response.data
    */
  } catch (error) {
    console.error('获取留言失败:', error)
    ElMessage.error('获取留言失败')
  }
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('留言内容不能为空')
    return
  }
  submitting.value = true
  try {
    // 模拟提交评论
    const newCommentData = {
      id: comments.value.length + 1,
      articleId: props.articleId,
      userId: localStorage.getItem('user_id'),
      content: newComment.value,
    }
    comments.value.push(newCommentData) // 添加到假数据
    ElMessage.success('留言发送成功')
    newComment.value = ''

    // 实际后端接口调用（注释掉，待后端实现）
    /*
    await CommentService.submitPrivateComment({
      articleId: props.articleId,
      content: newComment.value,
      userId: localStorage.getItem('user_id')
    })
    ElMessage.success('留言发送成功')
    newComment.value = ''
    await fetchComments() // 刷新评论列表
    */
  } catch (error) {
    console.error('发送留言失败:', error)
    ElMessage.error('发送留言失败')
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// 初始化
onMounted(() => {
  checkIsAuthor()
  fetchComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}
.intro-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
}
.comment-form {
  margin-bottom: 20px;
}
.submit-btn {
  margin-top: 10px;
  background: #67c23a;
  border-color: #67c23a;
}
.comment-list {
  max-height: 400px;
  overflow-y: auto;
}
.comment-item {
  margin-bottom: 10px;
  background: #fff;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.nickname {
  font-weight: bold;
  color: #333;
}
.created-at {
  color: #999;
  font-size: 12px;
}
.comment-content {
  margin: 0;
  color: #555;
}
.no-comments, .non-author-tip {
  text-align: center;
  color: #999;
  font-size: 14px;
}
</style>