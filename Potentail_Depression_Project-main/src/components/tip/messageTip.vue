<template>
  <teleport to="body">
    <div
      v-if="show"
      class="tip-popup"
      :class="{ 'show': isVisible }"
      :style="[animationStyle, { '--bg-color': props.backgroundColor, '--text-color': props.textColor }]"
    >
      <div class="tip-content">
        {{ message }}
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  message: string
  trigger: boolean
  backgroundColor?: string 
  textColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  backgroundColor: 'rgba(62, 62, 62, 0.9)', // 默认背景色
  textColor: 'rgb(234, 234, 234)' // 默认文字颜色
})

const show = ref(false)
const isVisible = ref(false)

// 动画样式
const animationStyle = computed(() => ({
  opacity: isVisible.value ? 1 : 0,
  transform: isVisible.value ? 'translate(-50%, -50%) scale(1)' : 'translate(-50%, -50%) scale(0.8)'
}))

// 显示提示
const showTip = () => {
  show.value = true
  setTimeout(() => {
    isVisible.value = true
  }, 10)

  // 1秒后自动隐藏
  setTimeout(() => {
    hideTip()
  }, 1000)
}

// 隐藏提示
const hideTip = () => {
  isVisible.value = false
  // 等待动画完成
  setTimeout(() => {
    show.value = false
  }, 300) // 与CSS过渡时间一致
}

// 监听trigger变化
watch(() => props.trigger, (newVal) => {
  if (newVal) {
    showTip()
  }
})

// 暴露方法给父组件
defineExpose({
  showTip,
  hideTip
})
</script>

<style scoped>
.tip-popup {
  position: fixed;
  opacity: .8;
  top: 20%;
  left: 50%;
  padding: 1rem 2rem;
  background: var(--bg-color); /* 使用 CSS 变量动态设置背景色 */
  border-radius: 12px;
  z-index: 2000;
  transition: all 0.3s ease-out;
  pointer-events: none;
}

.tip-content {
  color: var(--text-color); 
  font-weight: 500;
  text-align: center;
  font-size: 14px;
}
</style>