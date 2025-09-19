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
        :icon="Clock " 
      >
        <span>
          时间日志
        </span>
      </el-button>

      <el-button 
        type="primary" 
        class="personal-btn"
        @click="goToEmail"
        :icon="Clock " 
      >
        <span>
          个人信息
        </span>
      </el-button>

      <el-button 
        type="primary" 
        class="editor-btn"
        @click="gotoEditor"
        :icon="Clock " 
      >
        <span>
          編輯文章
        </span>
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
  </div>

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
    @primary="onAddToList"
    @open="onOpen"
    @close="onClose"
  />

  <AnnoHole
    v-model="annoShow"
  />

  <div style="background-color: aliceblue; ">
    <HelpComponent />
  </div>
</template>

<script setup>
import { ArrowLeft, Edit ,Clock  } from '@element-plus/icons-vue'
import { onMounted, watch, nextTick, ref,reactive } from 'vue'
import RecoveryCode from '../components/RecoveryCode.vue'
import HelpComponent from '../components/HelpComponent.vue'
import CardGallery3D from '../components/CardGallery3D.vue'
import CardPopup from '../components/CardPopup.vue'
import { useRouter } from 'vue-router'
import MoodApiService from '../api/moodApi.js'
import AnnoHole from '../components/AnnoHole.vue'
import todayCardImage from '../assets/image/today-card.jpg'
import anonCardImage from '../assets/image/anon-card.jpg'
import progressCardImage from '../assets/image/progress-card.jpg'
import { log } from 'three/src/nodes/TSL.js'
const showRecoveryCode = ref(false)
const buttonRef = ref(null)
const buttonElement = ref(null)
const clickCount = ref(0)
const show = ref(false)
const isAccountLogin = ref(true)
const submitting = ref(false)
const annoShow = ref(false)
const goToHome = () => {
  window.location.href = '/'
}

function gotoEditor(){
  router.push('/editor')
}

async function onAddToList() {
  submitting.value = true
  try {
    await MoodApiService.addToList(demo.title, demo.author)
    ElMessage.success('已成功加入书单')
  } catch (error) {
    ElMessage.error('加入书单失败')
  } finally {
    submitting.value = false
  }
}
const router = useRouter()
async function goToDateLog() {
  router.push('/mood-log')
}
async function goToEmail(){
  router.push('/personal-compotent')
}

const getButtonElement = async () => {
  if (!buttonRef.value) return null
  
  await nextTick()

  return buttonRef.value.$el || buttonRef.value
}

function handleCardSelect(card, index) {
  if (card.id === 'today') {
    // 打开今日卡片
    clickCount.value++
    if (clickCount.value === 2) {
      show.value = true
      clickCount.value = 0
    }
  } else if (card.id === 'anon') {
    // 打开树洞
    console.log(11);
    
     clickCount.value++
    if (clickCount.value === 2) {
      annoShow.value = true
      clickCount.value = 0
    }
  } else if (card.id === 'progress') {
    // 打开进展
  }
}

watch([showRecoveryCode, buttonRef], async ([newShowVal, newButtonVal]) => {
  if (newButtonVal) {
    buttonElement.value = await getButtonElement()
  }
})

// 组件挂载后获取按钮元素
onMounted(async () => {
  const user_id = localStorage.getItem("user_id")
  if(user_id){
    isAccountLogin.value = false
  }
  
  await nextTick()
  buttonElement.value = await getButtonElement();
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
  background:
    linear-gradient(135deg,
      rgba(220, 229, 243, 0.9) 0%,
      rgba(228, 232, 237, 0.9) 100%),
    url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="none" stroke="%23A3C1E0" stroke-width="0.5"/></svg>');
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
.personal-btn{
  position: fixed;
  right: 2rem;
  top: 4rem;
  z-index: 21000;
  width: 6rem;
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.editor-btn{
  position: fixed;
  right: 2rem;
  top: 8rem;
  z-index: 21000;
  width: 6rem;
  background: linear-gradient(135deg, #1a5f9c 0%, #0d2b4e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e0f2fe;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.personal-btn:hover {
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

/* 暗色模式适配 */
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