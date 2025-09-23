<template>
  <div class="reply-section flat-style">
    <h3 class="reply-title">私密留言板</h3>
    <p class="intro-text">这是一个与作者的私密交流空间，仅作者可见，放心分享你的想法！</p>

    <!-- 顶层留言输入框 -->
    <div class="reply-form-card">
      <el-input
        v-model="newReply"
        type="textarea"
        :rows="4"
        placeholder="写下你的支持或想法（仅作者可见，最多500字）"
        maxlength="500"
        show-word-limit
        class="reply-input"
      />
      <el-button
        type="primary"
        :disabled="!newReply.trim() || submitting"
        @click="submitReply"
        class="submit-btn"
        :loading="submitting"
      >
        {{ submitting ? '发送中...' : '发送留言' }}
      </el-button>
    </div>

    <!-- 留言列表（父 + 子都平铺展示） -->
    <div class="reply-list">
      <div
        v-for="reply in flatReplies"
        :key="reply.id"
        class="reply-card"
      >
        <ReplyItem :reply="reply" :parentNickname="reply.parentNickname" />
      </div>
      <p v-if="!flatReplies.length" class="no-replys">暂无留言，赶紧成为第一个留言的人吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import ArticleService from '/src/api/articleApi';
import NotificationService from '/src/api/notificationApi';
import ReplyItem from './ReplyItem.vue';

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  authorId: { type: [String, Number], required: true },
});

const newReply = ref('');
const replyTree = ref([]);
const flatReplies = ref([]);
const submitting = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png';
const isAuthor = ref(false);

const showReplyForm = ref({});
const nestedReplyContent = ref({});
const submittingNested = ref({});

const checkIsAuthor = () => {
  const currentUserId = localStorage.getItem('user_id') || localStorage.getItem('userId');
  isAuthor.value = currentUserId && currentUserId == props.authorId;
};

const canDeleteReply = (replyUserId) => {
  const currentUserId = localStorage.getItem('user_id') || localStorage.getItem('userId');
  return currentUserId && (currentUserId == replyUserId || isAuthor.value);
};

// 拉平树状结构
const flatten = (list, parentNickname = '') => {
  return list.reduce((acc, item) => {
    acc.push({ ...item, parentNickname });
    if (item.children?.length) {
      acc.push(...flatten(item.children, item.nickname));
    }
    return acc;
  }, []);
};

// 获取留言
const fetchReplies = async () => {
  try {
    const userId = localStorage.getItem('user_id') || localStorage.getItem('userId');
    if (!userId) {
      ElMessage.warning('请登录后查看私密留言');
      return;
    }
    const res = await ArticleService.getArticleReplyInfo(props.articleId, userId);
    if (!res.success) throw new Error(res.message);

    const flatReplys = res.data || [];
    const replyMap = new Map();
    flatReplys.forEach(r => {
      r.children = [];
      replyMap.set(r.id, r);
    });

    const roots = [];
    flatReplys.forEach(r => {
      if (!r.parentId) roots.push(r);
      else replyMap.get(r.parentId)?.children.push(r);
    });

    replyTree.value = roots;
    flatReplies.value = flatten(roots);
  } catch (e) {
    console.error(e);
    ElMessage.error('获取留言失败：' + e.message);
  }
};

// 顶层留言
const submitReply = async () => {
  if (!newReply.value.trim()) return ElMessage.warning('留言内容不能为空');
  submitting.value = true;
  try {
    const userId = localStorage.getItem('user_id') || localStorage.getItem('userId');
    const res = await ArticleService.putArticleReplyInfo({
      userId,
      articleId: props.articleId,
      content: newReply.value,
    });
    // 发送通知给作者
    if (userId != props.authorId) { // 避免自己给自己发通知
      await NotificationService.createNotification({
        userId: props.authorId,
        title: '新留言',
        content: `用户在您的文章（ID: ${props.articleId}）下发表了新留言`,
        notificationType: 'COMMENT'
      });
    }
    fetchReplies();
    newReply.value = '';
    ElMessage.success('留言发送成功！');
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试');
    console.error('提交留言失败:', e);
  } finally {
    submitting.value = false;
  }
};

// 删除
const confirmDeleteReply = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此留言吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    const userId = localStorage.getItem('user_id') || localStorage.getItem('userId');
    const res = await ArticleService.deleteArticleReply(id, userId);
    if (res.success) {
      fetchReplies();
      ElMessage.success('删除成功！');
    }
  } catch {
    ElMessage.error('删除失败，请重试');
  }
};

const toggleReplyForm = (id) => {
  showReplyForm.value[id] = !showReplyForm.value[id];
  if (!showReplyForm.value[id]) nestedReplyContent.value[id] = '';
};

const submitNestedReply = async (parentId) => {
  if (!nestedReplyContent.value[parentId]?.trim()) return ElMessage.warning('回复不能为空');
  submittingNested.value[parentId] = true;
  try {
    const userId = localStorage.getItem('user_id') || localStorage.getItem('userId');
    await ArticleService.putArticleReplyInfo({
      userId,
      articleId: props.articleId,
      content: nestedReplyContent.value[parentId],
      parentId,
    });
    // 发送通知给作者
    if (userId != props.authorId) { // 避免自己给自己发通知
      await NotificationService.createNotification({
        userId: props.authorId,
        title: '新回复',
        content: `用户在您的文章（ID: ${props.articleId}）下回复了您的留言`,
        notificationType: 'COMMENT'
      });
    }
    fetchReplies();
    nestedReplyContent.value[parentId] = '';
    showReplyForm.value[parentId] = false;
    ElMessage.success('回复成功！');
  } catch (e) {
    ElMessage.error('回复失败，请稍后重试');
    console.error('提交回复失败:', e);
  } finally {
    submittingNested.value[parentId] = false;
  }
};

const formatDate = (str) => new Date(str).toLocaleString('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

provide('replyContext', {
  defaultAvatar,
  formatDate,
  toggleReplyForm,
  canDeleteReply,
  showReplyForm,
  nestedReplyContent,
  submittingNested,
  submitNestedReply,
  confirmDeleteReply,
});

onMounted(() => {
  checkIsAuthor();
  fetchReplies();
});
</script>

<style scoped>
.reply-section.flat-style {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
}

.reply-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 10px;
  border-bottom: 1px solid #e1e1e1;
  padding-bottom: 8px;
}

.intro-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.reply-form-card {
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #eee;
}

.reply-input {
  margin-bottom: 12px;
}

.submit-btn {
  width: 100%;
}

.reply-list {
  max-height: 600px;
  overflow-y: auto;
}

.reply-card {
  margin-bottom: 12px;
}

.no-replys {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 4px;
}
</style>