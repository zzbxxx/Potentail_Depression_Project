<template>
  <div class="follow-detail">
    <!-- 關注的人 -->
    <div v-if="activeTab === 'follower'">
      <h3>關注的人</h3>
      <el-table :data="followingList" style="width: 100%" v-loading="loading">
        <el-table-column label="頭像" width="80">
          <template #default="scope">
            <img
              :src="scope.row.avatar || '/src/assets/image/FT.jpg'"
              alt="用戶頭像"
              class="avatar"
              @click="handleUserClick(scope.row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用戶名" />
        <el-table-column prop="nickname" label="暱稱" />
        <el-table-column prop="joinDate" label="加入日期" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <div class="button-group">
              <el-button
                type="danger"
                size="small"
                @click="handleUnfollow(scope.row.id)"
              >
                取消關注
              </el-button>
              <el-button
                type="primary"
                size="small"
                @click="handleSendMessage(scope.row.id)"
              >
                發送信息
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!followingList.length" class="empty">暫無關注用戶</p>
    </div>

    <!-- 關注我的 -->
    <div v-else-if="activeTab === 'followed'">
      <h3>關注我的</h3>
      <el-table :data="followedList" style="width: 100%" v-loading="loading">
        <el-table-column label="頭像" width="80">
          <template #default="scope">
            <img
              :src="scope.row.avatar || '/src/assets/image/FT.jpg'"
              alt="用戶頭像"
              class="avatar"
              @click="handleUserClick(scope.row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用戶名" />
        <el-table-column prop="nickname" label="暱稱" />
        <el-table-column prop="joinDate" label="加入日期" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <div class="button-group">
              <el-button
                v-if="!scope.row.isFollowing"
                type="primary"
                size="small"
                @click="handleFollow(scope.row.id)"
              >
                回關
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                已關注
              </el-button>
              <el-button
                type="primary"
                size="small"
                @click="handleSendMessage(scope.row.id)"
              >
                發送信息
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!followedList.length" class="empty">暫無關注你的用戶</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import FollowService from '/src/api/followApi';

const router = useRouter();
const props = defineProps({
  activeTab: {
    type: String,
    required: true,
  },
});

const loading = ref(false);
const followingList = ref([]);
const followedList = ref([]);

// 獲取關注的人列表
const fetchFollowers = async (userId) => {
  try {
    loading.value = true;
    const res = await FollowService.getFollowers(userId);
    followingList.value = res || [];
  } catch (error) {
    ElMessage.error(`獲取關注列表失敗: ${error.message}`);
    followingList.value = [];
  } finally {
    loading.value = false;
  }
};

// 獲取關注我的列表
const fetchFollowed = async (userId) => {
  try {
    loading.value = true;
    const res = await FollowService.getFollowed(userId);
    followedList.value = res || [];
  } catch (error) {
    ElMessage.error(`獲取粉絲列表失敗: ${error.message}`);
    followedList.value = [];
  } finally {
    loading.value = false;
  }
};

// 處理取消關注
const handleUnfollow = async (id) => {
  try {
    const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
    await FollowService.unfollow({ followedId: id }, userId);
    ElMessage.success(`已取消關注用戶 ID: ${id}`);
    followingList.value = followingList.value.filter((user) => user.id !== id);
  } catch (error) {
    ElMessage.error(`取消關注失敗: ${error.message}`);
  }
};

// 處理關注
const handleFollow = async (id) => {
  try {
    const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
    await FollowService.follow({ followedId: id }, userId);
    ElMessage.success(`已關注用戶 ID: ${id}`);
    followedList.value = followedList.value.map((user) =>
      user.id === id ? { ...user, isFollowing: true } : user
    );
  } catch (error) {
    ElMessage.error(`關注失敗: ${error.message}`);
  }
};

// 處理發送信息
const handleSendMessage = (id) => {
  ElMessage.info(`正在向用戶 ID: ${id} 發送信息`);
};

// 處理頭像點擊，跳轉到用戶詳情頁面
const handleUserClick = (id) => {
  router.push(`/userDetail/${id}`);
};

// 監聽 activeTab 變化
watch(
  () => props.activeTab,
  (newTab) => {
    const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
    if (!userId) {
      ElMessage.error('請先登錄');
      return;
    }
    if (newTab === 'follower') {
      fetchFollowers(userId);
    } else if (newTab === 'followed') {
      fetchFollowed(userId);
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.follow-detail {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
}

h3 {
  font-size: 20px;
  color: #409eff;
  margin-bottom: 20px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.empty {
  text-align: center;
  color: #909399;
  font-size: 16px;
  margin-top: 20px;
}

.button-group {
  display: flex;
  gap: 10px; /* 按鈕之間的間距 */
  align-items: center;
}

@media (max-width: 768px) {
  .follow-detail {
    padding: 10px;
  }

  h3 {
    font-size: 18px;
  }

  .avatar {
    width: 32px;
    height: 32px;
  }

  .button-group {
    gap: 8px; 
  }

  .button-group .el-button {
    padding: 8px;
    font-size: 12px; 
  }
}
</style>