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
          <!-- 僅當用戶在房間中顯示「進入房間」按鈕 -->
          <el-button type="warning" v-if="isInRoom" @click="showMyRoom">進入房間</el-button>
          <el-button type="danger" size="small" @click="close">關閉</el-button>
        </div>
      </div>
    
      <p v-if="filteredRooms.length === 0" class="nullTip">還沒有自習室喔！！</p>

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
              <div class="-room-header">
                <img 
                  :src="room.avatar" 
                  class="room-avatar" 
                  alt="房間頭像" 
                  @error="handleImageError($event, room.id)"
                />
                <div>
                  <h3>{{ room.name }}</h3>
                  <p>房間ID: {{ room.id }}</p>
                </div>
              </div>
              <p>人數: {{ room.currentUsers }}/{{ room.maxUsers }}</p>
              <p>創建者: {{ room.creatorNickname || `匿名用戶${room.creatorId}` }}</p>
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
          <button class="confirm-btn" @click="debounceConfirmJoin">確定</button>
          <button class="cancel-btn" @click="closeDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import RoomService from '/src/api/roomApi.js'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { debounce } from 'lodash'

const router = useRouter()
const props = defineProps({
  modelValue: Boolean,
  rooms: Array
})
const subscribedRooms = ref(new Set())
const emit = defineEmits(['update:modelValue', 'select-room', 'create-room'])

const searchQuery = ref('')
const selectedType = ref('')
const selectedTag = ref('')
const typeOptions = ref(['嚴格番茄鐘', '自由計時', '互助房', '編程學習'])
const tagOptions = ref(['數學', '繪畫', '哲學', '文學', '歷史', '科學'])
const userId = localStorage.getItem('userId') || localStorage.getItem('user_id')
const isInRoom = ref(false)
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

const showDialog = ref(false)
const selectedRoomForJoin = ref(null)

const handleAvatar = (avatar) => {
  if (avatar && typeof avatar === 'string' && avatar.trim() !== '') {
    return new URL(`/src/assets/image/${avatar}.jpg`, import.meta.url).href
  }
  return new URL('/src/assets/image/default-avatar.png', import.meta.url).href
}

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

// 檢查用戶是否在房間中
async function checkIfInRoom() {
  if (!userId) {
    isInRoom.value = false
    return
  }
  try {
    const res = await RoomService.getMyRoom(userId)
    isInRoom.value = res.success && !!res.data
  } catch (e) {
    console.error('檢查房間狀態失敗:', e)
    isInRoom.value = false
  }
}

async function fetchRooms() {
  try {
    const res = await RoomService.getActiveRooms()
    if (!res.data || !Array.isArray(res.data)) {
      throw new Error('接口返回數據格式不正確')
    }
    const formattedRooms = res.data.map(room => ({
      ...room,
      type: typeMap[room.type] || room.type,
      tags: room.topics.map(topic => topicMap[topic] || topic),
      avatar: handleAvatar(room.avatar || room.creatorAvatar)
    }))
    props.rooms.splice(0, props.rooms.length, ...formattedRooms)
    await checkIfInRoom()
  } catch (e) {
    console.error('獲取自習室數據失敗:', e)
    ElMessage.error('獲取自習室數據失敗')
  }
}

async function openDialog(room) {
  selectedRoomForJoin.value = room
  try {
    const res = await RoomService.getCurrentRoom(userId)
    if (res.success && res.data && res.data.id === room.id) {
      router.push({ name: 'StudyRoomPage', params: { id: room.id } })
      emit('select-room', room)
    } else if (res.success && res.data) {
      ElMessage.warning(`您已在房間 "${res.data.name}" 中，請先退出再加入其他房間`)
    } else {
      showDialog.value = true
    }
  } catch (e) {
    console.error('檢查當前房間失敗:', e)
    ElMessage.error('檢查房間狀態失敗: ' + e.message)
    showDialog.value = true
  }
}

async function confirmJoin() {
  if (!selectedRoomForJoin.value || !userId) {
    ElMessage.error('用戶未登錄或無效的房間')
    closeDialog()
    return
  }

  try {
    const res = await RoomService.joinRoom(selectedRoomForJoin.value.id, userId)
    if (!res.success) {
      if (res.message.includes('房間已滿')) {
        ElMessage.warning('房間已滿，無法加入！')
      } else if (res.message.includes('用戶已在房間')) {
        const currentRoomRes = await RoomService.getCurrentRoom(userId)
        if (currentRoomRes.success && currentRoomRes.data.id === selectedRoomForJoin.value.id) {
          router.push({ name: 'StudyRoomPage', params: { id: selectedRoomForJoin.value.id } })
          emit('select-room', selectedRoomForJoin.value)
        } else {
          ElMessage.warning(res.message)
        }
      } else {
        ElMessage.error('加入自習室失敗: ' + res.message)
      }
      return
    }
    router.push({ name: 'StudyRoomPage', params: { id: selectedRoomForJoin.value.id } })
    emit('select-room', selectedRoomForJoin.value)
    isInRoom.value = true
  } catch (e) {
    console.error('加入自習室失敗:', e)
    if (e.message.includes('房間已滿')) {
      ElMessage.warning('房間已滿，無法加入！')
    } else {
      ElMessage.error('加入自習室失敗: ' + e.message)
    }
  }
  closeDialog()
}

const debounceConfirmJoin = debounce(confirmJoin, 200)

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
  event.target.src = new URL('/src/assets/images/default-avatar.png', import.meta.url).href
}

async function showMyRoom() {
  if (!userId) {
    ElMessage.error('請先登錄')
    return
  }

  try {
    const res = await RoomService.getMyRoom(userId)
    if (res.success) {
      if (res.data) {
        const formattedRoom = {
          ...res.data,
          type: typeMap[res.data.type] || res.data.type,
          tags: res.data.topics.map(topic => topicMap[topic] || topic),
          avatar: handleAvatar(res.data.avatar || res.data.creatorAvatar)
        }
        router.push({ name: 'StudyRoomPage', params: { id: formattedRoom.id } })
        emit('select-room', formattedRoom)
        ElMessage.success(`已進入您的房間: ${formattedRoom.name}`)
      } else {
        ElMessage.info(res.message || '您目前不在任何自習室中')
        isInRoom.value = false
      }
    } else {
      ElMessage.error(res.message || '查詢我的房間失敗')
    }
  } catch (e) {
    console.error('查詢我的房間失敗:', e)
    ElMessage.error('查詢我的房間失敗: ' + e.message)
  }
}

function connectWebSocket() {
  const socket = new SockJS('http://localhost:8080/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    // 訂閱新房間創建事件
    stompClient.subscribe('/topic/rooms', (message) => {
      const newRoom = JSON.parse(message.body)
      const formattedRoom = {
        ...newRoom,
        type: typeMap[newRoom.type] || newRoom.type,
        tags: newRoom.topics.map(topic => topicMap[topic] || topic),
        avatar: handleAvatar(newRoom.avatar || newRoom.creatorAvatar)
      }
      if (!props.rooms.some(room => room.id === formattedRoom.id)) {
        props.rooms.push(formattedRoom)
        ElMessage.success(`新房間 "${formattedRoom.name}" 已創建`)
      }
      if (userId && newRoom.creatorId === parseInt(userId)) {
        isInRoom.value = true
      }
    })

    // 初始訂閱當前房間列表的 topic
    subscribeToAllRooms()
  }, (error) => {
    console.error('WebSocket 連接失敗:', error)
    ElMessage.error('無法連接到實時更新服務')
  })
}

// 新增：訂閱所有房間的 topic
function subscribeToAllRooms() {
  props.rooms.forEach(room => {
    const topic = `/topic/room/${room.id}`
    if (!subscribedRooms.value.has(room.id)) {
      stompClient.subscribe(topic, (message) => {
        const body = message.body
        const roomId = parseInt(message.headers.destination.split('/').pop())
        const targetRoom = props.rooms.find(r => r.id === roomId)
        if (targetRoom) {
          if (body === 'closed') {
            props.rooms.splice(props.rooms.indexOf(targetRoom), 1)
            unsubscribeFromRoom(roomId) // 取消訂閱已關閉房間
            if (userId) {
              checkIfInRoom()
            }
          } else {
            targetRoom.currentUsers = parseInt(body)
          }
        }
      })
      subscribedRooms.value.add(room.id)
    }
  })
}

// 新增：取消特定房間的訂閱
function unsubscribeFromRoom(roomId) {
  if (stompClient && subscribedRooms.value.has(roomId)) {
    stompClient.unsubscribe(`/topic/room/${roomId}`)
    subscribedRooms.value.delete(roomId)
  }
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    fetchRooms()
    connectWebSocket()
  } else {
    disconnectWebSocket()
  }
})

watch(() => userId, () => {
  checkIfInRoom()
}, { immediate: true })
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 2000;
}

.study-room {
  background-color: #f8f1e9;
  width: 60%;
  padding-left: 40px;
  padding-right: 40px;
  max-width: 1000px;
  height: 90vh;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #e6d8c6;
}

.header h2 {
  font-size: 1.5rem;
  color: #4a2f1a;
  font-weight: 500;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nullTip {
  opacity: 0.6;
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
  background-color: #fffaf4;
  border: 1px solid #e6d8c6;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  background-color: #d9c8b0;
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
  color: #4a2f1a;
}

.room-info p {
  margin: 5px 0;
  color: #6b4e31;
  font-size: 0.9rem;
}

.room-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: #8a6d4a;
}

.time {
  color: #8a6d4a;
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
