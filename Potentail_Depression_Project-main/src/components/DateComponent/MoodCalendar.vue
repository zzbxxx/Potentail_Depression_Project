<template>
  <div class="MoodCalendar">
    <el-calendar v-model="selectedDate" @change="handleDateChange">
      <template #date-cell="{ data }">
        <div :class="{ 'has-data': hasMoodData(data.day) }">
          {{ data.day.split('-')[2] }}
        </div>
      </template>
    </el-calendar>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 正确定义 props
const props = defineProps({
  moodDataStore: {
    type: Object,
    default: () => ({})
  }
})
const emit = defineEmits(['date-change'])
const selectedDate = ref(new Date())

// 检查某天是否有数据
const hasMoodData = (day) => {
  return !!props.moodDataStore[day]
}

// 日期变化时触发事件
const handleDateChange = (date) => {
  const formattedDate = date.toISOString().split('T')[0]
  emit('date-change', formattedDate)
}
</script>

<style scoped>
.MoodCalendar {
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-calendar-day) {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.has-data) {
  background-color: #e6f4ea;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .MoodCalendar {
    padding: 1rem;
  }
}
</style>
