import { ref, reactive, computed } from 'vue'
import type { ComputedRef, Ref } from 'vue'

interface AnimationState {
    x: number
    y: number
    scale: number
    opacity: number
}

interface AnimationConfig {
    scale?: number
    duration?: number
    parabolaHeight?: number
}

interface ParabolaAnimationReturn {
    style: ComputedRef<{
        transform: string
        opacity: number
        transition?: string
    }>
    animateHide: (element: HTMLElement, targetElement: HTMLElement, onComplete?: () => void) => void
    animateShow: (element: HTMLElement, onComplete?: () => void) => void
    isAnimating: Ref<boolean>
}

export default function useParabolaAnimation(options: AnimationConfig = {}): ParabolaAnimationReturn {
    // 默认配置
    const config = {
        scale: 0.2,
        duration: 600,
        parabolaHeight: 100,
        ...options
    }

    // 动画状态
    const isAnimating = ref(false)
    const progress = ref(0)
    const animationType = ref<'show' | 'hide'>('show') // 添加动画类型标识
    const animState = reactive<{
        start: AnimationState
        target: AnimationState
    }>({
        start: { x: 0, y: 0, scale: 1, opacity: 1 },
        target: { x: 0, y: 0, scale: config.scale, opacity: 0 }
    })

    // 计算动画样式
    const style = computed(() => {
        if (!isAnimating.value) {
            // 根据动画类型返回最终状态
            if (animationType.value === 'show') {
                return {
                    transform: 'translate(0, 0) scale(1)',
                    opacity: 1
                }
            } else {
                return {
                    transform: `translate(${animState.target.x}px, ${animState.target.y}px) scale(${config.scale})`,
                    opacity: 0
                }
            }
        }

        const currentX = animState.start.x + (animState.target.x - animState.start.x) * progress.value
        const currentY = animState.start.y + (animState.target.y - animState.start.y) * progress.value
        const currentScale = animState.start.scale + (animState.target.scale - animState.start.scale) * progress.value
        const currentOpacity = animState.start.opacity + (animState.target.opacity - animState.start.opacity) * progress.value

        // 修复抛物线效果判断条件
        const isHiding = animationType.value === 'hide'
        const parabolaY = isHiding ? -config.parabolaHeight * Math.sin(progress.value * Math.PI) : 0

        return {
            transform: `translate(${currentX}px, ${currentY + parabolaY}px) scale(${currentScale})`,
            opacity: currentOpacity,
            transition: `transform ${config.duration}ms cubic-bezier(0.25, 0.8, 0.25, 1), opacity ${config.duration}ms ease-out`
        }
    })

    // 隐藏动画（抛物线缩小）
    const animateHide = (
        element: HTMLElement,
        targetElement: HTMLElement,
        onComplete?: () => void
    ): void => {
        if (isAnimating.value) {
            console.log('Animation already in progress, cannot start new hide animation')
            return
        }

        animationType.value = 'hide'

        const elementRect = element.getBoundingClientRect()
        const targetRect = targetElement.getBoundingClientRect()

        const elementCenterX = elementRect.left + elementRect.width / 2
        const elementCenterY = elementRect.top + elementRect.height / 2

        const targetCenterX = targetRect.left + targetRect.width / 2
        const targetCenterY = targetRect.top + targetRect.height / 2

        // 设置动画参数
        animState.start.x = 0
        animState.start.y = 0
        animState.start.scale = 1
        animState.start.opacity = 1

        animState.target.x = targetCenterX - elementCenterX
        animState.target.y = targetCenterY - elementCenterY
        animState.target.scale = config.scale
        animState.target.opacity = 0

        // 开始动画
        startAnimation(onComplete)
    }

    // 显示动画（平滑淡入）
    const animateShow = (
        element: HTMLElement,
        onComplete?: () => void
    ): void => {
        if (isAnimating.value) {
            console.log('Animation already in progress, cannot start new show animation')
            return
        }

        animationType.value = 'show'

        // 设置动画参数（从完全透明到完全显示）
        animState.start.x = 0
        animState.start.y = 0
        animState.start.scale = 0.8 // 从稍微缩小开始
        animState.start.opacity = 0

        animState.target.x = 0
        animState.target.y = 0
        animState.target.scale = 1
        animState.target.opacity = 1

        // 开始动画
        startAnimation(onComplete)
    }

    const startAnimation = (onComplete?: () => void) => {
        isAnimating.value = true
        progress.value = 0

        const startTime = performance.now()

        const frame = (currentTime: number): void => {
            const elapsed = currentTime - startTime
            progress.value = Math.min(elapsed / config.duration, 1)

            if (progress.value < 1) {
                requestAnimationFrame(frame)
            } else {
                // 动画完成
                isAnimating.value = false
                progress.value = 1
                onComplete?.()
            }
        }

        requestAnimationFrame(frame)
    }

    return {
        style,
        animateHide,
        animateShow,
        isAnimating
    }
}