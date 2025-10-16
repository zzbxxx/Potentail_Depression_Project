<template>
  <div class="bottom-panel" v-show="showPanel">
    <div class="panel-main">
      <el-select v-model="localMyStatus" @change="handleStatusChange" class="status-select">
        <el-option label="專注中" value="FOCUSED"></el-option>
        <el-option label="短暫休息" value="RESTING"></el-option>
        <el-option label="需要幫助" value="IN_TROUBLE"></el-option>
      </el-select>

      <el-input
        v-model="localMyMessage"
        placeholder="想說的話（會顯示在座位）"
        class="message-input"
      ></el-input>

      <el-button type="success" @click="handleSendMessage">發送</el-button>
    </div>

    <!-- 新增：頭像選擇區域 -->
    <div class="avatar-section">
      <div class="avatar-selection">
        <div class="avatar-options">
          <img
            v-for="avatar in avatarOptions"
            :key="avatar.value"
            :src="avatar.url"
            class="avatar-option"
            :class="{ 'selected': roomAvatar === avatar.value }"
            @click="debounceSelectAvatar(avatar.value)"
            alt="頭像選項"
            @error="handleAvatarError"
          />
        </div>
        <div class="avatar-preview-container">
          <img
            v-if="roomAvatar"
            :src="avatarOptions.find(a => a.value === roomAvatar)?.url || defaultAvatar"
            class="avatar-preview"
            alt="頭像預覽"
            @error="handleAvatarError"
          />
          <span v-else class="no-avatar">房間頭像</span>
        </div>
      </div>
    </div>

    <!-- 操作按鈕區域 -->
    <div class="panel-actions">
      <el-button type="danger" @click="handleExitRoom">退出自習室</el-button>
      <el-button @click="handleBackToHome">返回首頁</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue' 
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash'
import { toRefs } from 'vue'

const props = defineProps({
  myStatus: String,
  myMessage: String,
  userId: String,
  roomId: String,
  stompClient: Object,
  roomAvatar: String,
  showPanel: {
    type: Boolean,
    default: true
  }
})

const { showPanel } = toRefs(props)

const emit = defineEmits([
  'update:myStatus',
  'update:myMessage',
  'sendMessage',
  'exitRoom',
  'backToHome',
  'update:roomAvatar',
])

const localMyStatus = ref(props.myStatus)
const localMyMessage = ref(props.myMessage)
const roomAvatar = ref(props.roomAvatar || '')  // 新增：本地房間頭像狀態
const defaultAvatar = new URL('../assets/image/default-avatar.png', import.meta.url).href

// 頭像選項（完全借鑒創建房間）
const avatarOptions = ref([
  { label: '頭像 1', value: 'happy1', url: new URL('/src/assets/image/happy1.jpg', import.meta.url).href },
  { label: '頭像 2', value: 'sweet1', url: new URL('/src/assets/image/sweet1.jpg', import.meta.url).href },
  { label: '頭像 3', value: 'kind1', url: new URL('/src/assets/image/kind1.jpg', import.meta.url).href }
])

// 雙向綁定狀態
watch(() => props.myStatus, (newVal) => {
  localMyStatus.value = newVal
})
watch(() => props.myMessage, (newVal) => {
  localMyMessage.value = newVal
})
watch(() => props.roomAvatar, (newVal) => {
  roomAvatar.value = newVal
})

// 狀態改變
function handleStatusChange(newStatus) {
  emit('update:myStatus', newStatus)
}
// 發送消息
function handleSendMessage() {
  if (localMyMessage.value.trim()) {
    emit('sendMessage', localMyMessage.value)
    localMyMessage.value = ''
  }
}

// 選擇頭像
function selectAvatar(avatarValue) {
  roomAvatar.value = avatarValue
  emit('update:roomAvatar', avatarValue)
}

const debounceSelectAvatar = debounce(selectAvatar, 500)

// 退出房間
function handleExitRoom() {
  emit('exitRoom')
}

// 返回首頁
function handleBackToHome() {
  emit('backToHome')
}

function handleAvatarError(event) {
  event.target.src = defaultAvatar
}
</script>

<style scoped>
.bottom-panel {
  position: fixed;
  bottom: 0; 
  left: 0; 
  right: 0;
  background-color: rgba(250,249,247,0.95);
  backdrop-filter: blur(8px);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  z-index: 1000;
}

/* ✅ 新增：右上角隱藏按鈕樣式 */
.hide-btn {
  position: absolute;
  top: 8px;
  right: 16px;
  background: rgba(166, 124, 82, 0.8) !important;
  border: none !important;
  color: white !important;
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.hide-btn:hover {
  background: rgba(166, 124, 82, 1) !important;
  transform: scale(1.1);
}
.bottom-panel {
  position: fixed;
  bottom: 0; 
  left: 0; 
  right: 0;
  background-color: rgba(250,249,247,0.95);
  backdrop-filter: blur(8px);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  z-index: 1000;
}

.panel-main {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-select {
  width: 120px;
}

.message-input {
  width: 180px;
}

.panel-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 頭像選擇區域樣式 */
.avatar-section {
  display: flex;
  justify-content: center;
}

.avatar-selection {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.8);
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #e6d8c6;
}

.avatar-options {
  display: flex;
  gap: 6px;
}

.avatar-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e6d8c6;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-option:hover {
  border-color: #a67c52;
  transform: scale(1.1);
}

.avatar-option.selected {
  border-color: #a67c52;
  box-shadow: 0 0 6px rgba(166, 124, 82, 0.5);
}

.avatar-preview-container {
  display: flex;
  align-items: center;
  min-width: 40px;
}

.avatar-preview {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e6d8c6;
}

.no-avatar {
  color: #4a2f1a;
  font-size: 0.75rem;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .bottom-panel { 
    padding: 10px 12px;
    gap: 6px;
  }
  
  .panel-main { 
    flex-direction: column; 
    gap: 6px; 
    width: 100%;
  }
  
  .status-select,
  .message-input {
    width: 100%;
    max-width: 280px;
  }
  
  .avatar-selection {
    gap: 8px;
    padding: 4px 8px;
  }
  
  .avatar-option {
    width: 28px;
    height: 28px;
  }
  
  .avatar-preview {
    width: 32px;
    height: 32px;
  }
}
</style>