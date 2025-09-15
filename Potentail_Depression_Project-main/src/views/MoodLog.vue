<template>
  <div class="MoodLog">
    <h1>时间日志</h1>
    <div class="content">
      <div class="left-panel">
        <CardPopup :date="selectedDate" :data="moodData" />
        <p v-if="!moodData" class="no-data">暂无记录</p>
      </div>
      <!-- 右侧：MoodCalendar 组件 -->
      <div class="right-panel">
        <MoodCalendar :mood-data-store="moodDataStore" @date-change="handleDateChange" />
      </div>
    </div>
    <el-button type="primary" @click="goBack">返回首页</el-button>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import CardFront from '/src/components/CardPopup.vue'
import MoodCalendar from '/src/components/DateComponent/MoodCalendar.vue' 
import MoodApiService from '/src/api/moodApi.js'
const router = useRouter()
const selectedDate = ref(new Date().toISOString().split('T')[0]) // 当前选中的日期
const moodData = ref(null) // 当前日期的情绪数据

const fetchData = async () => {
    try {
    const info = await MoodApiService.getMoodHistoryInfo()
  } catch (error) {
    console.error('API failed:', error)
    ElMessage.error('获取日志数据失败，请稍后再试')
  }
}
fetchData();

// 模拟情绪数据
const moodDataStore = ref({
  '2025-09-13': { mood: '平静', note: '今天感觉还不错' },
  '2025-09-12': { mood: '低落', note: '有点疲惫' }
})

// 处理日历日期变化
const handleDateChange = (formattedDate) => {
  selectedDate.value = formattedDate
  moodData.value = moodDataStore.value[formattedDate] || null
}

// 返回首页
const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.MoodLog {
  padding: 2rem;
  text-align: center;
  background-color: #f5f7fa; /* 柔和背景色 */
  min-height: 100vh;
}

.content {
  display: flex;
  justify-content: space-between;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.left-panel {
  flex: 1;
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.right-panel {
  flex: 1;
}

.no-data {
  color: #909399;
  font-size: 1rem;
  margin-top: 1rem;
}

h1 {
  color: #303133;
  font-size: 1.8rem;
  margin-bottom: 1.5rem;
  font-family: 'PingFang SC', sans-serif;
}

.el-button {
  margin-top: 1.5rem;
  background-color: #67c23a;
  border-color: #67c23a;
}

@media (max-width: 768px) {
  .content {
    flex-direction: column;
  }
}
</style>
