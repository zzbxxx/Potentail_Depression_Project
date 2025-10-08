<template>
  <div>
    <h3>建议与反馈</h3>
    <p class="encourage">您的反馈对我们很重要，感谢分享您的想法。</p>
    <el-form label-width="120px">
      <el-form-item label="反馈内容">
        <el-input type="textarea" v-model="feedbackContent" placeholder="请输入您的建议或反馈" :rows="5"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitFeedback">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const feedbackContent = ref('');

const submitFeedback = () => {
  if (feedbackContent.value.trim()) {
    ElMessage.success('反馈提交成功，感谢您的建议！');
    feedbackContent.value = '';
  } else {
    ElMessage.warning('请填写反馈内容');
  }
};
</script>

<style scoped>
.encourage {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}
</style>