<template>
  <div class="MoodCalendar">
    <el-calendar :value="currentDate" @input="handleDateSelect">
      <template #date-cell="{ data }">
        <div :class="{ 'has-data': markedDates.includes(data.day) }" @click="emitDateChange(data.day)">
          {{ data.day.split('-').slice(-1)[0] }}
        </div>
      </template>
    </el-calendar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElCalendar } from 'element-plus'

defineProps({
  moodDataStore: {
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

const handleDateSelect = (date) => {
  const formattedDate = date.toISOString().split('T')[0]
  emit('date-change', formattedDate)
}

const emitDateChange = (day) => {
  emit('date-change', day)
}
</script>

<style scoped>
.MoodCalendar {
  background-color: #ffffff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 绿色标记样式 */
:deep(.el-calendar-day) {
  position: relative;
  cursor: pointer;
}

.has-data {
  position: relative;
  display: inline-block;
  width: 100%;
  height: 100%;
  text-align: center;
}

.has-data::after {
  content: '';
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 6px;
  height: 6px;
  background-color: #67c23a; /* 绿色标记 */
  border-radius: 50%;
}
</style>