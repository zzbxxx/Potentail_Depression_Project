<template>
  <div class="author-info">
    <el-avatar :size="40" 
    :src="author.avatar || defaultAvatar"
    @click="handleUserClick(author.authorId)"
    />
    <div class="author-details">
      <span class="nickname">{{ author.nickname || '匿名' }}</span>
      <span class="created-at">{{ formatDate(author.createdAt) }}</span>
    </div>
    <el-button
      class="follow-btn"
      type="text"
      v-if="userId && userId != author.authorId"
      @click="debouncedToggleFollow"
    >
      <span v-if="!Following">关注</span>
      <span v-else>已关注</span>
    </el-button>
  </div>
</template>

<script setup>
import { ElAvatar, ElMessage } from 'element-plus';
import { onMounted, ref } from 'vue';
import FollowService from '/src/api/followApi'
import { ca } from 'element-plus/es/locales.mjs';
import { debounce } from 'lodash';
import { useRouter } from 'vue-router';
const router = useRouter();
const props = defineProps({
  author: {
    type: Object,
    required: true,
    default: () => ({
      nickname: null,
      avatar: null,
      createdAt: null,
      authorId: null
    })
  }
});
const Following = ref(false);

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d395c9d8b2e3e3f1f2c8e7b2f7.png';
const userId = ref(localStorage.getItem('userId') || localStorage.getItem('user_id'));

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const handleUserClick = (authorId) => {
  router.push(`/userDetail/${authorId}`);
}

const checkFollow = async () => {
  if (!userId.value) return;
  const result = await FollowService.checkFollow(userId.value, props.author.authorId);
  
  if (result.following) {
    Following.value = true;
  }
};

const toggleFollow = async () => {
  try {
    console.log(Following.value);
    
    if(Following.value){
      const response =await FollowService.unfollow(userId.value, props.author.authorId);
      if (response.code === 0) {
        Following.value = false;
        ElMessage.success('取消关注成功');
      }
    }else{
      const response = await FollowService.follow(userId.value, props.author.authorId);
      if (response.code === 0){
        Following.value = true;
        ElMessage.success('关注成功');
      }
    }
  }catch (error) {
    console.log(error);
  }
}
const debouncedToggleFollow = debounce(toggleFollow, 500);

onMounted(() => {
  checkFollow();
});
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
.follow-btn {
  margin-left: auto;
  margin-right: 2rem;
}
</style>
