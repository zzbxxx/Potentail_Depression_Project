<!-- src/views/MoodLog.vue -->
<template>
  <div class="MoodLog">
    <h1>时间日志</h1>
    <p>这里是时间日志页面</p>
    <el-button type="primary" @click="goBack">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.MoodLog {
  padding: 2rem;
  text-align: center;
}
</style>