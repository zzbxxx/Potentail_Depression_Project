<template>
  <div class="study-room-container">
    <!-- 模式切換按鈕 -->
    <div class="enjoy-toggle">
      <el-switch
        v-model="showBottomPanel"
        :disabled="isToggleLocked"
        active-text="顯示控制面板"
        inactive-text="享受學習模式"
        @update:modelValue="handleTogglePanel"
      />
    </div>

    <!-- 頂部：房間標題與資訊 -->
    <RoomHeader :room="room" />

    <!-- 中心主區：用戶狀態 -->
    <div class="room-content">
      <el-card class="user-status" shadow="never">
        <h2>房間用戶狀態</h2>
        <div class="virtual-room-layout">
          <div
            v-for="(seat, index) in maxSeats"
            :key="index"
            class="seat"
            @click="debounceSeatClick(index)"
            :class="{ 'my-seat': userId.value && getMySeatIndex() === index }"
          >
            <div v-if="userStates[index]" class="user-info">
              <img
                :src="userStates[index].avatar"
                alt="頭像"
                class="user-avatar"
              />
              <p class="user-nickname">
                {{ userStates[index].nickname }}
                {{ userStates[index].userId === userId.value ? '(你)' : '' }}
              </p>
              <p class="user-status">{{ userStates[index].status }}</p>
              <div class="seat-message">
                {{ userStates[index].message || '...' }}
              </div>
            </div>
            <div v-else class="empty-seat" @click="debounceSeatClick(index)">
              {{ index + 1 }}號位 <br> <span class="click-hint">点击入座</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 底部控制面板 -->
    <RoomBottomPanel
      v-model:my-status="myStatus"
      v-model:my-message="myMessage"
      :user-id="userId.value"
      :room-id="route.params.id"
      :stomp-client="stompClient"
      :room-avatar="roomAvatarComputed"
      :show-panel="showBottomPanel"
      @update:room-avatar="updateRoomAvatar"
      @send-message="sendMyMessage"
      @exit-room="confirmExitRoom"
      @back-to-home="backToHome"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RoomService from '/src/api/roomApi.js'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import RoomHeader from '/src/components/room/RoomHeader.vue'
import RoomBottomPanel from '/src/components/room/RoomBottomPanel.vue'
import { debounce } from 'lodash'
const route = useRoute()
const router = useRouter()
const room = ref(null)
const userStates = ref([])
const myStatus = ref('FOCUSED')
const myMessage = ref('')
const showBottomPanel = ref(true)
const userId = ref(localStorage.getItem('userId') || localStorage.getItem('user_id'))
const maxSeats = 6
let stompClient = null

// 🔒 鎖定機制防止頻繁切換
const isToggleLocked = ref(false)
let toggleLockTimer = null

const roomAvatarComputed = computed(() => room.value?.avatar || '')

const statusMap = {
  FOCUSED: '專注中',
  RESTING: '短暫休息',
  IN_TROUBLE: '需要幫助',
  LEFT: '已離開'
}

const typeMap = {
  strict_tomato: '嚴格番茄鐘',
  free_time: '自由計時',
  mutual_aid: '互助房',
  programming: '編程學習'
}

const topicMap = {
  Math: '數學',
  Physics: '科學',
  Painting: '繪畫',
  Philosophy: '哲學',
  Literature: '文學',
  History: '歷史'
}

function handleTogglePanel(newValue) { 
  console.log('🔄 Switch changed to:', newValue, 'locked:', isToggleLocked.value)
  
  if (isToggleLocked.value) {
    console.log('🔒 Locked, ignore')
    return
  }
  
  showBottomPanel.value = newValue
  
  isToggleLocked.value = true
  clearTimeout(toggleLockTimer)
  toggleLockTimer = setTimeout(() => {
    console.log('🔓 Unlock')
    isToggleLocked.value = false
  }, 3000)
  
  ElMessage[newValue ? 'success' : 'info'](
    newValue ? '控制面板已顯示' : '進入享受學習模式'
  )
}

// ✅ 获取我的座位索引
function getMySeatIndex() {
  return userId.value ? userStates.value.findIndex(u => u && u.userId === parseInt(userId.value)) : -1
}

// ✅ 点击座位处理
function handleSeatClick(seatIndex) {
  // 如果已经是我的座位，不做操作
  if (getMySeatIndex() === seatIndex) {
    ElMessage.info('你已在该座位！')
    return
  }
  
  // 如果座位有人，提示不可坐
  if (userStates.value[seatIndex]) {
    ElMessage.warning('座位已被占用！')
    return
  }
  
  // 移动我到新座位
  const myCurrentIndex = getMySeatIndex()
  if (myCurrentIndex !== -1) {
    userStates.value[myCurrentIndex] = null  // 清空原座位
  }
  
  // 添加到新座位
  userStates.value[seatIndex] = {
    userId: parseInt(userId.value),
    nickname: localStorage.getItem('nickname') || `用户${userId.value}`,
    avatar: handleAvatar(), //  实时从 localStorage 获取最新头像
    status: statusMap[myStatus.value] || '專注中',
    message: myMessage.value || '...',
    seatIndex: seatIndex
  }
  localStorage.setItem(`seat_${route.params.id}_${userId.value}`, seatIndex)
  
  ElMessage.success(`入座 ${seatIndex + 1} 號位成功！`)
  
  // 同步到WebSocket（可选）
  if (stompClient) {
    stompClient.send(`/app/room/${route.params.id}/seatChange`, {}, JSON.stringify({
      userId: userId.value,
      seatIndex: seatIndex,
      nickname: localStorage.getItem('nickname') || `用户${userId.value}`,
      avatar: localStorage.getItem('avatar') || ''
    }))
  }
}

function restoreMySeat() {
  const savedSeat = localStorage.getItem(`seat_${route.params.id}_${userId.value}`)
  if (!savedSeat) {
    findEmptySeatForMe()  //  無保存時自動找位
    return
  }
  
  const seatIndex = parseInt(savedSeat)
  if (seatIndex < 0 || seatIndex >= maxSeats) return
  
  //  檢查是否占用
  if (userStates.value[seatIndex] && userStates.value[seatIndex].userId !== parseInt(userId.value)) {
    findEmptySeatForMe()
    return
  }
  
  handleSeatClick(seatIndex)  // 恢復 + 廣播更新後端
}

// 自動找位置
function findEmptySeatForMe() {
  for (let i = 0; i < maxSeats; i++) {
    if (!userStates.value[i]) {
      console.log(`🪑 自動入座：${i + 1}號位`)
      handleSeatClick(i)
      localStorage.setItem(`seat_${route.params.id}_${userId.value}`, i)
      return
    }
  }
  console.log('🪑 房間已滿，無法入座')
}

const debounceSeatClick = debounce(handleSeatClick, 500)

//  每次都实时从 localStorage 获取最新头像
function handleAvatar(avatarName = null) {
  // 如果传入参数为空，从 localStorage 实时获取
  const latestAvatar = avatarName || localStorage.getItem('avatar') || ''
  
  if (latestAvatar && latestAvatar.trim() !== '') {
    return new URL(`/src/assets/image/${latestAvatar}.jpg`, import.meta.url).href
  }
  return new URL('/src/assets/image/default-avatar.png', import.meta.url).href
}

async function fetchRoomDetails() {
  try {
    const roomId = route.params.id
    const res = await RoomService.getRoomById(roomId)
    if (!res.success || !res.data) throw new Error('無效的房間數據')

    room.value = {
      ...res.data,
      type: typeMap[res.data.type] || res.data.type,
      tags: res.data.topics?.map(t => topicMap[t] || t) || [],
      avatar: res.data.avatar || ''
    }
    initializeUserStates(res.data.users || [])
  } catch (e) {
    console.error(e)
    ElMessage.error('獲取自習室詳情失敗')
    router.push({ name: 'MainPage' })
  }
}

// ✅ 修改：头像变更后立即同步到 localStorage 和 WebSocket
async function updateRoomAvatar(newAvatar) {
  try {
    console.log('🎨 开始更新头像:', newAvatar)
    
    // ✅ 第一步：立即更新 localStorage
    localStorage.setItem('avatar', newAvatar)
    console.log('✅ localStorage 已更新:', newAvatar)
    
    // ✅ 第二步：更新我的座位显示
    const myIndex = getMySeatIndex()
    if (myIndex !== -1) {
      userStates.value[myIndex].avatar = handleAvatar(newAvatar)
      console.log('✅ 本地座位头像已更新:', userStates.value[myIndex].avatar)
    }
    
    // ✅ 第三步：API 更新（可选）
    const res = await RoomService.updateRoomAvatar(route.params.id, userId.value, newAvatar)
    if (res.success) {
      room.value.avatar = newAvatar
    }
    
    // ✅ 第四步：WebSocket 广播给其他用户
    if (stompClient) {
      stompClient.send(`/app/room/${route.params.id}/updateAvatar`, {}, JSON.stringify({
        userId: userId.value,
        avatar: newAvatar
      }))
      console.log('✅ WebSocket 已广播头像变更')
    }
    
    ElMessage.success('头像更新成功！')
  } catch (e) {
    console.error('更新头像错误:', e)
    ElMessage.error('更新头像失败')
  }
}

function initializeUserStates(users) {
  userStates.value = new Array(maxSeats).fill(null);

  users.forEach(u => {
    const seatIndex = u.seatIndex || 0;
    const status = u.status || 'FOCUSED';
    const message = u.message || '';
    // const timer = u.timer || 0; // 未來計時器

    if (seatIndex >= 0 && seatIndex < maxSeats) {
      userStates.value[seatIndex] = {
        userId: u.userId,
        nickname: u.nickname,
        avatar: u.userId === parseInt(userId.value) ? handleAvatar() : handleAvatar(u.avatar),
        status: statusMap[status] || status,
        message: message,
        seatIndex: seatIndex
      };
    }
  });
}

async function joinRoom() {
  try {
    const roomId = route.params.id
    const res = await RoomService.joinRoom(roomId, userId.value)
    if (!res.success) throw new Error(res.message)
    
    // 更新 room 數據！
    room.value = {
      ...res.data,
      type: typeMap[res.data.type] || res.data.type,
      tags: res.data.topics?.map(t => topicMap[t] || t) || [],
      avatar: res.data.avatar || ''
    }

    setTimeout(() => {
      restoreMySeat()
    }, 500);
    initializeUserStates(res.data.users || [])
    
    updateMyStatus('FOCUSED')
  } catch (e) {
    ElMessage.error('加入自習室失敗: ' + e.message)
    router.push({ name: 'MainPage' })
  }
}

function confirmExitRoom() {
  ElMessageBox.confirm('確定要退出這個自習室嗎？', '提示', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => backToHome())
}

async function leaveRoom() {
  try {
    await RoomService.leaveRoom(route.params.id, userId.value);
    localStorage.removeItem(`seat_${route.params.id}_${userId.value}`);
    ElMessage.success('退出房間成功');
  } catch (e) {
    ElMessage.error('退出失敗: ' + e.message);
  }
}

async function updateMyStatus(newStatus) {
  if (!stompClient) return
  stompClient.send(`/app/room/${route.params.id}/updateState`, {}, JSON.stringify({
    userId: userId.value, newStatus
  }))
  
  //  API持久化
  await RoomService.updateUserState(route.params.id, userId.value, newStatus, myMessage.value, 0)
}


// 监听头像变更消息
function connectWebSocket() {
  const socket = new SockJS('http://localhost:8080/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/room/${route.params.id}/states`, (msg) => {
      const updatedStates = JSON.parse(msg.body)
      Object.entries(updatedStates).forEach(([uid, status]) => {
        const index = userStates.value.findIndex(u => u && u.userId === parseInt(uid))
        if (index !== -1) userStates.value[index].status = statusMap[status] || status
      })
    })
    stompClient.subscribe(`/topic/room/${route.params.id}/messages`, (msg) => {
      const { userId: uid, message } = JSON.parse(msg.body)
      const index = userStates.value.findIndex(u => u && u.userId === uid)
      if (index !== -1) userStates.value[index].message = message
    })
    stompClient.subscribe(`/topic/room/${route.params.id}/seatUpdate`, (msg) => {
      const { userId, seatIndex, nickname, avatar } = JSON.parse(msg.body)
      console.log('🪑 收到座位更新:', userId, '->', seatIndex + 1, '號位')
      
      updateSeatForUser(userId, seatIndex, nickname, avatar)
    })
    stompClient.subscribe(`/topic/room/${route.params.id}/fullUpdate`, (msg) => {
        console.log(' 收到完整房間更新:', msg.body);
        const updatedRoom = JSON.parse(msg.body);

        // 更新 room 數據
        room.value = {
          ...updatedRoom,
          type: typeMap[updatedRoom.type] || updatedRoom.type,
          tags: updatedRoom.topics?.map(t => topicMap[t] || t) || [],
          avatar: updatedRoom.avatar || ''
        };

        // 重新初始化所有座位狀態
        initializeUserStates(updatedRoom.users || []);

        // 如果用戶已離開房間，導航回主頁
        const isUserInRoom = updatedRoom.users?.some(u => u.userId === parseInt(userId.value));
        if (!isUserInRoom && route.name === 'StudyRoomPage') {
          ElMessage.warning('您已不在房間中，即將返回主頁');
          router.push({ name: 'MainPage' });
        }

        ElMessage.info('房間狀態已更新');
      });
    stompClient.subscribe(`/topic/room/${route.params.id}/avatarUpdate`, (msg) => {
      const { userId: uid, avatar } = JSON.parse(msg.body)
      console.log('📸 收到头像更新:', uid, avatar)
      
      // 更新本地 userStates
      const index = userStates.value.findIndex(u => u && u.userId === parseInt(uid))
      if (index !== -1) {
        userStates.value[index].avatar = handleAvatar(avatar) // 重新生成路径
        console.log(' 本地头像更新成功:', userStates.value[index].avatar)
      }
      
      // 如果是自己的头像，也更新 localStorage
      if (parseInt(uid) === parseInt(userId.value)) {
        localStorage.setItem('avatar', avatar)
      }
    })
  })
}

function updateSeatForUser(userId, seatIndex, nickname, avatar) {
  console.log('🪑 更新座位:', userId, '->', seatIndex + 1, '號位')

  if (seatIndex === -1) {
    userStates.value = userStates.value.map(state => {
      if (state && state.userId === parseInt(userId)) {
        console.log('👋 用户离开，清空座位:', userId)
        return null;
      }
      return state;
    })
    ElMessage.success(`${nickname || '用户'} 已离开房间`)
    return
  }
  
  // 原有逻辑：移动座位
  userStates.value = userStates.value.map(state => {
    if (state && state.userId === parseInt(userId)) return null;
    return state;
  })
  
  if (seatIndex < maxSeats) {
    userStates.value[seatIndex] = {
      userId: parseInt(userId),
      nickname,
      avatar: handleAvatar(avatar),
      status: '專注中',
      message: '...',
      seatIndex
    }
  }
}

function disconnectWebSocket() {
  if (stompClient) stompClient.disconnect()
}

function backToHome() {
  leaveRoom()
    .then(() => {
      disconnectWebSocket() 
      router.push({ name: 'MainPage' })
    })
    .catch(e => {
      disconnectWebSocket()
      router.push({ name: 'MainPage' })
    })
}

onMounted(async () => {
  await fetchRoomDetails()
  await joinRoom()

  connectWebSocket()
    setTimeout(()=>{
    restoreMySeat()
  }, 1000)
})

onUnmounted(() => {
  disconnectWebSocket()
  clearTimeout(toggleLockTimer)
})
</script>

<style scoped>
.study-room-container {
  background-color: #e8ede7;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.enjoy-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1100;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.room-content {
  flex: 1;
  width: 100%;
  max-width: 1000px;
  background-color: #fefcf9;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 25px 20px 90px;
  position: relative;
  overflow-y: auto; 
  max-height: calc(100vh - 140px);
}

.virtual-room-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 25px;
  justify-items: center;
  padding-bottom: 20px;
}

.seat {
  background-color: #f9faf8;
  border: 1px solid #d3d8cf;
  border-radius: 10px;
  width: 100%;
  max-width: 200px;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  transition: all 0.2s ease;
}
.seat:hover {
  transform: translateY(-3px);
  box-shadow: 0 3px 6px rgba(0,0,0,0.05);
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px solid #c7d2c3;
  margin-bottom: 6px;
}
.user-nickname { font-weight: 500; color: #3b3b3b; margin-bottom: 4px; }
.user-status { font-size: 0.85rem; color: #6b766c; }
.seat-message {
  width: 100%;
  background: #f1f4f0;
  border-radius: 6px;
  font-size: 0.85rem;
  padding: 4px 6px;
  text-align: center;
  color: #666;
  min-height: 26px;
}
.empty-seat { color: #aaa; font-style: italic; }

@media (max-width: 768px) {
  .virtual-room-layout { grid-template-columns: repeat(2, 1fr); gap: 15px; }
  .seat { min-height: 150px; }
}

/* 我的座位高亮 */
.my-seat {
  border-color: #a67c52 !important;
  background: linear-gradient(135deg, #fff8e1, #f5e8c7) !important;
  box-shadow: 0 4px 12px rgba(166, 124, 82, 0.3);
}

.click-hint {
  font-size: 0.7rem;
  color: #a67c52;
  margin-top: 4px;
}

.empty-seat {
  cursor: pointer !important;
  color: #666 !important;
  text-align: center;
  padding: 20px 10px;
  transition: all 0.2s ease;
}

.empty-seat:hover {
  background-color: #e3f2fd !important;
  transform: scale(1.05);
  color: #1976d2 !important;
}
</style>