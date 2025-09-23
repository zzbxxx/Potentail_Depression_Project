<template>
  <div class="notification-panel">
    <h3>通知</h3>
    <el-table
      :data="paginatedData"
      style="width: 100%"
      class="notification-table"
      :header-cell-style="{ background: '#e6f0fa', color: '#2C3E50' }"
      :loading="loading"
    >
      <el-table-column prop="title" label="通知标题" width="200">
        <template #default="{ row }">
          {{ row.title }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="timestamp" label="时间" width="180">
        <template #default="{ row }">
          {{ formatTimestamp(row.timestamp) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <span :class="{ 'unread': !row.read }">{{ row.read ? '已读' : '未读' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button
            v-if="!row.read"
            type="text"
            size="small"
            @click="markAsRead(row.id)"
          >
            标记为已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[5, 10, 15]"
      layout="total, sizes, prev, pager, next"
      :total="notifications.length"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useNotificationStore } from '/src/stores/notification';
import NotificationService from '/src/api/notificationApi';

const notificationStore = useNotificationStore();
const props = defineProps({
  userId: Number,
  filter: {
    type: String,
    default: 'all',
  },
});

const notifications = ref([]);
const currentPage = ref(1);
const pageSize = ref(5);
const loading = ref(false);

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return notifications.value.slice(start, end);
});

const formatTimestamp = (timestamp) => {
  return timestamp
    ? new Date(timestamp).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '';
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
};

const loadNotifications = async () => {
  const userId = localStorage.getItem('user_id') || localStorage.getItem('userId');
  if (!userId) {
    ElMessage.error('用户ID不可用');
    return;
  }
  loading.value = true;
  try {
    let response;
    if (props.filter === 'unread') {
      response = await NotificationService.getUnreadNotifications(userId);
    } else if (props.filter === 'read') {
      response = await NotificationService.getReadNotifications(userId);
    } else {
      response = await NotificationService.getNotifications(userId);
    }
    if (Array.isArray(response)) {
      notifications.value = response.map(n => ({
        ...n,
        read: n.read || false,
      }));
      if (props.filter === 'unread') {
        notificationStore.updateUnreadCount(response.length);
      } else if (props.filter === 'all') {
        notificationStore.initializeNotifications(response);
      }
    } else {
      throw new Error('返回数据格式错误');
    }
  } catch (error) {
    console.error('加载通知失败:', error);
    ElMessage.error(`加载通知失败: ${error.message}`);
  } finally {
    loading.value = false;
  }
};

const markAsRead = async (notificationId) => {
  try {
    await NotificationService.markAsRead(notificationId);
    notificationStore.markAsRead(notificationId);
    if (props.filter === 'unread') {
      notifications.value = notifications.value.filter(n => n.id !== notificationId);
      notificationStore.updateUnreadCount(notifications.value.length);
    }
    ElMessage.success('通知已标记为已读');
  } catch (error) {
    ElMessage.error(`标记失败: ${error.message}`);
  }
};

onMounted(() => {
  loadNotifications();
});

watch(() => notificationStore.unreadCount, (newCount) => {
  console.log('Unread Count:', newCount);
});
</script>

<style scoped>
.notification-panel { padding: 1rem; }
.notification-table {
  background-color: #f9fbfd;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}
.unread { color: #f56c6c; font-weight: bold; }
.pagination { display: flex; justify-content: center; }
@media (prefers-color-scheme: dark) {
  .notification-table { background-color: #2c3e50; }
}
</style>
