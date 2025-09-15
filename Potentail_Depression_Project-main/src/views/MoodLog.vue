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
        <MoodCalendar :mood-data-store="moodDataStore" :marked-dates="markedDates" @date-change="handleDateChange" />
      </div>
    </div>
    <el-button type="primary" @click="goBack">返回首页</el-button>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CardPopup from '/src/components/CardPopup.vue'
import MoodCalendar from '/src/components/DateComponent/MoodCalendar.vue'
import MoodApiService from '/src/api/moodApi.js'

const router = useRouter()
const selectedDate = ref(new Date().toISOString().split('T')[0]) // 当前选中的日期
const moodData = ref(null) // 当前日期的情绪数据
const moodDataStore = ref({}) // 存储日期和对应的 CardResp 数据
const markedDates = ref([]) // 存储有记录的日期，用于绿色标记

// 获取情绪历史数据
const fetchData = async () => {
  try {
    const info = await MoodApiService.getMoodHistoryInfo()
    // 假设 info 是提供的 JSON 数据
    moodDataStore.value = info.reduce((acc, item) => {
      acc[item.date] = {
        id: item.id,
        quoteText: item.quoteText,
        author: item.author,
        bookTitle: item.bookTitle,
        tags: item.tags
      }
      return acc
    }, {})
    // 提取有记录的日期用于标记
    markedDates.value = info.map(item => item.date)
    // 初始化选中日期的数据
    moodData.value = moodDataStore.value[selectedDate.value] || null
  } catch (error) {
    console.error('API failed:', error)
    ElMessage.error('获取日志数据失败，请稍后再试')
  }
}

onMounted(() => {
  fetchData()
})

// 处理日历日期变化
const handleDateChange = (formattedDate) => {
  selectedDate.value = formattedDate
  moodData.value = moodDataStore.value[formattedDate] || null
}

const goBack = () => {
  router.push('/main')
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