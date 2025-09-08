<!-- ThreeCardCarousel.vue -->
<template>
  <div class="three-card-carousel-container" ref="containerRef">
    <div class="three-card-carousel" ref="carouselRef" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch, nextTick, computed } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
// src\assets\image\anon-card.jpg
import todayCardImage from '../assets/image/today-card.jpg'
import anonCardImage from '../assets/image/anon-card.jpg'
import progressCardImage from '../assets/image/progress-card.jpg'

type Card = {
  id: string | number
  title: string
  imageUrl?: string
}

const props = withDefaults(defineProps<{
  cards: Card[]
  width?: number | string
  height?: number | string
  cardWidth?: number
  cardHeight?: number
  spacing?: number
  radius?: number
  enableControls?: boolean
  initialIndex?: number
  responsive?: boolean // 新增：是否启用响应式
  breakpoints?: { // 新增：断点配置
    sm?: number
    md?: number
    lg?: number
    xl?: number
  }
}>(), {
  width: '100%',
  height: '100%',
  cardWidth: 1.8,
  cardHeight: 2.6,
  spacing: 0.35,
  radius: 3.2,
  enableControls: false,
  initialIndex: 0,
  responsive: true,
  breakpoints: () => ({
    sm: 640,    // 小屏幕
    md: 768,    // 中等屏幕
    lg: 1024,   // 大屏幕
    xl: 1280    // 超大屏幕
  })
})

const emit = defineEmits<{
  (e: 'select', card: Card, index: number): void
  (e: 'resize', size: { width: number; height: number }): void
}>()

const containerRef = ref<HTMLDivElement | null>(null)
const carouselRef = ref<HTMLDivElement | null>(null)
const isInitialized = ref(false)

// 响应式尺寸
const containerSize = ref({ width: 0, height: 0 })
const screenSize = ref({ width: window.innerWidth, height: window.innerHeight })

// 计算响应式参数
const responsiveParams = computed(() => {
  if (!props.responsive) {
    return {
      cardWidth: props.cardWidth,
      cardHeight: props.cardHeight,
      radius: props.radius,
      spacing: props.spacing
    }
  }

  const { sm, md, lg, xl } = props.breakpoints
  const width = screenSize.value.width

  if (width < sm!) {
    // 超小屏幕
    return {
      cardWidth: props.cardWidth * 0.7 * 0.7,
      cardHeight: props.cardHeight * 0.7 * 0.7,
      radius: props.radius * 0.8 * 0.7,
      spacing: props.spacing * 0.8 * 0.7
    }
  } else if (width < md!) {
    // 小屏幕
    return {
      cardWidth: props.cardWidth * 0.8,
      cardHeight: props.cardHeight * 0.8,
      radius: props.radius * 0.9,
      spacing: props.spacing * 0.9
    }
  } else if (width < lg!) {
    // 中等屏幕
    return {
      cardWidth: props.cardWidth,
      cardHeight: props.cardHeight,
      radius: props.radius,
      spacing: props.spacing
    }
  } else {
    // 大屏幕
    return {
      cardWidth: props.cardWidth * 1.1,
      cardHeight: props.cardHeight * 1.1,
      radius: props.radius * 1.1,
      spacing: props.spacing * 1.1
    }
  }
})

// three.js 基础对象
let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let controls: OrbitControls | null = null
let raycaster: THREE.Raycaster | null = null
let mouse: THREE.Vector2 | null = null
let frameId: number | null = null

// 卡片网格及其数据映射
let cardMeshes: THREE.Mesh[] = []
let cardTargets: { position: THREE.Vector3; rotationY: number }[] = []
let selectedIndex = props.initialIndex

// 纹理加载器
const textureLoader = new THREE.TextureLoader()
// 存储已加载的纹理
const loadedTextures = new Map<string, THREE.Texture>()

// 预定义的图片映射
const cardImageMap: Record<string, string> = {
  'today': todayCardImage,
  'anon': anonCardImage,
  'progress': progressCardImage
}

// 监听窗口大小变化
const handleWindowResize = () => {
  screenSize.value = {
    width: window.innerWidth,
    height: window.innerHeight
  }
  updateContainerSize()
}

// 更新容器尺寸
const updateContainerSize = () => {
  if (!containerRef.value) return
  
  const computedStyle = getComputedStyle(containerRef.value)
  const paddingX = parseFloat(computedStyle.paddingLeft) + parseFloat(computedStyle.paddingRight)
  const paddingY = parseFloat(computedStyle.paddingTop) + parseFloat(computedStyle.paddingBottom)
  
  const newWidth = Math.max(100, containerRef.value.clientWidth - paddingX)
  const newHeight = Math.max(100, containerRef.value.clientHeight - paddingY)
  
  if (newWidth !== containerSize.value.width || newHeight !== containerSize.value.height) {
    containerSize.value = { width: newWidth, height: newHeight }
    emit('resize', containerSize.value)
    handleResize()
  }
}

// 创建观察器监听容器大小变化
let resizeObserver: ResizeObserver | null = null

function createRenderer(target: HTMLElement) {
  if (renderer) {
    renderer.dispose()
  }
  
  renderer = new THREE.WebGLRenderer({ 
    antialias: true, 
    alpha: true,
    powerPreference: "high-performance"
  })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(containerSize.value.width, containerSize.value.height, false)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  target.appendChild(renderer.domElement)
}

function getAdaptiveRadius(count: number, angleStep: number) {
  const minArc = responsiveParams.value.cardWidth + responsiveParams.value.spacing
  const rMin = minArc / angleStep
  return Math.max(responsiveParams.value.radius, rMin)
}

function createSceneCamera() {
  scene = new THREE.Scene()
  
  // 灯光设置
  const ambientLight = new THREE.AmbientLight(0xccccff, 0.6)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xfffaf0, 0.8)
  directionalLight.position.set(2, 3, 4)
  scene.add(directionalLight)

  const fillLight = new THREE.DirectionalLight(0xe6e8ff, 0.4)
  fillLight.position.set(-2, -1, -3)
  scene.add(fillLight)

  const pointLight = new THREE.PointLight(0xfff0e0, 0.3, 10)
  pointLight.position.set(0, 2, 5)
  scene.add(pointLight)

  camera = new THREE.PerspectiveCamera(
    35,
    containerSize.value.width / containerSize.value.height,
    0.1,
    100
  )
  camera.position.set(0, 0.2, 8)
  scene.add(camera)
}

function createControls() {
  if (!renderer || !camera) return
  if (controls) {
    controls.dispose()
  }
  
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.enabled = !!props.enableControls
  controls.enablePan = false
  controls.minDistance = 4
  controls.maxDistance = 10
  controls.minPolarAngle = Math.PI / 2.6
  controls.maxPolarAngle = Math.PI / 1.6
}

// 加载图片纹理
async function loadCardTexture(cardId: string, imageUrl: string): Promise<THREE.Texture> {
  return new Promise((resolve, reject) => {
    if (loadedTextures.has(cardId)) {
      resolve(loadedTextures.get(cardId)!)
      return
    }

    textureLoader.load(
      imageUrl,
      (texture) => {
        texture.colorSpace = THREE.SRGBColorSpace
        texture.anisotropy = 16
        loadedTextures.set(cardId, texture)
        resolve(texture)
      },
      undefined,
      (error) => {
        console.error('Failed to load texture:', error)
        reject(error)
      }
    )
  })
}

// 创建卡片材质
async function createCardMaterial(card: Card): Promise<THREE.Material> {
  try {
    const imageUrl = card.imageUrl || cardImageMap[card.id] || ''
    
    if (!imageUrl) {
      throw new Error(`No image found for card ${card.id}`)
    }

    const texture = await loadCardTexture(card.id.toString(), imageUrl)
    
    return new THREE.MeshPhysicalMaterial({
      map: texture,
      transparent: true,
      roughness: 0.4,
      metalness: 0.1,
      reflectivity: 0.2,
      clearcoat: 0.3,
      clearcoatRoughness: 0.1,
      side: THREE.FrontSide,
      thickness: 0.1,
      transmission: 0.05,
    })
  } catch (error) {
    console.error('Error creating card material:', error)
    return new THREE.MeshBasicMaterial({
      color: 0x8888ff,
      transparent: true,
      opacity: 0.8
    })
  }
}

// 创建卡片
async function createCards() {
  if (!scene) return
  
  // 清理旧卡片
  cardMeshes.forEach(m => {
    scene!.remove(m)
    m.geometry.dispose()
    Array.isArray(m.material)
      ? m.material.forEach(mat => mat.dispose?.())
      : (m.material as THREE.Material).dispose?.()
  })
  cardMeshes = []
  cardTargets = []

  const geometry = new THREE.PlaneGeometry(
    responsiveParams.value.cardWidth, 
    responsiveParams.value.cardHeight
  )
  const count = Math.max(1, props.cards.length)

  // 均分角度
  const angleStep = (Math.PI * 2) / count
  const radius = getAdaptiveRadius(count, angleStep)

  // 创建所有卡片的材质
  const materialPromises = props.cards.map(card => createCardMaterial(card))
  const materials = await Promise.all(materialPromises)

  for (let i = 0; i < count; i++) {
    const card = props.cards[i]
    const material = materials[i]

    const mesh = new THREE.Mesh(geometry, material)
    mesh.name = `card-${card.id}`
    mesh.userData = { index: i, card }

    const offset = i - selectedIndex
    const angle = offset * angleStep
    const x = Math.sin(angle) * radius
    const z = Math.cos(angle) * radius
    mesh.position.set(x, 0, z)

    if (camera) mesh.lookAt(camera.position)

    scene!.add(mesh)
    cardMeshes.push(mesh)

    const target = {
      position: new THREE.Vector3(x, 0, z),
      rotationY: mesh.rotation.y
    }
    cardTargets.push(target)
  }

  if(renderer && scene && camera){
    renderer.render(scene, camera)
  }
}

function focusToIndex(index: number, animate = true) {
  selectedIndex = index
  const count = cardMeshes.length
  if (count === 0) return

  const angleStep = (Math.PI * 2) / count
  const radius = getAdaptiveRadius(count, angleStep)

  for (let i = 0; i < count; i++) {
    const offset = i - selectedIndex
    const angle = offset * angleStep
    const x = Math.sin(angle) * radius
    const z = Math.cos(angle) * radius

    const mesh = cardMeshes[i]
    const toPos = new THREE.Vector3(x, 0, z)

    let toRotY = mesh.rotation.y
    if (camera) {
      const dir = new THREE.Vector3().subVectors(camera.position, toPos)
      toRotY = Math.atan2(dir.x, dir.z)
    }

    if (!animate) {
      mesh.position.copy(toPos)
      mesh.rotation.set(0, toRotY, 0)
      continue
    }

    animateTransform(mesh, toPos, toRotY, 420)
  }
}

function animateTransform(
  mesh: THREE.Mesh,
  toPos: THREE.Vector3,
  toRotY: number,
  duration = 600
) {
  const fromPos = mesh.position.clone()
  const fromRotY = mesh.rotation.y
  const start = performance.now()

  function easeOutQuart(t: number): number {
    return 1 - Math.pow(1 - t, 4)
  }

  function tick(now: number) {
    const p = Math.min(1, (now - start) / duration)
    const e = easeOutQuart(p)
    
    mesh.position.lerpVectors(fromPos, toPos, e)
    mesh.rotation.y = fromRotY + (toRotY - fromRotY) * e
    mesh.position.y = Math.sin(p * Math.PI) * 0.1
    
    if (p < 1) {
      requestAnimationFrame(tick)
    } else {
      mesh.position.y = 0
    }
  }
  requestAnimationFrame(tick)
}

function onClick(e: MouseEvent) {
  if (!renderer || !camera || !scene) return
  if (!raycaster || !mouse) return

  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(cardMeshes, false)
  if (intersects.length > 0) {
    const mesh = intersects[0].object as THREE.Mesh
    const idx = mesh.userData.index as number
    focusToIndex(idx, true)
    emit('select', mesh.userData.card as Card, idx)
  }
}

function animate() {
  if (!renderer || !scene || !camera) return
  frameId = requestAnimationFrame(animate)
  controls?.update()
  renderer.render(scene, camera)
}

let resizeTimeout: number | null = null

function handleResize() {
  if (!renderer || !camera || !containerRef.value) return
  
  if (resizeTimeout) {
    clearTimeout(resizeTimeout)
  }
  
  resizeTimeout = setTimeout(() => {
    updateContainerSize()
    
    if (renderer && containerSize.value.width > 0 && containerSize.value.height > 0) {
      renderer.setSize(containerSize.value.width, containerSize.value.height, false)
      camera!.aspect = containerSize.value.width / containerSize.value.height
      camera!.updateProjectionMatrix()
      
      if (controls) {
        controls.update()
      }
      
      // 重新创建卡片以适应新的尺寸
      createCards().then(() => {
        if (renderer && scene && camera) {
          renderer.render(scene, camera)
        }
      })
    }
    
    resizeTimeout = null
  }, 100) as unknown as number
}

onMounted(async () => {
  await nextTick()
  
  if (!containerRef.value) return
  
  // 设置初始尺寸
  updateContainerSize()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleWindowResize)
  
  // 使用 ResizeObserver 监听容器大小变化
  resizeObserver = new ResizeObserver(handleResize)
  resizeObserver.observe(containerRef.value)

  if (carouselRef.value) {
    createRenderer(carouselRef.value)
    createSceneCamera()
    createControls()

    raycaster = new THREE.Raycaster()
    mouse = new THREE.Vector2()

    await createCards()

    if (renderer) {
      renderer.domElement.addEventListener('click', onClick)
    }

    animate()
    isInitialized.value = true
  }
})

onBeforeUnmount(() => {
  // 移除事件监听器
  window.removeEventListener('resize', handleWindowResize)
  
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  
  if (renderer) {
    renderer.domElement.removeEventListener('click', onClick)
  }
  
  if (resizeTimeout) {
    clearTimeout(resizeTimeout)
  }
  
  if (frameId) cancelAnimationFrame(frameId)

  // 释放资源
  cardMeshes.forEach(m => {
    m.geometry.dispose()
    const mat = m.material
    if (Array.isArray(mat)) mat.forEach(mm => mm.dispose?.())
    else (mat as THREE.Material).dispose?.()
  })
  cardMeshes = []
  
  controls?.dispose()
  renderer?.dispose()
  
  if (renderer && renderer.domElement.parentElement) {
    renderer.domElement.parentElement.removeChild(renderer.domElement)
  }
  
  renderer = null
  scene = null
  camera = null
  raycaster = null
  mouse = null
})

// 监听响应式参数变化
watch(responsiveParams, async () => {
  if (isInitialized.value) {
    await createCards()
  }
}, { deep: true })

watch(
  () => props.cards,
  async () => {
    if (isInitialized.value) {
      await createCards()
    }
  },
  { deep: true }
)

watch(
  () => props.initialIndex,
  v => {
    focusToIndex(Math.min(Math.max(0, v), props.cards.length - 1), true)
  }
)
</script>

<style scoped>
.three-card-carousel-container {
  position: fixed;
  top: -20%;
  left: 0;
  bottom: 0;
  right: 0;
  transform: translate(-25%);
  margin: auto;
  width: v-bind('typeof props.width === "number" ? props.width + "px" : props.width');
  height: v-bind('typeof props.height === "number" ? props.height + "px" : props.height');
  min-width: 150px;
  min-height: 200px;
}

.three-card-carousel {
  width: 100%;
  height: 100%;
  background: transparent;
  border-radius: 12px;
}

/* 响应式媒体查询 */
@media (max-width: 640px) {
  .three-card-carousel-container {
    min-width: 150px;
    min-height: 210px;
  }
}

@media (max-width: 480px) {
  .three-card-carousel-container {
    min-width: 120px;
    min-height: 180px;
    
  }
}

@media (min-width: 1024px) {
  .three-card-carousel-container {
    min-width: 240px;
    min-height: 300px;
  }
}

@media (min-width: 1280px) {
  .three-card-carousel-container {
    min-width: 300px;
    min-height: 360px;
  }
}
</style>