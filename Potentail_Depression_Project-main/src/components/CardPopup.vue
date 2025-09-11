<template>
  <transition name="card-zoom-rotate">
    <div
      v-if="visible"
      class="overlay"
      @click.self="close"
      role="dialog"
      aria-modal="true"
    >
      <div class="card-container">
        <el-tooltip placement="bottom" content="">
          <el-button
            class="close-btn el-icon-btn"
            circle
            @click="close"
            :aria-label="'关闭'"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip placement="right" :content="isFlipped ? '查看封面' : '记录心情'">
          <el-button
            class="flip-btn el-icon-btn"
            circle
            @click="toggleFlip"
          >
            <el-icon v-if="!isFlipped"><Collection /></el-icon>
            <el-icon v-else><Memo /></el-icon>
          </el-button>
        </el-tooltip>

        <div class="flip-wrapper" :class="{ 'flipped': isFlipped }">
          <div class="flip-face front">
            <CardFront
              :cover="cover"
              :cover-alt="coverAlt"
              :title="title"
              :subtitle="subtitle"
              :description="description"
              :primary-action-text="primaryActionText"
              @primary="$emit('primary')"
              @close="close"
            />
          </div>
          <div class="flip-face back">
            <CardBack
              v-model="moodState"
              :submitting="submitting"
              @close="close"
            />
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import CardFront from './InnerCard/CardFront.vue'
import CardBack from './InnerCard/CardBack.vue'
import { ElButton, ElTooltip } from 'element-plus'
import { Close, Memo, Collection } from '@element-plus/icons-vue'

// 更新 MoodState 接口以支持多选
interface MoodState {
  events?: string[]
  moods?: string[]
  intensity?: number
  text?: string
}

interface Props {
  cover?: string
  coverAlt?: string
  title?: string
  subtitle?: string
  description?: string
  primaryActionText?: string
  width?: string | number
  maxWidth?: string | number
  theme?: 'light' | 'dark' | 'calm'
  modelValue?: boolean
  submitting?: boolean
  defaultMood?: MoodState
}

const props = withDefaults(defineProps<Props>(), {
  cover: '',
  coverAlt: '',
  title: '',
  subtitle: '',
  description: '',
  primaryActionText: '',
  width: '92vw',
  maxWidth: '440px',
  theme: 'calm',
  modelValue: false,
  submitting: false,
  defaultMood: () => ({ events: [], moods: [], intensity: 0.5, text: '' })
})

// 更新 emit 定义以支持多选
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'open'): void
  (e: 'close'): void
  (e: 'primary'): void
  (e: 'submit-mood', payload: { events: string[]; moods: string[]; intensity: number; text: string }): void
}>()

const innerVisible = ref<boolean>(props.modelValue)
watch(
  () => props.modelValue,
  v => (innerVisible.value = v)
)
watch(innerVisible, (v) => {
  if (v) emit('open')
})

const visible = computed({
  get: () => innerVisible.value,
  set: v => {
    innerVisible.value = v
    emit('update:modelValue', v)
  }
})

function close() {
  visible.value = false
  emit('close')
}

const isFlipped = ref(false)
function toggleFlip() {
  isFlipped.value = !isFlipped.value
}

const moodState = ref<MoodState>({ ...props.defaultMood })

// 更新 onSubmitMood 函数以处理多选数据
function onSubmitMood(payload: { events: string[]; moods: string[]; intensity: number; text: string }) {
  emit('submit-mood', payload)
}

const submitting = computed(() => props.submitting)
</script>

<style scoped>
/* 样式保持不变 */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(11, 19, 32, 0.5);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 23000;
}

.card-container {
  width: v-bind(width);
  max-width: v-bind(maxWidth);
  position: relative;
  border-radius: 30px;
  perspective: 1200px;
  top: -6%;
}

.close-btn {
  position: absolute;
  top: -12px;
  right: -12px;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  background: rgba(28, 33, 39, 0.85);
  color: #f1f5f9;
  cursor: pointer;
  line-height: 32px;
  font-size: 18px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.28);
  z-index: 1;
}

.flip-btn {
  position: absolute;
  top: 50%;
  left: -30px;
  width: 40px;
  height: 40px;
  border: 1px solid #d9c2a5;
  border-radius: 50%;
  background: #f5e8c7;
  color: #4a3c2f;
  cursor: pointer;
  transform: translateY(-50%) rotate(0deg);
  overflow: hidden;
  clip-path: polygon(0 0, 50% 0, 100% 50%, 50% 100%, 0 100%);
  transition: transform 0.5s;
  z-index: 1;
}

.flip-wrapper {
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.5s;
}
.flip-wrapper.flipped {
  transform: rotateY(180deg);
}
.flip-face {
  backface-visibility: hidden;
}
.front, .back {
  position: relative;
}
.back {
  transform: rotateY(180deg);
}

.card-zoom-rotate-enter-from .card-container {
  transform: scale(0.86) rotate(-3deg) translateY(10px);
  opacity: 0;
}
.card-zoom-rotate-enter-active {
  transition: transform 0.36s cubic-bezier(0.2, 0.8, 0.2, 1), opacity 0.36s ease;
}
.card-zoom-rotate-enter-to .card-container {
  transform: scale(1) rotate(0) translateY(0);
  opacity: 1;
}
.card-zoom-rotate-leave-active {
  transition: transform 0.22s ease, opacity 0.22s ease;
}
.card-zoom-rotate-leave-to .card-container {
  transform: scale(0.96) rotate(2deg) translateY(8px);
  opacity: 0;
}
</style>