<template>
  <div>
    <div class="tip-popup" :class="{ show: isCopy }">
      <div class="tip-content">已经复制找回码</div>
    </div>
    
    <teleport to="body">
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
            <button class="deep-sea-action-btn" id="copyBtn" @click="copyToClipboard">复制到剪贴板</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
// @ts-ignore
import useParabolaAnimation from '/src/composables/useParabolaAnimation'
import html2canvas from 'html2canvas'
// @ts-ignore
import DeviceApiService  from '../api/apiService'

interface Props {
  modelValue?: boolean
  buttonRef?: HTMLElement | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  buttonRef: null
})

// 数据状态
const pageTitle = ref('跨设备登录模式')
const remainingAttempts = ref(3)
const recoveryCode = ref('xxxxxxx')
const showCodeSection = ref(false)
const isCopy = ref(false)

// 动画相关
const containerRef = ref<HTMLElement | null>(null)
const shouldShowContainer = ref(false)

// 使用抛物线动画hook - 修改配置
const { style: animationStyle, animateHide, animateShow, isAnimating } = useParabolaAnimation({
  scale: 0.5, 
  duration: 500, 
  parabolaHeight: 50 
})

// 监听动画状态变化
watch(isAnimating, (newVal) => {
  console.log('Animation state changed:', newVal)
})

// 显示组件
const show = async () => {
  if (isAnimating.value || !containerRef.value) return
  
  shouldShowContainer.value = true
  await nextTick()
  console.log('Showing container:', containerRef.value)
  
  animateShow(containerRef.value)
}

// 隐藏组件 - 修复动画问题
const hide = async () => {
  console.log('Hide called, isAnimating:', isAnimating.value)

  if (isAnimating.value) {
    console.log('Animation in progress, waiting for completion')
    // 使用 setTimeout 等待动画完成
    setTimeout(() => {
      if (!isAnimating.value) {
        hide()
      } else {
        // 如果还在动画，再次等待
        setTimeout(() => hide(), 100)
      }
    }, 100)
    return
  }
  
  if (!containerRef.value) {
    console.log('No container ref, closing directly')
    shouldShowContainer.value = false
    return
  }
  
  console.log('Hiding container, button ref:', props.buttonRef)
  
  // 检查按钮引用是否有效
  if (props.buttonRef && props.buttonRef instanceof HTMLElement) {
    try {
      console.log('Using parabola animation')
      // 如果有按钮引用，使用抛物线动画
      animateHide(containerRef.value, props.buttonRef, () => {
        console.log('Parabola animation completed')
        shouldShowContainer.value = false
      })
    } catch (error) {
      console.error('抛物线动画错误:', error)
      // 如果抛物线动画失败，使用简单的淡出效果
     
    }
  } else {
    console.log('Button ref not valid, using simple animation')
    // 如果没有按钮引用，使用简单的淡出效果

  }
}

// 监听visible变化 - 添加调试
watch(() => props.modelValue, (newVal) => {
  console.log('Visible changed:', newVal)
  if (newVal) {
    show()
  } else {
    hide()
  }
})

// 添加点击容器外部关闭的功能
const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && 
      !containerRef.value.contains(event.target as Node) && 
      shouldShowContainer.value) {
    // 这里可以添加关闭逻辑
  }
}

// 添加全局点击事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function generateCode() {
  // 生成找回码逻辑
  if (remainingAttempts.value > 0) {
    const { code ,expiresAt} =await DeviceApiService.registerDevice();
    showCodeSection.value = true
    recoveryCode.value = code
    
    try{

      console.log('注册设备成功', code, expiresAt);
    }catch   (error) {
      console.log(error);
    }
      

    remainingAttempts.value--
  }
}


const takeScreenshot = async () => {
  try {
    const element = containerRef.value;
    
    if (!element) {
      alert('找不到要截图的内容');
      return;
    }

    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: null
    });

    const link = document.createElement('a');
    link.download = '找回码截图.png';
    link.href = canvas.toDataURL('image/png');
    link.click();
    
  } catch (error) {
    console.error('截图失败:', error);
    alert('截图失败，请重试');
  }
};

const copyToClipboard = async () => {
  if (!recoveryCode.value || recoveryCode.value === 'xxxxxxx') {
    alert('请先生成有效的找回码')
    return
  }
  try {
    await navigator.clipboard.writeText(recoveryCode.value)
    isCopy.value = true
    setTimeout(() => {
      isCopy.value = false
    }, 1500)
  } catch (error) {
    console.error('复制失败', error)
  }
};

// 暴露方法给父组件
defineExpose({
  show,
  hide
})
</script>

<style scoped>
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

.tip-content{
  color: rgb(234, 234, 234);
  font-weight: 500;
  text-align: center;
}

.container {
  position: fixed;
  left: 0;
  right: 0;
  top: 25%; /* 改为居中定位 */
  transform: translateY(-50%); 
  width: 400px;
  height: auto; 
  margin: auto;
  background: linear-gradient(135deg, #0d3b66 0%, #1a5f9c 100%);
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  transform-origin: center;
  will-change: transform, opacity; /* 添加 opacity */
  z-index: 1999;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: opacity 0.3s ease, transform 0.3s ease;
  max-height: 80vh; /* 限制最大高度 */
  overflow-y: auto; /* 添加滚动条以防内容过多 */
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