<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import AppAlert from '@/components/AppAlert.vue'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMsg = ref('')
const showAdminAlert = ref(false)

async function handleLogin() {
  errorMsg.value = ''

  if (!loginForm.username.trim()) {
    errorMsg.value = '请输入账号（身份证号）'
    return
  }
  if (!loginForm.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const res: any = await login({
      username: loginForm.username,
      password: loginForm.password,
    })
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    if (res.data.userType === 1) {
      router.push('/student')
    } else if (res.data.userType === 2) {
      router.push('/counselor')
    } else if (res.data.userType === 3) {
      showAdminAlert.value = true
      return
    } else if (res.data.userType === 4) {
      router.push('/volunteer')
    } else if (res.data.userType === 5) {
      router.push('/volunteer/uniform')
    } else {
      router.push('/home')
    }
  } catch (err: any) {
    errorMsg.value = err.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

function handleKeyup(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    handleLogin()
  }
}

function handleAdminConfirm() {
  showAdminAlert.value = false
  window.location.href = 'https://ggqhweb.iizyinz.cn'
}
</script>

<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 主要内容区 -->
    <div class="login-content">
      <!-- Logo和品牌标识 -->
      <div class="brand-section">
        <div class="logo-wrapper">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="logo-svg">
            <circle cx="24" cy="24" r="22" stroke="white" stroke-width="2" fill="none" opacity="0.3"/>
            <path d="M14 32L24 14L34 32" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="18" r="3" fill="#FF7A1A"/>
            <path d="M18 28H30" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-title">光谷启航</h1>
        <p class="brand-subtitle">新生一站式服务平台</p>
      </div>

      <!-- 登录表单 -->
      <div class="form-section">
        <form class="login-form" @submit.prevent="handleLogin">
          <!-- 错误提示 -->
          <div class="error-msg" v-if="errorMsg">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {{ errorMsg }}
          </div>

          <!-- 账号输入 -->
          <div class="form-group">
            <label for="username">账号</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                id="username"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入身份证号"
                autocomplete="username"
                @keyup="handleKeyup"
              />
            </div>
          </div>

          <!-- 密码输入 -->
          <div class="form-group">
            <label for="password">密码</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码（身份证后六位）"
                autocomplete="current-password"
                @keyup="handleKeyup"
              />
            </div>
          </div>

          <!-- 登录按钮 -->
          <button
            type="submit"
            class="login-btn"
            :disabled="loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>登 录</span>
          </button>
        </form>

        <!-- 底部提示 -->
        <div class="form-footer">
          <p>忘记密码？请联系辅导员重置</p>
        </div>
      </div>
    </div>

    <!-- 管理员提示弹窗 -->
    <AppAlert 
      v-model:show="showAdminAlert" 
      message="管理员暂不支持 App 端，请转到 Web 端进行登录。"
      type="info"
      @update:show="handleAdminConfirm"
    />
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1A5CFF 0%, #0033CC 40%, #00C9A7 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: #FF7A1A;
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: #fff;
  bottom: -50px;
  left: -50px;
  animation: float 6s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: #4D8AFF;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.1; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.15; }
}

/* 主要内容区 */
.login-content {
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 1;
}

/* 品牌区域 */
.brand-section {
  text-align: center;
  margin-bottom: 40px;
  color: #fff;
}

.logo-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.logo-svg {
  width: 50px;
  height: 50px;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 3px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
  letter-spacing: 2px;
}

/* 表单区域 */
.form-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 32px 24px;
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FFF0F0;
  border: 1px solid #FFD4D4;
  border-radius: 10px;
  color: #FF4D6A;
  font-size: 14px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #3D465C;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #8C95A6;
  pointer-events: none;
}

.input-wrapper input {
  width: 100%;
  height: 48px;
  padding: 0 14px 0 44px;
  border: 1.5px solid #E8ECF1;
  border-radius: 12px;
  font-size: 15px;
  color: #3D465C;
  background: #fff;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.input-wrapper input::placeholder {
  color: #8C95A6;
}

.input-wrapper input:focus {
  border-color: #1A5CFF;
  box-shadow: 0 0 0 3px rgba(26, 92, 255, 0.1);
}

.login-btn {
  width: 100%;
  height: 50px;
  margin-top: 8px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #1A5CFF, #0033CC);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(26, 92, 255, 0.3);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(26, 92, 255, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.form-footer p {
  font-size: 13px;
  color: #8C95A6;
  margin: 0;
}

/* 移动端优化 */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-subtitle {
    font-size: 13px;
  }

  .form-section {
    padding: 28px 20px;
  }

  .input-wrapper input {
    height: 46px;
    font-size: 14px;
  }

  .login-btn {
    height: 48px;
    font-size: 15px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 360px) {
  .brand-title {
    font-size: 24px;
  }

  .logo-wrapper {
    width: 70px;
    height: 70px;
  }

  .logo-svg {
    width: 45px;
    height: 45px;
  }
}
</style>
