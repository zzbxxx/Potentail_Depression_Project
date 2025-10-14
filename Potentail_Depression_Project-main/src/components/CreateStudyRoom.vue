<template>
  <div class="create-room-overlay" v-if="modelValue" @click.self="close">
    <div class="create-room">
      <div class="create-room-header">
        <h2>創建自習室</h2>
      </div>
      <div class="create-room-content">
        <div class="form-item">
          <label>自習室名稱</label>
          <el-input
            v-model="roomName"
            placeholder="請輸入自習室名稱"
            clearable
            maxlength="50"
          />
        </div>
        <div class="form-item">
          <label>房間頭像</label>
          <div class="avatar-selection">
            <!-- 頭像選擇器 -->
            <div class="avatar-options">
              <img
                v-for="avatar in avatarOptions"
                :key="avatar.value"
                :src="avatar.url"
                class="avatar-option"
                :class="{ 'selected': roomAvatar === avatar.value }"
                @click="selectAvatar(avatar.value)"
                alt="頭像選項"
                @error="handleAvatarError"
              />
            </div>
            <!-- 預覽區域 -->
            <div class="avatar-preview-container">
              <img
                v-if="roomAvatar"
                :src="avatarOptions.find(a => a.value === roomAvatar)?.url || defaultAvatar"
                class="avatar-preview"
                alt="頭像預覽"
                @error="handleAvatarError"
              />
              <span v-else class="no-avatar">尚未選擇頭像</span>
            </div>
          </div>
        </div>
        <div class="form-item">
          <label>類型</label>
          <el-select v-model="roomType" placeholder="選擇房間類型" class="w-1/2">
            <el-option label="嚴格番茄鐘" value="嚴格番茄鐘" />
            <el-option label="自由計時" value="自由計時" />
            <el-option label="互助房" value="互助房" />
            <el-option label="編程學習" value="編程學習" />
          </el-select>
        </div>
        <div class="form-item">
          <label>標簽（可多選，最多3個）</label>
          <el-select
            v-model="topics"
            multiple
            filterable
            allow-create
            default-first-option
            :max="3"
            placeholder="選擇或輸入話題（最多3個）"
            class="w-1/2"
          >
            <el-option v-for="item in topicOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="form-item">
          <label>最大人數</label>
          <el-input-number
            v-model="maxUsers"
            :min="1"
            :max="6"
            :step="1"
            controls-position="right"
          />
        </div>
      </div>
      <div class="create-room-footer">
        <button class="confirm-btn" @click="handleCreate">創建</button>
        <button class="cancel-btn" @click="close">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import RoomService from '/src/api/roomApi.js'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'create-room'])

const roomName = ref('')
const roomType = ref('嚴格番茄鐘')
const topics = ref([])
const maxUsers = ref(6)
const roomAvatar = ref('')
const defaultAvatar = new URL('../assets/image/default-avatar.png', import.meta.url).href

// 頭像選項，value 為後端期望的字符串，url 為前端顯示的路徑
const avatarOptions = ref([
  { label: '頭像 1', value: 'happy1', url: new URL('../assets/image/happy1.jpg', import.meta.url).href },
  { label: '頭像 2', value: 'sweet1', url: new URL('../assets/image/sweet1.jpg', import.meta.url).href },
  { label: '頭像 3', value: 'kind1', url: new URL('../assets/image/kind1.jpg', import.meta.url).href }
])

// 類型映射：前端中文 -> 後端英文
const typeMap = {
  '嚴格番茄鐘': 'strict_tomato',
  '自由計時': 'free_time',
  '互助房': 'mutual_aid',
  '編程學習': 'programming'
}

// 話題映射：前端中文 -> 後端英文
const topicMap = {
  '數學': 'Math',
  '繪畫': 'Painting',
  '哲學': 'Philosophy',
  '文學': 'Literature',
  '歷史': 'History',
  '科學': 'Physics',
  '美食': 'Food',
  '旅遊': 'Travel',
  '學習': 'Study',
  '生活': 'Life',
  '科技': 'Technology'
}

// 話題選項
const topicOptions = ref(['數學', '繪畫', '哲學', '文學', '歷史', '科學', '美食', '旅遊', '學習', '生活', '科技'])

function selectAvatar(avatarValue) {
  roomAvatar.value = avatarValue
}

async function handleCreate() {
  if (!roomName.value.trim()) {
    ElMessage.warning('請輸入自習室名稱');
    return;
  }
  if (!roomType.value) {
    ElMessage.warning('請選擇類型');
    return;
  }
  if (!roomAvatar.value) {
    ElMessage.warning('請選擇頭像');
    return;
  }
  if (topics.value.length > 3) {
    ElMessage.warning('最多選擇3個標籤');
    return;
  }

  try {
    const userId = localStorage.getItem('userId') || localStorage.getItem('user_id');
    if (!userId) {
      ElMessage.error('用戶未登錄');
      return;
    }

    const requestData = {
      userId: Number(userId),
      name: roomName.value.trim(),
      type: typeMap[roomType.value],
      topics: topics.value.map(topic => topicMap[topic] || topic),
      maxUsers: maxUsers.value,
      avatar: roomAvatar.value
    };

    const response = await RoomService.createRoom(requestData);
    if (!response.success) {
      throw new Error(response.message || '創建自習室失敗');
    }

    const newRoom = {
      ...response.data,
      type: Object.keys(typeMap).find(key => typeMap[key] === response.data.type) || response.data.type,
      tags: response.data.topics.map(topic => Object.keys(topicMap).find(key => topicMap[key] === topic) || topic),
      avatar: avatarOptions.value.find(a => a.value === response.data.avatar)?.url || defaultAvatar
    };

    emit('create-room', newRoom);
    close();
  } catch (e) {
    console.error('創建自習室失敗:', e);
    ElMessage.error(e.message || '創建自習室失敗');
  }
}

function close() {
  emit('update:modelValue', false)
  roomName.value = ''
  roomType.value = '嚴格番茄鐘'
  topics.value = []
  maxUsers.value = 6
  roomAvatar.value = ''
}

function handleAvatarError(event) {
  event.target.src = defaultAvatar
}
</script>

<style scoped>
.create-room-overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  z-index: 2000;
}

.create-room {
  background-color: #fffaf4;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.create-room-header {
  padding: 12px 16px;
  background-color: #e6d8c6;
  color: #4a2f1a;
  text-align: center;
}

.create-room-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
}

.create-room-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  color: #4a2f1a;
  font-size: 0.9rem;
  font-weight: 500;
}

.avatar-selection {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.avatar-option {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e6d8c6;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.avatar-option:hover {
  border-color: #a67c52;
}

.avatar-option.selected {
  border-color: #a67c52;
  box-shadow: 0 0 8px rgba(166, 124, 82, 0.5);
}

.avatar-preview-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e6d8c6;
}

.no-avatar {
  color: #4a2f1a;
  font-size: 0.9rem;
}

.create-room-footer {
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
  .create-room {
    width: 90%;
  }

  .avatar-options {
    flex-direction: row;
    justify-content: flex-start;
  }

  .avatar-preview-container {
    margin-top: 8px;
  }
}
</style>
