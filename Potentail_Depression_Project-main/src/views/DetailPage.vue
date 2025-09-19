<!-- DetailPage.vue -->
<template>
  <div>
    <h1>详细页面</h1>
    <p>这是详细页面内容</p>
    <el-button @click="goBack">返回</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/main')
}
</script>