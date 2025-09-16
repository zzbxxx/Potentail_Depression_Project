<template>
  <div>
    <h3>邮箱管理</h3>
    <el-row justify="end" style="margin-bottom: 20px;">
      <el-button type="primary" @click="sendEmail" :disabled="!hasSendPermission">
        发送新邮件
      </el-button>
      <el-tooltip v-if="!hasSendPermission" content="需要权限才能发送邮件" placement="top">
        <el-icon style="margin-left: 10px; color: #909399;"><InfoFilled /></el-icon>
      </el-tooltip>
    </el-row>
    <el-table :data="emailData" style="width: 100%">
      <el-table-column prop="subject" label="主题"></el-table-column>
      <el-table-column prop="sender" label="发件人"></el-table-column>
      <el-table-column label="操作">
        <template #default>
          <el-button type="text">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';

// 示例数据
const emailData = ref([
  { subject: '欢迎邮件', sender: 'system@example.com' },
  // ...更多数据
]);

// 权限检查（示例，实际从 store 或 API 获取）
const hasSendPermission = ref(false); // 默认无权限

// 发送邮件方法
const sendEmail = () => {
  if (hasSendPermission.value) {
    // 发送逻辑
    ElMessage.success('邮件发送成功');
  }
};
</script>

<style scoped>
/* 继承柔和风格 */
h3 {
  color: #409EFF;
}
</style>