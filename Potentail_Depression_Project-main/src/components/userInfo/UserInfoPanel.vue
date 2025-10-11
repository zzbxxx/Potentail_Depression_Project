<template>
  <el-card class="user-info">
    <div class="user-info-content">
      <el-avatar :size="100" :src="user.avatar" alt="用户头像" />
      <div class="info-details">
        <el-row>
            <el-col :span="22">
                <h2>{{ user.nickname }}</h2>
            </el-col>
            <el-col :span="2">
                <el-button type="text" size="mini" @click="goBack">返回</el-button>
            </el-col>
        </el-row>
        <el-text class="bio">{{ user.bio }}</el-text>
        <div class="meta">
          <el-text>电子邮件: {{ user.email }}</el-text>
          <el-text>IP: {{ user.lastIpLocation }}</el-text>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps } from 'vue';
import { useRouter } from 'vue-router';
// 定义 props
const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};
const router = useRouter();
const goBack = () => {
    router.go(-1);
}

</script>

<style scoped>
.user-info {
  margin-bottom: 20px;
}

.user-info-content {
  display: flex;
  align-items: center;
}

.info-details {
  margin-left: 20px;
  flex: 1;
}

h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.username {
  color: #666;
  font-size: 16px;
  margin: 5px 0;
}

.bio {
  color: #555;
  font-size: 14px;
  margin: 10px 0;
  display: block;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: #888;
  font-size: 14px;
}
</style>