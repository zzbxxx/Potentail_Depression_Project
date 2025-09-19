<template>
  <div class="author-info">
    <el-avatar :size="40" :src="author.avatar || defaultAvatar" />
    <div class="author-details">
      <span class="nickname">{{ author.nickname || '匿名' }}</span>
      <span class="created-at">{{ formatDate(author.createdAt) }}</span>
    </div>
  </div>
</template>

<script setup>
import { ElAvatar } from 'element-plus'

defineProps({
  author: {
    type: Object,
    required: true,
    default: () => ({
      nickname: null,
      avatar: null,
      createdAt: null
    })
  }
})

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png'

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid #ebebeb;
  margin-top: 20px;
}
.author-details {
  display: flex;
  flex-direction: column;
}
.nickname {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}
.created-at {
  color: #999;
  font-size: 12px;
}
</style>