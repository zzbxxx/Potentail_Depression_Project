<template>
  <div class="header">
    <h1>{{ room?.name || '自習室' }}</h1>
    <div class="room-details-compact">
      <div class="detail-item"><span class="label">ID:</span> {{ room?.id || '未知' }}</div>
      <div class="detail-item"><span class="label">類型:</span> {{ room?.type || '未知' }}</div>
      <div class="detail-item"><span class="label">人數:</span> {{ room?.currentUsers || 0 }}/{{ room?.maxUsers || '∞' }}</div>
      <div class="detail-item"><span class="label">創建者:</span> {{ room?.creatorNickname || `匿名用戶${room?.creatorId || '未知'}` }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  room: {
    type: Object,
    default: null
  }
})
</script>

<style scoped>
.header {
  background-color: #fefcf9;
  width: 100%;
  max-width: 1000px;
  border-radius: 12px;
  padding: 20px 25px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
}
.header h1 { 
  color: #3f3b3a; 
  font-size: 1.8rem; 
  margin-bottom: 10px; 
}
.room-details-compact {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  font-size: 0.9rem;
  color: #555;
}
</style>