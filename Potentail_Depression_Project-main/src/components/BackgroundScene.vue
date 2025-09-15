<template>
  <div id="bubble-container"></div>
  <div id="audio-control">
    <div class="play-icon paused"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { useAudioStore } from '@/stores/audio'

let animationFrameId
let scene, camera, renderer
let bubbles = []
let plankton, swim
let bubbleSounds, manyBubblesSound
let lastBubbleTime = 0
let activeBubbles = 0
const MAX_BUBBLES_FOR_SINGLE = 15
const audioStore = useAudioStore()
const audioElements = ref({})
const bubbleGeometry = new THREE.SphereGeometry(0.2, 32, 32)

function getRandomInterval() { return 3000 + Math.random() * 5000 }
let nextBubbleInterval = getRandomInterval()

function createBubbleMaterial() {
  return new THREE.MeshPhongMaterial({
    color: 0xffffff,
    transparent: true,
    opacity: 0.8,
    specular: 0x111111,
    shininess: 100,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })
}

const mouse = new THREE.Vector2()
const handleClick = (event) => {
  const audioControl = document.getElementById('audio-control')
  if (audioControl && audioControl.contains(event.target)) return
  mouse.x = (event.clientX / window.innerWidth) * 2 - 1
  mouse.y = -(event.clientY / window.innerHeight) * 2 + 1
  const bubble = new THREE.Mesh(bubbleGeometry, createBubbleMaterial())
  bubble.position.set(
    camera.position.x + mouse.x * 5,
    camera.position.y - 5,
    camera.position.z + mouse.y * 5
  )
  bubble.scale.setScalar(0.5 + Math.random() * 0.5)
  bubble.userData = {
    speed: 0.02 + Math.random() * 0.03,
    wobbleSpeed: Math.random() * 0.02,
    wobbleAmount: Math.random() * 0.1
  }
  scene.add(bubble)
  bubbles.push(bubble)
  activeBubbles++

  if (bubbles.length > 80) {
    const old = bubbles.shift()
    scene.remove(old)
    activeBubbles--
  }
  if (activeBubbles < MAX_BUBBLES_FOR_SINGLE) {
    audioStore.playBubbleSound('single')
  } else {
    audioStore.playBubbleSound('many')
  }
}

async function initAudioElements() {
  const audioConfig = [
    { id: 'bg-music', src: '/assets/sea/deep-sea-type-ambient-noise-loop_69bpm_F_major.wav', type: 'audio/wav', loop: true, volume: 0.4 },
    { id: 'bubble-sound1', src: '/assets/bubble/one_bubble_01.mp3', type: 'audio/mp3', volume: 0.6 },
    { id: 'bubble-sound2', src: '/assets/bubble/two_bubble_01.mp3', type: 'audio/mp3', volume: 0.6 },
    { id: 'many-bubbles-sound', src: '/assets/bubble/many_bubble.mp3', type: 'audio/mp3', volume: 0.5 }
  ]

  for (const config of audioConfig) {
    const audio = new Audio()
    audio.id = config.id
    audio.src = config.src
    audio.type = config.type
    audio.volume = config.volume
    audio.loop = config.loop || false
    audio.preload = 'auto'
    
    audioStore.registerAudioElement(config.id, audio)
    audioElements.value[config.id] = audio
  }
}

function setupAudioControls() {
  const bgMusic = audioElements.value['bg-music']
  const audioControl = document.getElementById('audio-control')
  const playIcon = document.querySelector('.play-icon')

  function updateAudioUI(isPlaying) {
    console.log('更新 UI，播放状态:', isPlaying, 'bgMusic.paused:', bgMusic?.paused)
    if (!playIcon || !audioControl) return
    if (isPlaying) {
      playIcon.classList.remove('paused')
      audioControl.title = '点击暂停'
    } else {
      playIcon.classList.add('paused')
      audioControl.title = '点击播放'
    }
  }

  function showLoading() {
    if (!playIcon || !audioControl) return
    playIcon.style.display = 'none'
    const spinner = document.createElement('div')
    spinner.className = 'loading-spinner'
    audioControl.appendChild(spinner)
  }

  function hideLoading() {
    if (!playIcon || !audioControl) return
    const spinner = audioControl.querySelector('.loading-spinner')
    if (spinner) spinner.remove()
    playIcon.style.display = 'block'
  }

  if (bgMusic && audioControl && playIcon) {
    updateAudioUI(false)

    // 移除已有的事件监听器，避免重复绑定
    audioControl.removeEventListener('click', handleAudioControl)
    function handleAudioControl(event) {
      event.stopPropagation() // 防止点击事件冒泡到 document
      console.log('音频控制点击，当前状态:', bgMusic.paused ? '暂停' : '播放', 'currentTime:', bgMusic.currentTime)
      if (bgMusic.paused) {
        showLoading()
        bgMusic.play().then(() => {
          console.log('音乐播放成功')
          updateAudioUI(true)
          hideLoading()
        }).catch(err => {
          console.error('播放失败:', err)
          updateAudioUI(false)
          hideLoading()
        })
      } else {
        console.log('尝试暂停音乐')
        bgMusic.pause()
        bgMusic.currentTime = 0 // 强制重置时间，确保暂停后从头开始
        updateAudioUI(false)
      }
    }

    audioControl.addEventListener('click', handleAudioControl)

    const firstInteraction = () => {
      showLoading()
      bgMusic.play().then(() => {
        console.log('首次交互播放成功')
        updateAudioUI(true)
        hideLoading()
      }).catch(err => {
        console.error('首次交互播放失败:', err)
        updateAudioUI(false)
        hideLoading()
      })
      window.removeEventListener('pointerdown', firstInteraction)
      window.removeEventListener('keydown', firstInteraction)
    }

    window.addEventListener('pointerdown', firstInteraction, { once: true })
    window.addEventListener('keydown', firstInteraction, { once: true })

    bgMusic.addEventListener('play', () => {
      console.log('音乐开始播放')
      updateAudioUI(true)
    })
    bgMusic.addEventListener('pause', () => {
      console.log('音乐暂停')
      updateAudioUI(false)
    })
  } else {
    console.error('音频控制初始化失败:', { bgMusic, audioControl, playIcon })
  }
}

onMounted(() => {
  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x04345a, 0.06)

  camera = new THREE.PerspectiveCamera(
    75,
    window.innerWidth / window.innerHeight,
    0.1,
    1000
  )

  renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    preserveDrawingBuffer: true
  })
  renderer.domElement.style.position = 'fixed'
  renderer.domElement.style.top = '0'
  renderer.domElement.style.left = '0'
  renderer.domElement.style.zIndex = '-1'
  renderer.domElement.style.pointerEvents = 'none'
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputEncoding = THREE.sRGBEncoding
  document.getElementById('bubble-container').appendChild(renderer.domElement)

  const hemiLight = new THREE.HemisphereLight(0x6aa9ff, 0x00152a, 0.9)
  scene.add(hemiLight)
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.55)
  directionalLight.position.set(2, 3, 2)
  scene.add(directionalLight)
  const ambientLight = new THREE.AmbientLight(0x406080, 0.3)
  scene.add(ambientLight)

  const planktonCount = 1400
  const halfRange = 70
  const wrapRange = halfRange * 2
  const planktonGeo = new THREE.BufferGeometry()
  const pPositions = new Float32Array(planktonCount * 3)
  for (let i = 0; i < planktonCount; i++) {
    const i3 = i * 3
    pPositions[i3 + 0] = (Math.random() * 2 - 1) * halfRange
    pPositions[i3 + 1] = (Math.random() * 2 - 1) * halfRange
    pPositions[i3 + 2] = (Math.random() * 2 - 1) * halfRange
  }
  planktonGeo.setAttribute('position', new THREE.BufferAttribute(pPositions, 3))
  planktonGeo.attributes.position.usage = THREE.DynamicDrawUsage
  const planktonMat = new THREE.PointsMaterial({
    color: 0x98d7ff,
    size: 0.075,
    sizeAttenuation: true,
    transparent: true,
    opacity: 0.65,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  })
  plankton = new THREE.Points(planktonGeo, planktonMat)
  scene.add(plankton)

  swim = {
    pos: new THREE.Vector3(0, -2, 0),
    vel: new THREE.Vector3(0, 0.01, 0.03),
    speed: 0.035,
    targetDir: new THREE.Vector3(0, 0.15, 1).normalize(),
    lastChange: performance.now(),
    changeInterval: 2500 + Math.random() * 2500,
    boundsRadius: 45,
    yMin: -18,
    yMax: 6
  }

  initAudioElements().then(() => {
    bubbleSounds = [
      audioElements.value['bubble-sound1'],
      audioElements.value['bubble-sound2']
    ].filter(Boolean)
    manyBubblesSound = audioElements.value['many-bubbles-sound'] || null

    bubbleSounds.forEach(s => {
      if (s) {
        s.volume = 0.6
        s.preload = 'auto'
      }
    })
    if (manyBubblesSound) {
      manyBubblesSound.volume = 0.5
      manyBubblesSound.preload = 'auto'
    }

    setupAudioControls()
  })

  document.addEventListener('click', handleClick)

  camera.position.copy(swim.pos)
  camera.lookAt(swim.pos.clone().add(swim.targetDir))

  function animate() {
    animationFrameId = requestAnimationFrame(animate)
    const now = performance.now()

    if (now - swim.lastChange > swim.changeInterval) {
      const jitter = new THREE.Vector3(
        (Math.random() * 2 - 1) * 0.6,
        (Math.random() * 2 - 1) * 0.35,
        1
      ).normalize()
      swim.targetDir.lerp(jitter, 0.5).normalize()
      swim.changeInterval = 2400 + Math.random() * 2800
      swim.lastChange = now
    }

    const toCenter = swim.pos.clone().multiplyScalar(-1)
    const dist = swim.pos.length()
    if (dist > swim.boundsRadius) {
      const pull = toCenter.normalize().multiplyScalar((dist - swim.boundsRadius) * 0.002)
      swim.targetDir.add(pull).normalize()
    }
    if (swim.pos.y < swim.yMin) {
      swim.targetDir.y += 0.02
    } else if (swim.pos.y > swim.yMax) {
      swim.targetDir.y -= 0.02
    }

    const dir = swim.vel.clone().normalize().lerp(swim.targetDir, 0.02).normalize()
    const speed = THREE.MathUtils.lerp(swim.vel.length(), swim.speed, 0.03)
    swim.vel.copy(dir.multiplyScalar(speed))
    swim.pos.add(swim.vel)

    const bob = Math.sin(now * 0.003) * 0.25
    const roll = Math.sin(now * 0.002) * 0.03
    camera.position.copy(swim.pos).add(new THREE.Vector3(0, bob * 0.4, 0))
    const lookTarget = swim.pos.clone()
      .add(dir.clone().multiplyScalar(5))
      .add(new THREE.Vector3(
        Math.sin(now * 0.0013) * 0.5,
        Math.cos(now * 0.0017) * 0.2,
        0
      ))
    camera.lookAt(lookTarget)
    camera.rotation.z += roll

    directionalLight.position.set(
      Math.sin(now * 0.0008) * 5,
      3 + Math.sin(now * 0.0013) * 0.6,
      Math.cos(now * 0.0009) * 5
    )

    if (now - lastBubbleTime > nextBubbleInterval) {
      const bubbleCount = getRandomBubbleCount()
      for (let i = 0; i < bubbleCount; i++) {
        const b = createRandomBubble()
        scene.add(b)
        bubbles.push(b)
        activeBubbles++
        if (activeBubbles < MAX_BUBBLES_FOR_SINGLE && bubbleSounds.length) {
          const s = bubbleSounds[Math.floor(Math.random() * bubbleSounds.length)]
          try { s.currentTime = 0; s.play() } catch (e) {}
        }
      }
      lastBubbleTime = now
      nextBubbleInterval = getRandomInterval()
    }

    for (let i = bubbles.length - 1; i >= 0; i--) {
      const bubble = bubbles[i]
      bubble.position.y += bubble.userData.speed
      const t = now * bubble.userData.wobbleSpeed
      bubble.position.x += Math.sin(t) * bubble.userData.wobbleAmount
      bubble.position.z += Math.cos(t) * bubble.userData.wobbleAmount
      bubble.scale.multiplyScalar(1.001)
      const tooHigh = bubble.position.y > camera.position.y + 6
      const tooFar = bubble.position.distanceTo(camera.position) > 60
      if (tooHigh || tooFar) {
        scene.remove(bubble)
        bubbles.splice(i, 1)
        activeBubbles = Math.max(0, activeBubbles - 1)
      }
    }

    const arr = plankton.geometry.attributes.position.array
    for (let i = 0; i < arr.length; i += 3) {
      if (arr[i] - camera.position.x > halfRange) arr[i] -= wrapRange
      else if (arr[i] - camera.position.x < -halfRange) arr[i] += wrapRange
      if (arr[i + 1] - camera.position.y > halfRange) arr[i + 1] -= wrapRange
      else if (arr[i + 1] - camera.position.y < -halfRange) arr[i + 1] += wrapRange
      if (arr[i + 2] - camera.position.z > halfRange) arr[i + 2] -= wrapRange
      else if (arr[i + 2] - camera.position.z < -halfRange) arr[i + 2] += wrapRange
    }
    plankton.geometry.attributes.position.needsUpdate = true

    renderer.render(scene, camera)
  }

  window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight
    camera.updateProjectionMatrix()
    renderer.setSize(window.innerWidth, window.innerHeight)
  })

  animate()
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
  if (renderer) {
    renderer.dispose()
  }
  Object.values(audioElements.value).forEach(audio => {
    audio.pause()
    audio.src = ''
    audio.load()
  })
  Object.keys(audioElements.value).forEach(id => {
    audioStore.removeAudioElement(id)
  })
  document.removeEventListener('click', handleClick)
})

function createRandomBubble() {
  const bubble = new THREE.Mesh(bubbleGeometry, createBubbleMaterial())
  bubble.position.x = camera.position.x + (Math.random() * 2 - 1) * 5
  bubble.position.z = camera.position.z + (Math.random() * 2 - 1) * 5
  bubble.position.y = camera.position.y - 5
  bubble.scale.setScalar(0.5 + Math.random() * 0.5)
  bubble.userData = {
    speed: 0.02 + Math.random() * 0.03,
    wobbleSpeed: Math.random() * 0.02,
    wobbleAmount: Math.random() * 0.1
  }
  return bubble
}

function getRandomBubbleCount() { return 1 + Math.floor(Math.random() * 10) }
</script>

<style>
@import url('@/assets/css/index.css');
#bubble-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background: linear-gradient(to bottom, #7FDBFF, #0074D9, #001f3f);
}

#audio-control {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 50000;
  pointer-events: auto;
}

.play-icon {
  width: 20px;
  height: 20px;
  position: relative;
}

.play-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-40%, -50%);
  border-left: 14px solid #a0c4ff;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
}

.play-icon.paused::before {
  display: none;
}

.play-icon.paused::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
  width: 14px;
  height: 14px;
  background: linear-gradient(
    to right,
    #a0c4ff 4px,
    transparent 4px,
    transparent 6px,
    #a0c4ff 6px,
    #a0c4ff 10px,
    transparent 10px
  );
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>