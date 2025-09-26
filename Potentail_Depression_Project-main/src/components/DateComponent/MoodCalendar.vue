<template>
  <div class="MoodCalendar">
    <!-- 移动端日期选择器 -->
    <div class="mobile-date-picker" v-if="isMobile">
      <el-date-picker
        v-model="selectedDate"
        type="date"
        placeholder="选择日期"
        :clearable="false"
        @change="handleMobileDateChange"
        size="small"
      />
      <div class="mobile-marked-dates" v-if="markedDates.length > 0">
        <span class="marker-dot"></span>
        <span class="marker-text">有记录的日期</span>
      </div>
    </div>

    <!-- 桌面端日历 -->
    <el-calendar v-else :value="currentDate" @input="handleDateSelect">
      <template #date-cell="{ data }">
        <div 
          :class="{ 
            'has-data': markedDates.includes(data.day),
            'calendar-day': true
          }" 
          @click="emitDateChange(data.day)"
        >
          {{ data.day.split('-').slice(-1)[0] }}
        </div>
      </template>
    </el-calendar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElCalendar, ElDatePicker } from 'element-plus'

const props = defineProps({
  moodDataStore: {
    type: Object,
    default: () => ({})
  },
  moodHistoryStore: { 
    type: Object,
    default: () => ({})
  },
  markedDates: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['date-change'])
const currentDate = ref(new Date())
const selectedDate = ref(new Date())
const screenWidth = ref(window.innerWidth)

const isMobile = computed(() => screenWidth.value <= 768)

const handleResize = () => {
  screenWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const handleDateSelect = (date) => {
  const formattedDate = date.toISOString().split('T')[0]
  console.log(props.moodDataStore);
  
  emit('date-change', formattedDate)
}

const handleMobileDateChange = (date) => {
  if (date) {
    const formattedDate = new Date(date).toISOString().split('T')[0]
    emit('date-change', formattedDate)
    // 打印引言数据和情感数据
    console.log(`Card Data for ${formattedDate}:`, props.moodDataStore[formattedDate] || 'No card data')
    console.log(`Mood Data for ${formattedDate}:`, props.moodHistoryStore[formattedDate] || 'No mood data')
  }
}

const emitDateChange = (day) => {

  const cardId = props.moodDataStore[day]?.id || 'No ID found';
  
  emit('date-change', day , cardId)
}
</script>

<style scoped>
/* 样式保持不变 */
.MoodCalendar {
  background-color: #ffffff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 300px;
}

:deep(.el-calendar-day) {
  position: relative;
  cursor: pointer;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-day {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.has-data {
  position: relative;
}

.has-data::after {
  content: '';
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 6px;
  height: 6px;
  background-color: #67c23a;
  border-radius: 50%;
}

.mobile-date-picker {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  padding: 1rem 0;
}

.mobile-marked-dates {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #606266;
}

.marker-dot {
  width: 8px;
  height: 8px;
  background-color: #67c23a;
  border-radius: 50%;
  display: inline-block;
}

.marker-text {
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .MoodCalendar {
    padding: 0.5rem;
    min-height: auto;
  }
  
  :deep(.el-calendar-table .el-calendar-day) {
    height: 32px;
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .MoodCalendar {
    padding: 0.25rem;
  }
  
  .mobile-date-picker {
    padding: 0.5rem 0;
  }
  
  :deep(.el-calendar-table .el-calendar-day) {
    height: 28px;
    font-size: 0.75rem;
    padding: 2px;
  }
}

:deep(.el-calendar) {
  max-width: 100%;
  overflow: hidden;
}

:deep(.el-calendar-table) {
  table-layout: fixed;
  width: 100%;
}

:deep(.el-calendar-table td) {
  padding: 2px;
}

:deep(.el-calendar-table .cell) {
  padding: 4px;
}
</style>