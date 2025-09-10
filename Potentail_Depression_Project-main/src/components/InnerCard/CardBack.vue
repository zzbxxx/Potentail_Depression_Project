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
          @click="selectEvent(event)"
        >
          <span class="event-label">{{ event.label }}</span>
        </div>
      </div>

      <!-- 情绪类型选择 -->
      <div class="mood-selection" >
        <div
          v-for="mood in moodOptions"
          :key="mood.key"
          class="mood-item"
          :class="{ selected: selectedMood?.key === mood.key }"
          @click="selectMood(mood)"
        >
          <img :src="mood.image" :alt="mood.label" class="mood-image" />
          <span class="mood-label">{{ mood.label }}</span>
        </div>
      </div>

      <!-- 情绪强度滑块 -->
      <div class="intensity-selection" v-if="selectedMood">
        <el-form-item label="情绪强度">
          <el-slider
            v-model="form.intensity"
            :min="0"
            :max="1"
            :step="0.1"
            show-input
            :format-tooltip="formatTooltip"
          />

        </el-form-item>
      </div>

      <!-- 文本输入和语音输入 -->
      <el-form
        class="mood-form"
        :model="form"
        ref="formRef"
        label-position="top"
        v-if="selectedMood"
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

    <footer class="card-footer" v-if="selectedMood">
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
    event?: string
    mood?: string
    intensity?: number
    text?: string
  }
  submitting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ event: '', mood: 'happy', intensity: 0.5, text: '' }),
  submitting: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: { event?: string; mood?: string; intensity?: number; text?: string }): void
  (e: 'submit', payload: { event: string; mood: string; intensity: number; text: string }): void
  (e: 'close'): void
}>()

// 明确指定 eventOptions 和 moodOptions 的类型
const eventOptions = ref<EventOption[]>([
  { key: 'family', label: '家庭' },
  { key: 'love', label: '恋爱' },
  { key: 'study', label: '学业' },
  { key: 'work', label: '工作' },
  { key: 'other', label: '其他' }
]);

const moodOptions = ref<MoodOption[]>([
  { key: 'happy', label: '乐观', image: happyImage as string },
  { key: 'neutral', label: '平静', image: neutralImage as string },
  { key: 'sad', label: '低落', image: sadImage as string },
  { key: 'despair', label: '绝望', image: despairImage as string }
]);

const selectedEvent = ref<EventOption | null>(null)
const selectedMood = ref<MoodOption | null>(null)
const form = reactive({
  intensity: props.modelValue?.intensity || 0.5,
  text: props.modelValue?.text || ''
})

if (props.modelValue?.event) {
  //@ts-ignore
  const event = eventOptions.value.find(e => e.key === props.modelValue!.event);
  if (event) selectedEvent.value = event;
}
if (props.modelValue?.mood) {
  //@ts-ignore
  const mood = moodOptions.value.find(m => m.key === props.modelValue!.mood);
  if (mood) selectedMood.value = mood;
}

watch(() => [form.intensity, form.text], ([intensity, text]) => {
  const payload: { event?: string; mood?: string; intensity?: number; text?: string } = {
    event: selectedEvent.value?.key || '',
    mood: selectedMood.value?.key || '',
    intensity: Number(intensity), // 转换为 number
    text : form.text.trim()
  }
  emit('update:modelValue', payload)
})

function selectEvent(event: EventOption) {
  selectedEvent.value = event
  const payload: { event?: string; mood?: string; intensity?: number; text?: string } = {
    event: event.key,
    mood: selectedMood.value?.key || '',
    intensity: form.intensity,
    text: form.text
  }
  emit('update:modelValue', payload)
}

function selectMood(mood: MoodOption) {
  selectedMood.value = mood
  const payload: { event?: string; mood?: string; intensity?: number; text?: string } = {
    event: selectedEvent.value?.key || '',
    mood: mood.key,
    intensity: form.intensity,
    text: form.text
  }
  emit('update:modelValue', payload)
}

function goBack() {
  if (selectedMood.value) {
    selectedMood.value = null
  } else if (selectedEvent.value) {
    selectedEvent.value = null
  }
}

function formatTooltip(val: number) {
  return `${(val * 100).toFixed(0)}%`
}

const formRef = ref<FormInstance>()
function onSubmit() {
  if (!selectedEvent.value || !selectedMood.value) {
    ElMessage.warning('请完成所有选择～')
    return
  }
  const payload = {
    event: selectedEvent.value.key,
    mood: selectedMood.value.key,
    intensity: form.intensity,
    text: form.text.trim()
  }
  emit('submit', payload)
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

.mood-item.selected {
  background: rgba(255, 204, 128, 0.3);
  border: 2px solid #ffcc80;
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

.back-btn {
  background: transparent;
  color: #8d6e63;
  border: 1px solid #ffcc80;
  margin-bottom: 12px;
}

.back-btn:hover {
  background: rgba(255, 204, 128, 0.2);
  color: #5d4037;
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