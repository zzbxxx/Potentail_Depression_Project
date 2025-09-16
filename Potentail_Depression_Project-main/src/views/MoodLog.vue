<template>
  <div class="MoodLog">
    <h1>时间日志</h1>
    <div class="content">
      <div class="left-panel">
        <CardLogCard v-if="currentView === 'card'" :date="selectedDate" :data="moodData" />
        <MoodLogCard v-if="currentView === 'mood' && moodHistoryData" :date="selectedDate" :mood-data="moodHistoryData" />
        <p v-if="shouldShowNoData" class="no-data">{{ noDataMessage }}</p>
        <div class="button-group">
          <el-button type="primary" @click="handleCardClick">卡片</el-button>
          <el-button type="success" @click="handleMessageClick">留言</el-button>
          <el-button type="info" @click="handleOtherClick" disabled>其他</el-button>
        </div>
      </div>
      <div class="right-panel">
        <MoodCalendar 
          :mood-data-store="moodDataStore" 
          :mood-history-store="moodHistoryStore"
          :marked-dates="markedDates" 
          @date-change="handleDateChange" 
        />
      </div>
    </div>
    <el-button type="primary" @click="goBack">返回首页</el-button>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CardLogCard from '/src/components/DateComponent/CardLogCard.vue'
import MoodLogCard from '/src/components/DateComponent/MoodLogCard.vue'
import MoodCalendar from '/src/components/DateComponent/MoodCalendar.vue'
import MoodApiService from '/src/api/moodApi.js'

const router = useRouter()
const selectedDate = ref(new Date().toISOString().split('T')[0])
const moodData = ref(null)
const moodDataStore = ref({})
const moodHistoryStore = ref({})
const markedDates = ref([])
const currentView = ref('card') 
const moodHistoryData = ref(null)

const fetchCardData = async () => {
  try {
    const info = await MoodApiService.getCardHistoryInfo()
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
    markedDates.value = info.map(item => item.date)
    moodData.value = moodDataStore.value[selectedDate.value] || null
  } catch (error) {
    console.error('API failed:', error)
    ElMessage.error('获取卡片日志数据失败，请稍后再试')
  }
}

const fetchMoodData = async () => {
  try {
    const data = await MoodApiService.getMoodHistoryInfo()
    moodHistoryStore.value = data.reduce((acc, item) => {
      acc[item.date] = {
        text: item.text,
        primaryMood: item.primaryMood,
        events: item.events
      }
      return acc
    }, {})
    // 初始加载时设置当前日期的数据
    moodHistoryData.value = moodHistoryStore.value[selectedDate.value] || null
    console.log('moodHistoryStore:', moodHistoryStore.value) // 调试
  } catch (error) {
    console.error('API failed (mood):', error)
    ElMessage.error('获取情绪日志数据失败，请稍后再试')
  }
}

onMounted(() => {
  fetchCardData()
  fetchMoodData()
})

watch([selectedDate, moodHistoryStore], ([newDate], [oldDate]) => {
  moodHistoryData.value = moodHistoryStore.value[newDate] || null
  console.log('Updated moodHistoryData:', moodHistoryData.value) // 调试
})

const handleDateChange = (formattedDate) => {
  selectedDate.value = formattedDate
  moodData.value = moodDataStore.value[formattedDate] || null
  moodHistoryData.value = moodHistoryStore.value[formattedDate] || null
}

const goBack = () => {
  router.push('/main')
}

const handleCardClick = () => {
  currentView.value = 'card' // 切换到卡片视图
}

const handleMessageClick = () => {
  currentView.value = 'mood' // 切换到心情视图
}

const handleOtherClick = () => {
  console.log('点击了其他按钮')
}

// 计算属性：判断是否显示无数据提示
const shouldShowNoData = computed(() => {
  if (currentView.value === 'card' && !moodData.value) {
    return true
  } else if (currentView.value === 'mood' && !moodHistoryData.value) {
    return true
  }
  return false
})

// 计算属性：动态生成无数据提示信息
const noDataMessage = computed(() => {
  if (currentView.value === 'card' && !moodData.value) {
    return '这一天没有卡片数据喔'
  } else if (currentView.value === 'mood' && !moodHistoryData.value) {
    return '这一天没有心情数据喔'
  }
  return ''
})
</script>

<style scoped>
/* 样式保持不变 */
.MoodLog {
  padding: 1rem;
  text-align: center;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

.left-panel {
  background-color: #ffffff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  order: 2;
}

.right-panel {
  order: 1;
}

.no-data {
  color: #909399;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.button-group {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  margin-top: auto;
  flex-wrap: wrap;
}

.button-group .el-button {
  padding: 8px 12px;
  font-size: 0.9rem;
  min-width: 80px;
}

h1 {
  color: #303133;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  font-family: 'PingFang SC', sans-serif;
}

.el-button {
  margin-top: 1rem;
  background-color: #67c23a;
  border-color: #67c23a;
  font-size: 0.9rem;
}

@media (min-width: 769px) {
  .content {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .left-panel {
    flex: 1;
    min-height: 400px;
    order: 1;
  }
  
  .right-panel {
    flex: 1;
    order: 2;
  }
}

@media (max-width: 480px) {
  .MoodLog {
    padding: 0.5rem;
  }
  
  .left-panel {
    padding: 0.75rem;
  }
  
  .button-group {
    gap: 0.3rem;
  }
  
  .button-group .el-button {
    padding: 6px 10px;
    font-size: 0.8rem;
    min-width: 70px;
  }
  
  h1 {
    font-size: 1.2rem;
  }
  
  .no-data {
    font-size: 0.8rem;
  }
}
</style>