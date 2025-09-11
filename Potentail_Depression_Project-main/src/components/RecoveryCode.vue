```vue
<template>
  <div>
    <teleport to="body">
      <!-- 使用 ElOverlay 替换自定义遮罩层 -->
      <el-overlay
        v-show="shouldShowContainer"
        class="mask-layer"
        :class="{ 'mask-layer': true }"
        @click="handleMaskClick"
      />

      <!-- 容器保持不变 -->
      <div
        v-show="shouldShowContainer"
        ref="containerRef"
        class="container animated-container deep-sea-container"
        :style="animationStyle"
      >
        <h1 id="title">{{ pageTitle }}</h1>
        <div class="recovery-section">
          <p>生成找回码（剩余<span id="attempts">{{ remainingAttempts }}</span>次）</p>
          <button id="generateBtn" @click="generateCode" class="deep-sea-action-btn">发送</button>
        </div>
        <div id="codeSection" v-show="showCodeSection" class="deep-sea-code-section">
          <div class="code-display">
            <p id="recoveryCode">{{ recoveryCode }}</p>
          </div>
          <div class="action-buttons">
            <button class="deep-sea-action-btn" id="screenshotBtn" @click="takeScreenshot">截图保存</button>
            <button class="deep-sea-action-btn" id="copyBtn" @click="copyAndShowTip">复制到剪贴板</button>
          </div>
        </div>
      </div>
    </teleport>
    <messageTip
      :message="tipMessage"
      :trigger="showTrigger"
      :background-color="tipBackgroundColor"
      :text-color="tipTextColor"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue';
import { ElOverlay } from 'element-plus';
// @ts-ignore
import useParabolaAnimation from '/src/composables/useParabolaAnimation';
import html2canvas from 'html2canvas';
// @ts-ignore
import DeviceApiService from '../api/apiService';
import messageTip from './tip/messageTip.vue';


interface Props {
  modelValue?: boolean;
  buttonRef?: HTMLElement | null;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  buttonRef: null,
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

// 数据状态
const pageTitle = ref('跨设备登录模式');
const remainingAttempts = ref(3);
const recoveryCode = ref('xxxxxxx');
const showCodeSection = ref(false);

// 动画相关
const containerRef = ref<HTMLElement | null>(null);
const shouldShowContainer = ref(false);

const tipMessage = ref('复制成功！');
const showTrigger = ref(false);
const tipBackgroundColor = ref('rgba(88, 172, 209, 0.9)');
const tipTextColor = ref('#ffffff');

const { style: animationStyle, animateHide, animateShow, isAnimating } = useParabolaAnimation({
  scale: 0.5,
  duration: 500,
  parabolaHeight: 50,
});

// 监听动画状态变化
watch(isAnimating, (newVal) => {
  console.log('Animation state changed:', newVal);
});

// 显示组件
const show = async () => {
  if (isAnimating.value || !containerRef.value) return;

  shouldShowContainer.value = true;
  await nextTick();
  console.log('Showing container:', containerRef.value);

  animateShow(containerRef.value);
};

// 隐藏组件
const hide = async () => {
  console.log('Hide called, isAnimating:', isAnimating.value);

  if (isAnimating.value) {
    console.log('Animation in progress, waiting for completion');
    setTimeout(() => {
      if (!isAnimating.value) {
        hide();
      } else {
        setTimeout(() => hide(), 100);
      }
    }, 100);
    return;
  }

  if (!containerRef.value) {
    console.log('No container ref, closing directly');
    shouldShowContainer.value = false;
    emit('update:modelValue', false); // 通知父组件更新状态
    return;
  }

  console.log('Hiding container, button ref:', props.buttonRef);

  if (props.buttonRef && props.buttonRef instanceof HTMLElement) {
    try {
      console.log('Using parabola animation');
      animateHide(containerRef.value, props.buttonRef, () => {
        console.log('Parabola animation completed');
        shouldShowContainer.value = false;
        emit('update:modelValue', false); // 通知父组件更新状态
      });
    } catch (error) {
      console.error('抛物线动画错误:', error);
      shouldShowContainer.value = false;
      emit('update:modelValue', false); // 通知父组件更新状态
    }
  } else {
    console.log('Button ref not valid, using simple animation');
    shouldShowContainer.value = false;
    emit('update:modelValue', false); // 通知父组件更新状态
  }
};

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  console.log('Visible changed:', newVal);
  if (newVal) {
    show();
  } else {
    hide();
  }
});

// 处理遮罩层点击
const handleMaskClick = (event: MouseEvent) => {
  event.stopPropagation();
  hide();
  emit('update:modelValue', false); // 通知父组件更新状态
};

// 添加点击容器外部关闭的功能
const handleClickOutside = (event: MouseEvent) => {
  if (
    containerRef.value &&
    !containerRef.value.contains(event.target as Node) &&
    shouldShowContainer.value &&
    !(event.target as HTMLElement).closest('.deep-sea-btn')
  ) {
    hide();
    emit('update:modelValue', false); // 通知父组件更新状态
  }
};

// 添加全局点击事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

async function generateCode() {
  if (remainingAttempts.value > 0) {
    const { code, expiresAt } = await DeviceApiService.registerDevice();
    showCodeSection.value = true;
    recoveryCode.value = code;

    try {
      console.log('注册设备成功', code, expiresAt);
    } catch (error) {
      console.log(error);
    }

    remainingAttempts.value--;
  }
}

const takeScreenshot = async () => {
  try {
    const element = containerRef.value;

    if (!element) {
      tipBackgroundColor.value = 'rgba(220, 53, 69, 0.9)';
      tipTextColor.value = '#ffffff';
      showTrigger.value = true;
      setTimeout(() => {
        showTrigger.value = false;
      }, 50);
      return;
    }

    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: null,
    });

    const link = document.createElement('a');
    link.download = '找回码截图.png';
    link.href = canvas.toDataURL('image/png');
    link.click();
    tipMessage.value = '截图保存成功！';
    tipBackgroundColor.value = 'rgba(88, 172, 209, 0.9)';
    tipTextColor.value = '#ffffff';
    showTrigger.value = true;
    setTimeout(() => {
      showTrigger.value = false;
    }, 50);
  } catch (error) {
    console.error('截图失败:', error);
    tipMessage.value = '截图失败';
    tipBackgroundColor.value = 'rgba(220, 53, 69, 0.9)';
    tipTextColor.value = '#ffffff';
    showTrigger.value = true;
    setTimeout(() => {
      showTrigger.value = false;
    }, 50);
  }
};

const copyAndShowTip = async () => {
  try {
    await navigator.clipboard.writeText(recoveryCode.value);
    tipMessage.value = '复制成功！';
    tipBackgroundColor.value = 'rgba(88, 172, 209, 0.9)';
    tipTextColor.value = '#ffffff';
    showTrigger.value = true;
    setTimeout(() => {
      showTrigger.value = false;
    }, 50);
  } catch (error) {
    console.error('复制失败', error);
    tipMessage.value = '复制失败';
    tipBackgroundColor.value = 'rgba(220, 53, 69, 0.9)';
    tipTextColor.value = '#ffffff';
    showTrigger.value = true;
    setTimeout(() => {
      showTrigger.value = false;
    }, 50);
  }
};

defineExpose({
  show,
  hide,
});
</script>

<style scoped>
/* 调整 ElOverlay 的样式以匹配原有的 mask-layer */
.mask-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 1998; /* 比容器低一级 */
  transition: opacity 0.3s ease;
}

/* 以下样式保持不变 */
.tip-popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 1rem 2rem;
  transition: all 0.3s ease-out;
  background: rgba(62, 62, 62, 0.9);
  opacity: 0;
  border-radius: 12px;
  pointer-events: none;
}

.tip-popup.show {
  opacity: 1;
}

.tip-content {
  color: rgb(234, 234, 234);
  font-weight: 500;
  text-align: center;
}

.container {
  position: fixed;
  left: 0;
  right: 0;
  top: 25%;
  transform: translateY(-50%);
  width: 400px;
  height: auto;
  margin: auto;
  background: linear-gradient(135deg, #0d3b66 0%, #1a5f9c 100%);
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  transform-origin: center;
  will-change: transform, opacity;
  z-index: 1999;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: opacity 0.3s ease, transform 0.3s ease;
  max-height: 80vh;
  overflow-y: auto;
}

#title {
  color: #e0f2fe;
  text-align: center;
  margin-bottom: 20px;
  font-weight: 300;
  letter-spacing: 1px;
}

.recovery-section {
  color: #b3e0ff;
  text-align: center;
  margin-bottom: 25px;
}

.recovery-section p {
  margin-bottom: 15px;
}

.deep-sea-action-btn {
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
  min-width: 120px;
}

.deep-sea-action-btn:hover {
  background: linear-gradient(135deg, #0d3b66 0%, #1a5f9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.deep-sea-code-section {
  background: rgba(23, 42, 69, 0.7);
  border-radius: 8px;
  padding: 15px;
  margin-top: 20px;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.code-display {
  background: rgba(10, 25, 47, 0.5);
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
}

#recoveryCode {
  color: #64ffda;
  font-family: monospace;
  font-size: 18px;
  letter-spacing: 2px;
  text-align: center;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .container {
    width: 90%;
    padding: 20px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .deep-sea-action-btn {
    width: 100%;
    margin: 5px 0;
  }
}
</style>
```