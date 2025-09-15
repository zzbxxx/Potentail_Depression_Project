<template>
  <div class="emergency-help-container">
    <div class="gentle-help-btn" @click="showHelpOptions = !showHelpOptions">
      <el-icon><Help /></el-icon>
      <span>我需要帮助</span>
    </div>
    
    <transition name="fade">
      <div v-if="showHelpOptions" class="help-options-panel">
        <div class="help-option" @click="contactSupport">
          <el-icon><ChatRound /></el-icon>
          <span>联系支持人员</span>
        </div>
        <div class="help-option" @click="showSelfHelp">
          <el-icon><Reading /></el-icon>
          <span>自助指南</span>
        </div>
        <div class="help-option emergency-option" @click="showEmergencyDialog">
          <el-icon><Warning /></el-icon>
          <span>紧急情况</span>
        </div>
      </div>
    </transition>
    
    <el-dialog
      v-model="showEmergencyDialogVisible"
      title="紧急帮助"
      width="90%"
      :close-on-click-modal="true"
    >
      <div class="emergency-dialog-content">
        <p>您需要哪种紧急帮助？</p>
        <div class="emergency-actions">
          <el-button type="primary" @click="callEmergency">
            <el-icon><Phone /></el-icon>
            拨打紧急电话
          </el-button>
          <el-button @click="sendEmergencyAlert">
            <el-icon><Bell /></el-icon>
            发送紧急警报
          </el-button>
          <el-button @click="showEmergencyDialogVisible = false">
            取消
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import {
  Help,
  ChatRound,
  Reading,
  Warning,
  Phone,
  Bell
} from '@element-plus/icons-vue'

onMounted(() =>{
  document.addEventListener('click', handleClickOutside , true)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside , true)
})

const showHelpOptions = ref(false)
const showEmergencyDialogVisible = ref(false)
const helpContainerRef = ref<HTMLElement>()

const handleClickOutside = (e: MouseEvent) => {
  if (!showHelpOptions.value) return
  const target = e.target as HTMLElement
  if (
    !helpContainerRef.value?.contains(target) &&           // 不在面板里
    !(target.closest('.gentle-help-btn'))                  // 也不在按钮上
  ) {
    showHelpOptions.value = false
  }
}

const contactSupport = () => {
  showHelpOptions.value = false
  // 这里实现联系支持人员的逻辑
  console.log('联系支持人员')
}

const showSelfHelp = () => {
  showHelpOptions.value = false
  // 这里展示自助指南
  console.log('展示自助指南')
}

const showEmergencyDialog = () => {
  showHelpOptions.value = false
  showEmergencyDialogVisible.value = true
}

const callEmergency = () => {
  // 这里替换为实际的紧急联系电话
  window.location.href = 'tel:110'
  showEmergencyDialogVisible.value = false
}

const sendEmergencyAlert = async () => {
  try {
    // 这里实现发送紧急警报的逻辑
    console.log('发送紧急警报')
    showEmergencyDialogVisible.value = false
  } catch (error) {
    console.error('发送失败:', error)
  }
}
</script>

<style scoped>
.emergency-help-container {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 21000;
  width: 12rem;
  background-color: aliceblue;
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 温和的帮助按钮 */
.gentle-help-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  border: 1px solid #90caf9;
}

.gentle-help-btn:hover {
  background: linear-gradient(135deg, #bbdefb 0%, #90caf9 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(25, 118, 210, 0.2);
}

/* 帮助选项面板 */
.help-options-panel {
  margin-top: 10px;
  background: white;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.help-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 5px;
}

.help-option:hover {
  background-color: #f5f5f5;
}

.help-option:last-child {
  margin-bottom: 0;
}

.emergency-option {
  color: #f44336;
}

.emergency-option:hover {
  background-color: #ffebee;
}

/* 紧急对话框 */
.emergency-dialog-content {
  text-align: center;
  padding: 20px;
}

.emergency-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>