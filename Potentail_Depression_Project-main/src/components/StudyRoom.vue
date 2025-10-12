<template>
  <div class="overlay" v-if="modelValue" @click.self="close">
    <div class="study-room">
      <div class="header">
        <h2>自習室</h2>
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索自習室..."
            clearable
            @input="handleRoomSearch"
            style="width: 10rem;"
          />
          <el-select
            v-model="selectedType"
            placeholder="選擇類型"
            clearable
            style="width: 10rem;"
            @change="handleTypeFilter"
          >
            <el-option
              v-for="type in typeOptions"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
          <el-button type="primary" @click="createRoom">創建自習室</el-button>
          <el-button type="danger" size="small" @click="close">關閉</el-button>
        </div>
      </div>

      <el-scrollbar height="calc(100% - 50px)">
        <div class="room-list">
          <el-card
            v-for="room in filteredRooms"
            :key="room.id"
            shadow="hover"
            class="room-card"
            @click="openDialog(room)"
          >
            <div class="room-info">
              <div class="room-header">
                <img 
                  :src="room.avatar" 
                  class="room-avatar" 
                  alt="創建者頭像" 
                  @error="handleImageError($event, room.id)"
                />
                <div>
                  <h3>{{ room.name }}</h3>
                  <p>房間ID: {{ room.id }}</p>
                </div>
              </div>
              <p>人數: {{ room.currentUsers }}/{{ room.maxUsers }}</p>
              <p>創建者: 匿名用戶{{ room.creatorId }}</p>
              <p>狀態: {{ room.status }}</p>
              <p>類型: {{ room.type }}</p>
              <p>標簽: {{ room.tags.length ? room.tags.join(', ') : '無' }}</p>
            </div>
            <div class="room-footer">
              <span class="time">{{ formatTime(room.createdAt) }}</span>
            </div>
          </el-card>
        </div>
      </el-scrollbar>
    </div>

    <!-- 原生對話框 -->
    <div class="custom-dialog-overlay" v-if="showDialog" @click.self="closeDialog">
      <div class="custom-dialog">
        <div class="custom-dialog-header">加入自習室</div>
        <div class="custom-dialog-content">
          確定要加入自習室 "{{ selectedRoomForJoin?.name }}" 嗎？
        </div>
        <div class="custom-dialog-footer">
          <button class="confirm-btn" @click="confirmJoin">確定</button>
          <button class="cancel-btn" @click="closeDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  rooms: Array
})

const emit = defineEmits(['update:modelValue', 'select-room', 'create-room'])

const searchQuery = ref('')
const selectedType = ref('')
const selectedTag = ref('')
const typeOptions = ref(['嚴格番茄鐘', '自由計時', '互助房', '編程學習'])
const tagOptions = ref(['數學', '繪畫', '哲學', '文學', '歷史', '科學'])
const showDialog = ref(false)
const selectedRoomForJoin = ref(null)

const filteredRooms = computed(() => {
  let result = props.rooms
  if (searchQuery.value) {
    result = result.filter(room => 
      room.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  if (selectedType.value) {
    result = result.filter(room => room.type === selectedType.value)
  }
  if (selectedTag.value) {
    result = result.filter(room => room.tags.includes(selectedTag.value))
  }
  return result
})

function openDialog(room) {
  selectedRoomForJoin.value = room
  showDialog.value = true
}

function confirmJoin() {
  if (selectedRoomForJoin.value) {
    emit('select-room', selectedRoomForJoin.value)
  }
  closeDialog()
}

function closeDialog() {
  showDialog.value = false
  selectedRoomForJoin.value = null
}

function createRoom() {
  emit('create-room')
}

function close() {
  emit('update:modelValue', false)
}

function handleRoomSearch() {
  // 搜索邏輯已移到 computed 中
}

function handleTypeFilter() {
  // 過濾邏輯已移到 computed 中
}

function handleTagFilter() {
  // 標簽過濾邏輯已移到 computed 中
}

const formatTime = (t) => {
  return new Date(t).toLocaleString()
}

function handleImageError(event, roomId) {
  event.target.src = new URL('../assets/images/default-avatar.png', import.meta.url).href
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5); /* 保留原背景色 */
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 2000;
}

.study-room {
  background-color: #f8f1e9; /* 溫暖米色背景 */
  width: 60%;
  padding-left: 40px;
  padding-right: 40px;
  max-width: 1000px;
  height: 90vh;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 柔和陰影 */
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #e6d8c6; /* 暖色邊框 */
}

.header h2 {
  font-size: 1.5rem;
  color: #4a2f1a; /* 深棕色文字 */
  font-weight: 500;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.room-list {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  max-width: 100%;
}

.room-card {
  border-radius: 8px;
  background-color: #fffaf4; /* 淺暖色卡片背景 */
  border: 1px solid #e6d8c6; /* 暖色邊框 */
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* 柔和懸浮效果 */
}

.room-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.room-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #d9c8b0; /* 暖色備用背景 */
  border: 1px solid #e6d8c6;
}

.room-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #4a2f1a; /* 深棕色標題 */
}

.room-info p {
  margin: 5px 0;
  color: #6b4e31; /* 柔和棕色文字 */
  font-size: 0.9rem;
}

.room-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: #8a6d4a; /* 淺棕色時間文字 */
}

.time {
  color: #8a6d4a;
}

/* 原生對話框樣式 */
.custom-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5); /* 保留原背景色 */
  display: grid;
  place-items: center;
  z-index: 99999; /* 確保層級最高 */
}

.custom-dialog {
  background-color: #fffaf4; /* 淺暖色背景 */
  border-radius: 8px;
  width: 320px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.custom-dialog-header {
  padding: 12px 16px;
  background-color: #e6d8c6; /* 暖色頭部背景 */
  color: #4a2f1a; /* 深棕色文字 */
  font-size: 1.1rem;
  font-weight: 500;
  text-align: center;
}

.custom-dialog-content {
  padding: 16px;
  color: #6b4e31; /* 柔和棕色文字 */
  font-size: 0.9rem;
  text-align: center;
}

.custom-dialog-footer {
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e6d8c6; /* 暖色邊框 */
}

.confirm-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.confirm-btn {
  background-color: #a67c52; /* 溫暖棕色按鈕 */
  color: #fff;
}

.confirm-btn:hover {
  background-color: #b89168; /* 懸浮時稍亮 */
}

.cancel-btn {
  background-color: #e6d8c6; /* 淺暖色取消按鈕 */
  color: #4a2f1a;
}

.cancel-btn:hover {
  background-color: #d9c8b0; /* 懸浮時稍深 */
}

@media (max-width: 768px) {
  .study-room {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }

  .room-list {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .custom-dialog {
    width: 90%;
  }
}
</style>