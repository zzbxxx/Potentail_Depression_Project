<template>
  <div class="study-room-container">
    <div class="header">
      <h1>{{ room?.name || '自習室' }}</h1>
      <div class="button-group">
        <el-button type="primary" @click="backToHome">返回首頁</el-button>
        <el-button type="danger" @click="confirmExitRoom">退出自習室</el-button>
      </div>
    </div>
    <el-scrollbar height="calc(100vh - 120px)">
      <div class="room-content">
        <el-card class="room-details" shadow="always">
          <h2>自習室詳情</h2>
          <p><strong>房間ID:</strong> {{ room?.id || '未知' }}</p>
          <p><strong>類型:</strong> {{ room?.type || '未知' }}</p>
          <p><strong>人數:</strong> {{ room?.currentUsers || 0 }}/{{ room?.maxUsers || '無限制' }}</p>
          <p><strong>創建者:</strong> {{ room?.creatorNickname || `匿名用戶${room?.creatorId || '未知'}` }}</p>
          <p><strong>狀態:</strong> {{ room?.status || '未知' }}</p>
          <p><strong>標簽:</strong> {{ room?.tags?.length ? room.tags.join(', ') : '無' }}</p>
          <p><strong>創建時間:</strong> {{ formatTime(room?.createdAt) || '未知' }}</p>
        </el-card>
      </div>
    </el-scrollbar>

    <!-- 確認退出對話框 -->
    <div class="custom-dialog-overlay" v-if="showExitDialog" @click.self="closeExitDialog">
      <div class="custom-dialog">
        <div class="custom-dialog-header">退出自習室</div>
        <div class="custom-dialog-content">
          確定要退出自習室 "{{ room?.name || '當前自習室' }}" 嗎？
        </div>
        <div class="custom-dialog-footer">
          <button class="confirm-btn" @click="leaveRoomAndGoHome">確定</button>
          <button class="cancel-btn" @click="closeExitDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RoomService from '/src/api/roomApi.js'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

const route = useRoute()
const router = useRouter()
const room = ref(null)
const showBackDialog = ref(false)
const showExitDialog = ref(false)
const userId = ref(localStorage.getItem('userId') || localStorage.getItem('user_id')) 
let stompClient = null

const typeMap = {
  'strict_tomato': '嚴格番茄鐘',
  'free_time': '自由計時',
  'mutual_aid': '互助房',
  'programming': '編程學習'
}

const topicMap = {
  'Math': '數學',
  'Physics': '科學',
  'Painting': '繪畫',
  'Philosophy': '哲學',
  'Literature': '文學',
  'History': '歷史'
}

async function fetchRoomDetails() {
  try {
    const roomId = route.params.id
    if (!roomId || isNaN(roomId)) {
      throw new Error('無效的房間 ID')
    }
    const res = await RoomService.getRoomById(roomId)
    if (!res.success || !res.data) {
      throw new Error(res.message || '無效的房間數據')
    }
    room.value = {
      ...res.data,
      type: typeMap[res.data.type] || res.data.type,
      tags: res.data.topics?.map(topic => topicMap[topic] || topic) || [],
      avatar: handleAvatar(res.data.avatar || res.data.creatorAvatar)
    }
  } catch (e) {
    console.error('獲取自習室詳情失敗:', e)
    ElMessage.error('獲取自習室詳情失敗')
    router.push({ name: 'MainPage' })
  }
}

async function joinRoom() {
  try {
    const roomId = route.params.id
    const res = await RoomService.joinRoom(roomId, userId.value)
    if (!res.success) {
      throw new Error(res.message || '加入自習室失敗')
    }
    room.value = {
      ...res.data,
      type: typeMap[res.data.type] || res.data.type,
      tags: res.data.topics?.map(topic => topicMap[topic] || topic) || [],
      avatar: handleAvatar(res.data.avatar || res.data.creatorAvatar)
    }
  } catch (e) {
    console.error('加入自習室失敗:', e)
    ElMessage.error('加入自習室失敗: ' + e.message)
    router.push({ name: 'MainPage' })
  }
}

async function leaveRoom() {
  try {
    const roomId = route.params.id
    if (!userId.value) {
      throw new Error('用戶未登錄')
    }
    const res = await RoomService.leaveRoom(roomId, userId.value)
    if (!res.success) {
      throw new Error(res.message || '退出自習室失敗')
    }
  } catch (e) {
    console.error('退出自習室失敗:', e)
    ElMessage.error('退出自習室失敗: ' + e.message)
  }
}

function handleAvatar(avatar) {
  if (avatar && typeof avatar === 'string' && avatar.trim() !== '') {
    return new URL(`/src/assets/image/${avatar}.jpg`, import.meta.url).href
  }
  return new URL('/src/assets/image/default-avatar.png', import.meta.url).href
}

function formatTime(t) {
  return t ? new Date(t).toLocaleString() : '未知'
}

function backToHome() {
  router.push({ name: 'MainPage' })
  closeBackDialog()
  closeExitDialog()
}

function confirmExitRoom() {
  showExitDialog.value = true
}

function closeBackDialog() {
  showBackDialog.value = false
}

function closeExitDialog() {
  showExitDialog.value = false
}

function goToHome() {
  router.push({ name: 'MainPage' })
  closeBackDialog()
  closeExitDialog()
}

async function leaveRoomAndGoHome() {
  await leaveRoom()
  router.push({ name: 'MainPage' })
  closeBackDialog()
  closeExitDialog()
}

function connectWebSocket() {
  const socket = new SockJS('http://localhost:8080/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/room/${route.params.id}`, (message) => {
      const body = message.body
      if (body === 'closed') {
        router.push({ name: 'MainPage' })
      } else {
        room.value.currentUsers = parseInt(body)
      }
    })
  }, (error) => {
    console.error('WebSocket 連接失敗:', error)
    ElMessage.error('無法連接到實時更新服務')
  })
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
}

onMounted(() => {
  fetchRoomDetails()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.study-room-container {
  background-color: #f8f1e9;
  min-height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 1000px;
  padding: 10px 20px;
  background-color: #fffaf4;
  border-bottom: 1px solid #e6d8c6;
  border-radius: 8px;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 1.8rem;
  color: #4a2f1a;
  font-weight: 500;
  margin: 0;
}

.button-group {
  display: flex;
  gap: 10px;
}

.room-content {
  width: 100%;
  max-width: 1000px;
  padding: 20px;
}

.room-details {
  background-color: #fffaf4;
  border: 1px solid #e6d8c6;
  border-radius: 8px;
  padding: 20px;
}

.room-details h2 {
  font-size: 1.4rem;
  color: #4a2f1a;
  margin-bottom: 15px;
}

.room-details p {
  font-size: 1rem;
  color: #6b4e31;
  margin: 8px 0;
}

.custom-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  z-index: 99999;
}

.custom-dialog {
  background-color: #fffaf4;
  border-radius: 8px;
  width: 320px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.custom-dialog-header {
  padding: 12px 16px;
  background-color: #e6d8c6;
  color: #4a2f1a;
  font-size: 1.1rem;
  font-weight: 500;
  text-align: center;
}

.custom-dialog-content {
  padding: 16px;
  color: #6b4e31;
  font-size: 0.9rem;
  text-align: center;
}

.custom-dialog-footer {
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e6d8c6;
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
  background-color: #a67c52;
  color: #fff;
}

.confirm-btn:hover {
  background-color: #b89168;
}

.cancel-btn {
  background-color: #e6d8c6;
  color: #4a2f1a;
}

.cancel-btn:hover {
  background-color: #d9c8b0;
}

@media (max-width: 768px) {
  .study-room-container {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    gap: 10px;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .room-content {
    padding: 10px;
  }

  .custom-dialog {
    width: 90%;
  }
}
</style>