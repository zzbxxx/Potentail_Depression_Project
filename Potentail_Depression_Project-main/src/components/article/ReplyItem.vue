<template>
  <el-card class="reply-item">
    <!-- 评论头部 -->
    <div class="reply-header">
      <el-avatar :size="40" :src="reply.avatar || defaultAvatar" />
      <span class="nickname">{{ reply.nickname || '匿名' }}</span>
      <span class="created-at">{{ formatDate(reply.createdAt) }}</span>
    </div>

    <p v-if="parentNickname" class="reply-reference">
      回复 <span class="reference-nickname">@{{ parentNickname }}</span>：
    </p>

    <p class="reply-content">{{ reply.content }}</p>

    <!-- 操作区 -->
    <div class="reply-action">
      <div class="button-group">
        <el-button
          type="text"
          @click="toggleReplyForm(reply.id)"
          class="reply-btn"
        >
          {{ showReplyForm[reply.id] ? '取消回复' : '回复' }}
        </el-button>
        <el-button
          v-if="canDeleteReply(reply.userId)"
          type="text"
          @click="confirmDeleteReply(reply.id)"
          class="delete-btn"
        >
          删除
        </el-button>
      </div>

      <!-- 回复表单 -->
      <div v-if="showReplyForm[reply.id]" class="nested-reply-form">
        <el-input
          v-model="nestedReplyContent[reply.id]"
          type="textarea"
          :rows="3"
          placeholder="写下你的回复"
          maxlength="300"
          show-word-limit
        />
        <el-button
          type="primary"
          :disabled="!nestedReplyContent[reply.id]?.trim()"
          @click="submitNestedReply(reply.id)"
          class="submit-nested-btn"
        >
          {{ submittingNested[reply.id] ? '发送中...' : '发送回复' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { inject } from 'vue';

const {
  defaultAvatar,
  formatDate,
  toggleReplyForm,
  canDeleteReply,
  showReplyForm,
  nestedReplyContent,
  submittingNested,
  submitNestedReply,
  confirmDeleteReply
} = inject("replyContext");

const props = defineProps({
  reply: { type: Object, required: true },
  parentNickname: { type: String, default: '' }
});
</script>

<style scoped>
.reply-item {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  background: #ffffff;
  padding: 16px;
  transition: box-shadow 0.2s ease;
}

.reply-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.nickname {
  font-weight: 600;
  font-size: 15px;
  color: #333333;
}

.created-at {
  font-size: 12px;
  color: #999999;
  font-style: italic;
}

.reply-reference {
  font-size: 14px;
  color: #666666;
  margin-bottom: 6px;
}

.reference-nickname {
  color: #409EFF;
  font-weight: 500;
}

.reply-content {
  font-size: 14px;
  color: #444444;
  line-height: 1.6;
  margin-bottom: 10px;
}

.reply-action {
  margin-top: 6px;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.reply-btn {
  font-size: 13px;
  color: #606266;
}

.reply-btn:hover {
  color: #409EFF;
}

.delete-btn {
  font-size: 13px;
  color: #f56c6c;
}

.delete-btn:hover {
  color: #d63c3c;
}

.nested-reply-form {
  margin-top: 10px;
  padding: 10px;
  background: #fafafa;
  border-radius: 6px;
}

.submit-nested-btn {
  margin-top: 8px;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 4px;
}
</style>