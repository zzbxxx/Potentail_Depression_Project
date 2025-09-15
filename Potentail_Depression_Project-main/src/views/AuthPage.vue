<template>
  <BackgroundScene />

   <div class="auth-container">
    <!-- 欢迎页面 -->
    <div v-if="view === 'welcome'" class="welcome">
      <div class="login-buttons">
        <button class="feature-card" @click="touristLogin">
          <h3>游客登录</h3>
        </button>
        <button class="feature-card" @click="view = 'login'">
          <h3>账户登录</h3>
        </button>
        <button class="feature-card" @click="view = 'cross-device'">
          <h3>跨设备游客登录</h3>
        </button>
      </div>
    </div>

    <!-- 登录/注册页面 -->
    <div v-if="view === 'login' || view === 'register'" class="auth-form">
      <div v-if="view === 'login'" class="auth-tab">
        <h3>账户登录</h3>
        <input v-model="loginForm.username" class="input-field" placeholder="用户名/手机号" type="text" />
        <input v-model="loginForm.password" class="input-field" placeholder="密码" type="password" />
        <button class="submit-btn" @click="doLogin">登录</button>
      </div>
      <div v-if="view === 'register'" class="auth-tab">
        <h3>新用户注册</h3>
        <input v-model="registerForm.username" class="input-field" placeholder="设置用户名" type="text" />
        <input v-model="registerForm.phone" class="input-field" placeholder="手机号" type="tel" />
        <input v-model="registerForm.password" class="input-field" placeholder="设置密码" type="password" />
        <button class="submit-btn" @click="doRegister">注册</button>
      </div>
      <div class="action-tabs">
        <button class="tab-item" @click="view = view === 'login' ? 'register' : 'login'">
          {{ view === 'login' ? '没有账户？立即注册' : '已有账户？登录' }}
        </button>
        <a @click="backToIndex" class="tab-item">返回首页</a>
        <a href="#" class="tab-item">忘记密码？</a>
      </div>
      <div v-if="errorMessage" class="error-popup">{{ errorMessage }}</div>
    </div>

    <!-- 跨设备登录 -->
    <div v-if="view === 'cross-device'" class="cross-device-login">
      <div class="recovery-box">
        <div class="prompt-text">
          输入您的找回码（剩余 {{ attemptsLeft }} 次机会）
        </div>
        <input v-model="code" class="recovery-input" placeholder="请输入8位找回码" maxlength="8" />
        <button class="cross-submit-btn" @click="verifyCode">提交</button>
      </div>
      <button class="return-card" @click="view = 'welcome'">
        <h3>返回</h3>
      </button>
      <div v-if="errorMessage" class="error-popup">{{ errorMessage }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import DeviceApiService from '@/api/deviceApi'
import { login, register } from '@/api/authApi'
import BackgroundScene from '@/components/BackgroundScene.vue'

const router = useRouter()
const view = ref('welcome')

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', phone: '', password: '' })
const code = ref('')


async function touristLogin() {
  const fingerprint = await DeviceApiService.generateDeviceFingerprint()
  const result = await DeviceApiService.guestLogin(fingerprint)
  localStorage.setItem('auth_token', result.token)
  router.push('/main')
}

async function doLogin() {
  const res = await login(loginForm.value)
  if (res.token) {
    localStorage.setItem('auth_token', res.token)
    router.push('/main')
  }
}

async function doRegister() {
  const res = await register(registerForm.value)
  if (res.code === 0) {
    alert('注册成功，请登录')
    view.value = 'login'
  }
}

async function verifyCode() {
  const fingerprint = await DeviceApiService.generateDeviceFingerprint()
  const result = await DeviceApiService.verifyRecoveryCode({
    code: code.value,
    newDeviceFingerprint: fingerprint
  })
  localStorage.setItem('auth_token', result.token)
  router.push('/main')
}

async function backToIndex(){
    view.value = 'welcome'
}
</script>

<style scoped>
@import url('/src/assets/css/index.css');
@import url('/src/assets/css/auth.css');
@import url('/src/assets/css/cross.css');

.welcome{
    position: absolute;
    top: 0px;
}
</style>