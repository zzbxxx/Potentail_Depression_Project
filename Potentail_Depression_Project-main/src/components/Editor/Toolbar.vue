<template>
  <div class="toolbar mb-4">
     <el-button class="toolbar-btn" @click="goToHome">首頁</el-button>
    <el-button class="toolbar-btn" @click="$emit('add-text')">添文段</el-button>
    <el-button class="toolbar-btn" @click="$emit('add-image')">添圖片</el-button>
    <el-button class="toolbar-btn" @click="$emit('save')">保存</el-button>
  </div>
</template>

<script setup>
import { ElButton } from 'element-plus';
import { useRouter } from 'vue-router';
const router = useRouter();
function goToHome() {
  router.push('/main');
}
</script>

<style>
.toolbar {
  display: flex;
  gap: 10px;
  margin-top: 2%;
}
</style>