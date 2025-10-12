<template>
  <div class="MainPage">
    <div class="edit-button-wrapper">
      <el-button
        ref="buttonRef"
        type="primary"
        :icon="Edit"
        @click="toggleRecoveryCode"
        class="deep-sea-btn"
        v-if="!isAccountLogin"
      >
        {{ showRecoveryCode ? '收起找回码' : '打开找回码' }}
      </el-button>
    </div>

    <el-button 
      type="primary" 
      class="deep-sea-action-btn"
      @click="goToDateLog"
      :icon="Clock" 
    >
      <span>时间日志</span>
    </el-button>

    <el-button 
      type="primary" 
      class="personal-btn"
      @click="goToEmail"
      :icon="Clock" 
    >
      <span>个人信息</span>
    </el-button>

    <el-button 
      type="primary" 
      class="editor-btn"
      @click="gotoEditor"
      :icon="Clock" 
    >
      <span>编辑文章</span>
    </el-button>

    <el-button
      type="primary"
      class="audit-bth"
      @click="gotoAudit"
      :icon="Clock"
    >
      <span>管理中心</span>
    </el-button>

    <RecoveryCode
      v-model="showRecoveryCode" 
      :button-ref="buttonElement"
    />

    <el-button 
      class="home-btn" 
      type="primary" 
      :icon="ArrowLeft" 
      @click="goToHome"
    >
      首页
    </el-button>

    <CardGallery3D 
      :cards="[
        { id: 'today', title: '今日卡片', imageUrl: todayCardImage },
        { id: 'anon', title: '匿名树洞', imageUrl: anonCardImage },
        { id: 'progress', title: '小小进展', imageUrl: progressCardImage }
      ]"
      width="100%"
      height="300px"
      :responsive="true"
      @select="handleCardSelect"
    />

    <card-popup
      v-model="show"
      @open="onOpen"
      @close="onClose"
    />

    <AnnoHole
      v-model="annoShow"
    />

    <StudyRoom
      v-model="studyRoomShow"
      :rooms="rooms"
      @select-room="handleRoomSelect"
      @create-room="handleCreateRoom"
    />

    <CreateStudyRoom
      v-model="createRoomShow"
      @create-room="addNewRoom"
      @update:modelValue="handleCreateRoomCancel"
    />

    <div style="background-color: aliceblue;">
      <HelpComponent />
    </div>
  </div>
</template>

<script setup>
import { ArrowLeft, Edit, Clock } from '@element-plus/icons-vue'
import { onMounted, watch, nextTick, ref } from 'vue'
import RecoveryCode from '../components/RecoveryCode.vue'
import HelpComponent from '../components/HelpComponent.vue'
import CardGallery3D from '../components/CardGallery3D.vue'
import CardPopup from '../components/CardPopup.vue'
import AnnoHole from '../components/AnnoHole.vue'
import StudyRoom from '../components/StudyRoom.vue'
import CreateStudyRoom from '../components/CreateStudyRoom.vue'
import { useRouter } from 'vue-router'
import todayCardImage from '../assets/image/today-card.jpg'
import anonCardImage from '../assets/image/anon-card.jpg'
import progressCardImage from '../assets/image/progress-card.jpg'

const showRecoveryCode = ref(false)
const buttonRef = ref(null)
const buttonElement = ref(null)
const clickCount = ref(0)
const show = ref(false)
const isAccountLogin = ref(true)
const annoShow = ref(false)
const studyRoomShow = ref(false)
const createRoomShow = ref(false)
const router = useRouter()

const rooms = ref([
  { 
    id: 1, 
    name: '专注学习室1', 
    type: '嚴格番茄鐘', 
    tags: ['數學', '科學'], 
    currentUsers: 5, 
    maxUsers: 6, 
    creatorId: 1001, 
    status: '进行中', 
    createdAt: '2025-10-12T10:00:00', 
    avatar: new URL('../assets/image/avatar2.jpg', import.meta.url).href
  },
  { 
    id: 2, 
    name: '深夜自习室', 
    type: '自由計時', 
    tags: ['繪畫', '文學'], 
    currentUsers: 3, 
    maxUsers: 6, 
    creatorId: 1002, 
    status: '进行中', 
    createdAt: '2025-10-12T22:00:00', 
    avatar: new URL('../assets/image/avatar3.jpg', import.meta.url).href
  },
  { 
    id: 3, 
    name: '早起学习室', 
    type: '互助房', 
    tags: ['哲學', '歷史'], 
    currentUsers: 6, 
    maxUsers: 6, 
    creatorId: 1003, 
    status: '进行中', 
    createdAt: '2025-10-12T06:00:00', 
    avatar: new URL('../assets/image/avatar3.jpg', import.meta.url).href
  },
  { 
    id: 4, 
    name: '编程冲刺室', 
    type: '編程學習', 
    tags: ['編程學習', '科學'], 
    currentUsers: 4, 
    maxUsers: 6, 
    creatorId: 1004, 
    status: '进行中', 
    createdAt: '2025-10-12T15:00:00', 
    avatar: new URL('../assets/image/avatar2.jpg', import.meta.url).href
  },
])

const goToHome = () => {
  window.location.href = '/'
}

function gotoEditor() {
  router.push('/editor')
}

async function goToDateLog() {
  router.push('/mood-log')
}

async function goToEmail() {
  router.push('/personal-center')
}

async function gotoAudit() {
  router.push('/admin-control')
}

const getButtonElement = async () => {
  if (!buttonRef.value) return null
  await nextTick()
  return buttonRef.value.$el || buttonRef.value
}

function handleCardSelect(card, index) {
  if (card.id === 'today') {
    clickCount.value++
    if (clickCount.value === 2) {
      show.value = true
      clickCount.value = 0
    }
  } else if (card.id === 'anon') {
    clickCount.value++
    if (clickCount.value === 2) {
      annoShow.value = true
      clickCount.value = 0
    }
  } else if (card.id === 'progress') {
    clickCount.value++
    if (clickCount.value === 2) {
      studyRoomShow.value = true
      clickCount.value = 0
    }
  }
}

function handleRoomSelect(room) {
  console.log(`加入自習室: ${room.name}`)
}

function handleCreateRoom() {
  studyRoomShow.value = false
  createRoomShow.value = true
}

function handleCreateRoomCancel(value) {
  createRoomShow.value = value
  if (!value) {
    studyRoomShow.value = true // 取消時返回 StudyRoom
  }
}

function addNewRoom(newRoom) {
  rooms.value.push(newRoom)
  createRoomShow.value = false
  studyRoomShow.value = true // 創建後返回 StudyRoom
}

watch([showRecoveryCode, buttonRef], async ([newShowVal, newButtonVal]) => {
  if (newButtonVal) {
    buttonElement.value = await getButtonElement()
  }
})

onMounted(async () => {
  const user_id = localStorage.getItem("user_id")
  if (user_id) {
    isAccountLogin.value = false
  }
  await nextTick()
  buttonElement.value = await getButtonElement()
})

const toggleRecoveryCode = () => {
  showRecoveryCode.value = !showRecoveryCode.value
}
</script>

<style scoped>
.MainPage {
  font-family: 'Arial', sans-serif;
  color: #2C3E50;
  height: 91vh;
  margin: 0;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e6f0fa 0%, #f5e6e8 100%);
  background-size: auto, 100px 100px;
  background-attachment: fixed;
  transition: all 0.5s ease;
  line-height: 1.6;
  position: relative;
  overflow: hidden;
}

.edit-button-wrapper {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

.personal-btn, .editor-btn, .audit-bth {
  position: fixed;
  right: 2rem;
  z-index: 1000;
  width: 6rem;
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.personal-btn { top: 4rem; }
.editor-btn { top: 8rem; }
.audit-bth { top: 12rem; }

.personal-btn:hover, .editor-btn:hover, .audit-bth:hover {
  background: linear-gradient(135deg, #0d2b4e 0%, #1a5f9c 100%);
  transform: translateY(-2px);
}

.deep-sea-btn {
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  position: relative;
  top: 8rem;
  left: 0.2rem;
  padding: 1rem;
}

.deep-sea-btn:hover {
  background: linear-gradient(135deg, #0d2b4e 0%, #1a5f9c 100%);
  transform: translateY(-2px);
}

.home-btn {
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  position: fixed;
  bottom: 20px;
  left: 20px;
  font-weight: 500;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

@media (prefers-color-scheme: dark) {
  .MainPage {
    background: linear-gradient(135deg,
        rgba(30, 35, 41, 0.95) 0%,
        rgba(44, 62, 80, 0.95) 100%),
      url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="none" stroke="%233a4a62" stroke-width="0.5"/></svg>');
    color: #E0E0E0;
  }
}

.deep-sea-action-btn {
  position: fixed;
  top: 4rem;
  left: 1rem;
  bottom: 0;
  right: 0;
  margin: auto;
  background: linear-gradient(135deg, #1a5f9c 0%, #0d3b66 100%);
  border: none;
  color: #e0f2fe;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin: 0 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  font-size: 14px;
  max-width: 120px;
}

.deep-sea-action-btn:hover {
  background: linear-gradient(135deg, #0d3b66 0%, #1a5f9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}
</style>