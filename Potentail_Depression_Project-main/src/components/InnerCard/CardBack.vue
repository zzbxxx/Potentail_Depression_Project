<template>
  <article class="card card-back">
    <section class="card-content">
      <h3 class="back-title">分享你今天的感觉吧～</h3>

      <!-- 事件类别选择 -->
      <div class="event-selection">
        <div
          v-for="event in eventOptions"
          :key="event.key"
          class="event-item"
          :class="{ selected: selectedEvents.includes(event.key) }"
          @click="toggleEvent(event)"
        >
          <span class="event-label">{{ event.label }}</span>
        </div>
      </div>

      <!-- 情绪类型选择 -->
      <div class="mood-selection">
        <div
          v-for="mood in moodOptions"
          :key="mood.key"
          class="mood-item"
          :class="{ selected: selectedMoods.includes(mood.key) }"
          @click="toggleMood(mood)"
        >
          <img :src="mood.image" :alt="mood.label" class="mood-image" />
          <span class="mood-label">{{ mood.label }}</span>
        </div>
      </div>

      <!-- 情绪强度滑块（为每种情绪生成一个滑块） -->
      <div class="intensity-selection" v-if="selectedMoods.length > 0">
        <div v-for="mood in selectedMoods" :key="mood" class="intensity-item">
          <el-form-item :label="`情绪强度 - ${getMoodLabel(mood)}`">
            <el-slider
              v-model="form.intensities[mood]"
              :min="0"
              :max="1"
              :step="0.1"
              show-input
              :format-tooltip="formatTooltip"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 文本输入和语音输入 -->
      <el-form
        class="mood-form"
        :model="form"
        ref="formRef"
        label-position="top"
      >
        <el-form-item label="想对自己说点什么？（可选）">
          <div class="input-wrapper">
            <el-input
              v-model="form.text"
              type="textarea"
              :rows="3"
              placeholder="说点什么都可以～"
              maxlength="200"
              show-word-limit
            />
            <el-button
              class="voice-btn"
              :class="{ 'recording': isRecording }"
              circle
              @click="toggleVoiceInput"
              :disabled="!speechRecognitionAvailable"
            >
              <el-icon>
                <Microphone v-if="!isRecording" />
                <VideoPause v-else />
              </el-icon>
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </section>

    <footer class="card-footer" v-if="selectedMoods.length > 0">
      <el-button size="small" class="ghost-btn" @click="$emit('close')">
        稍后再说
      </el-button>
      <el-button
        size="small"
        type="primary"
        class="calm-btn"
        :loading="submitting"
        @click="onSubmit"
      >
        记录心情
      </el-button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { Microphone, VideoPause } from '@element-plus/icons-vue'
import happyImage from '/src/assets/moods/happy.png'
import neutralImage from '/src/assets/moods/neutral.png'
import sadImage from '/src/assets/moods/sad.png'
import despairImage from '/src/assets/moods/despair.png'
// @ts-ignore
import { withDefaults, defineProps, defineEmits } from '/src/utils/props'
// @ts-ignore
import MoodApiService from '/src/api/moodApi.js'

interface EventOption {
  key: string
  label: string
}

interface MoodOption {
  key: string
  label: string
  image: string
}

interface Props {
  modelValue?: {
    events?: string[]
    moods?: string[]
    intensities?: Record<string, number> // 修改为对象，存储每种情绪的强度
    text?: string
  }
  submitting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ events: [], moods: [], intensities: {}, text: '' }),
  submitting: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: { events: string[]; moods: string[]; intensities: Record<string, number>; text: string }): void
  (e: 'submit', payload: { events: string[]; moods: string[]; intensities: Record<string, number>; text: string }): void
  (e: 'close'): void
}>()

const eventOptions = ref<EventOption[]>([
  { key: 'family', label: '家庭' },
  { key: 'love', label: '恋爱' },
  { key: 'study', label: '学业' },
  { key: 'work', label: '工作' },
  { key: 'other', label: '其他' }
])

const moodOptions = ref<MoodOption[]>([
  { key: 'happy', label: '乐观', image: happyImage as string },
  { key: 'calm', label: '平静', image: neutralImage as string }, // 将 "neutral" 改为 "calm"
  { key: 'sad', label: '低落', image: sadImage as string },
  { key: 'anxious', label: '焦虑', image: despairImage as string } // 将 "despair" 改为 "anxious"
])

const selectedEvents = ref<string[]>(props.modelValue?.events || [])
const selectedMoods = ref<string[]>(props.modelValue?.moods || [])
const form = reactive({
  intensities: props.modelValue?.intensities || {}, // 存储每种情绪的强度
  text: props.modelValue?.text || ''
})

// 当选择情绪时，初始化强度值
watch(selectedMoods, (newMoods, oldMoods) => {
  newMoods.forEach((mood) => {
    if (!(mood in form.intensities)) {
      form.intensities[mood] = 0.5 // 默认强度为 50%
    }
  })
  // 移除未选择情绪的强度值
  Object.keys(form.intensities).forEach((mood) => {
    if (!newMoods.includes(mood)) {
      delete form.intensities[mood]
    }
  })
})

// 监听表单变化并触发更新
watch(
  () => [form.intensities, form.text, selectedEvents.value, selectedMoods.value],
  () => {
    const payload = {
      events: [...selectedEvents.value],
      moods: [...selectedMoods.value],
      intensities: { ...form.intensities },
      text: form.text.trim()
    }
    emit('update:modelValue', payload)
  },
  { deep: true }
)

// 获取情绪的标签
function getMoodLabel(moodKey: string) {
  const mood = moodOptions.value.find((m) => m.key === moodKey)
  return mood ? mood.label : moodKey
}

function toggleEvent(event: EventOption) {
  const index = selectedEvents.value.indexOf(event.key)
  if (index > -1) {
    selectedEvents.value.splice(index, 1)
  } else {
    selectedEvents.value.push(event.key)
  }
}

function toggleMood(mood: MoodOption) {
  const index = selectedMoods.value.indexOf(mood.key)
  if (index > -1) {
    selectedMoods.value.splice(index, 1)
  } else {
    selectedMoods.value.push(mood.key)
  }
}

function formatTooltip(val: number) {
  return `${(val * 100).toFixed(0)}%`
}

const formRef = ref<FormInstance>()
async function onSubmit() {
  if (selectedEvents.value.length === 0 || selectedMoods.value.length === 0) {
    ElMessage.warning('请至少选择一个事件类别和一种情绪～')
    return
  }
  let userId = localStorage.getItem("userId") || localStorage.getItem("user_id")
  const payload = {
    userId: userId, 
    moodVector: { ...form.intensities },
    events: [...selectedEvents.value],
    text: form.text.trim()
  }
  
  try {
    await MoodApiService.saveMood(payload)
    ElMessage.success('记录成功！')
  } catch (error) {
    ElMessage.error('记录失败，请稍后再试～')
    console.error('Submit error:', error)
  }
}

const submitting = computed(() => props.submitting)

// 语音输入逻辑（保持不变）
const isRecording = ref(false)
const speechRecognitionAvailable = ref(false)
let recognition: SpeechRecognition | null = null

onMounted(() => {
  const SpeechRecognition = window.SpeechRecognition || (window as any).webkitSpeechRecognition
  if (SpeechRecognition) {
    speechRecognitionAvailable.value = true
    recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.continuous = false
    recognition.interimResults = false

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      form.text = transcript
      ElMessage.success('语音输入完成！')
    }

    recognition.onerror = (event) => {
      ElMessage.error('语音输入出错，请稍后再试～')
      console.error('Speech recognition error:', event.error)
      isRecording.value = false
    }

    recognition.onend = () => {
      isRecording.value = false
    }
  } else {
    ElMessage.warning('当前浏览器不支持语音输入，请手动输入～')
  }
})

onUnmounted(() => {
  if (recognition) {
    recognition.stop()
    recognition = null
  }
})

function toggleVoiceInput() {
  if (!recognition) return

  if (isRecording.value) {
    recognition.stop()
    isRecording.value = false
  } else {
    try {
      recognition.start()
      isRecording.value = true
      ElMessage.info('开始录音，说点什么吧～')
    } catch (error) {
      ElMessage.error('无法开始录音，请检查麦克风权限～')
      isRecording.value = false
    }
  }
}
</script>
<style scoped>
/* 其他样式保持不变 */
.card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #fff8e1 0%, #fff3e0 100%);
  color: #5d4037;
  border: 1px solid #ffcc80;
  position: relative;
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

.card-content {
  padding: 16px;
}

.back-title {
  margin: 0 0 12px;
  font-size: 18px;
  color: #5d4037;
  font-weight: 500;
  text-align: center;
}

.event-selection {
  display: flex;
  justify-content: space-around;
  gap: 10px;
  margin-bottom: 16px;
}

.event-item {
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(255, 204, 128, 0.2);
  cursor: pointer;
  transition: transform 0.3s ease, background 0.3s ease;
}

.event-item:hover {
  transform: scale(1.05);
  background: rgba(255, 204, 128, 0.4);
}

.event-item.selected,
.mood-item.selected {
  background: rgba(255, 204, 128, 0.3);
  border: 2px solid #ffcc80;
}

.event-label {
  font-size: 14px;
  color: #5d4037;
}

.mood-selection {
  display: flex;
  justify-content: space-around;
  gap: 10px;
  margin-bottom: 16px;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: transform 0.3s ease, background 0.3s ease;
}

.mood-item:hover {
  transform: scale(1.1);
  background: rgba(255, 204, 128, 0.2);
}

.mood-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 4px;
}

.mood-label {
  font-size: 14px;
  color: #5d4037;
}

.intensity-selection {
  margin-bottom: 16px;
}

.intensity-item {
  margin-bottom: 12px;
}

.mood-form {
  margin-top: 12px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

:deep(.el-form-item__label) {
  color: #5d4037;
  font-size: 14px;
}

:deep(.el-textarea__inner) {
  background: rgba(255, 245, 230, 0.8);
  border-color: #ffcc80;
  color: #5d4037;
  border-radius: 8px;
  padding-right: 60px;
  max-height: 200px;
}

:deep(.el-slider__runway) {
  background: #ffcc80;
}

:deep(.el-slider__bar) {
  background: #ffca28;
}

:deep(.el-slider__button) {
  border: 2px solid #ffb300;
}

.voice-btn {
  position: absolute;
  right: 10px;
  bottom: 20px;
  width: 32px;
  height: 32px;
  background: #ffca28;
  border: 1px solid #ffb300;
  color: #5d4037;
}

.voice-btn.recording {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.card-footer {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  justify-content: flex-end;
}

.calm-btn.el-button {
  background: #ffca28;
  border: 1px solid #ffb300;
  color: #5d4037;
}

.calm-btn.el-button:hover {
  background: #ffb300;
}

.ghost-btn.el-button {
  background: transparent;
  color: #8d6e63;
  border: 1px solid #ffcc80;
}

.ghost-btn.el-button:hover {
  background: rgba(255, 204, 128, 0.2);
  color: #5d4037;
}
</style>