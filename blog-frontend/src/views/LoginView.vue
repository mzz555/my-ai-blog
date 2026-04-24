<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-top">
        <div class="logo">DevLog.</div>
        <h2 class="heading">欢迎回来</h2>
        <p class="sub">登录你的账号，继续探索优质技术内容</p>
        <div class="tabs">
          <button :class="['tab', { active: tab === 'login' }]" @click="tab = 'login'">登录</button>
          <button :class="['tab', { active: tab === 'register' }]" @click="tab = 'register'">注册</button>
        </div>
      </div>

      <!-- 登录表单 -->
      <div v-if="tab === 'login'" class="card-form">
        <div class="field">
          <label class="field-label">用户名/邮箱</label>
          <input v-model="form.username" class="field-input" placeholder="请输入用户名或邮箱" @keyup.enter="handleLogin" />
        </div>
        <div class="field">
          <label class="field-label">密码</label>
          <div class="password-wrap">
            <input v-model="form.password" :type="showPwd ? 'text' : 'password'"
              class="field-input" placeholder="请输入密码" @keyup.enter="handleLogin" />
            <button class="pwd-toggle" @click="showPwd = !showPwd" type="button">
              <svg v-if="showPwd" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
          </div>
        </div>
        <div class="options-row">
          <label class="remember">
            <input type="checkbox" v-model="remember" />
            <span>记住我</span>
          </label>
          <a href="#" class="forgot">忘记密码?</a>
        </div>
        <button class="submit-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录账号' }}
        </button>
        <div class="divider-row">
          <span class="divider-line"></span>
          <span class="divider-text">或</span>
          <span class="divider-line"></span>
        </div>
        <button class="github-btn" type="button">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/></svg>
          使用 GitHub 登录
        </button>
        <p class="footnote">
          还没有账号?
          <a href="#" class="footnote-link" @click.prevent="tab = 'register'">立即注册</a>
        </p>
      </div>

      <!-- 注册表单 -->
      <div v-else class="card-form">
        <div class="field">
          <label class="field-label">用户名 <span class="field-required">*</span></label>
          <input v-model="regForm.username" class="field-input" placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label class="field-label">邮箱 <span class="field-required">*</span></label>
          <input v-model="regForm.email" class="field-input" type="email" placeholder="请输入邮箱地址" />
        </div>
        <div class="field">
          <label class="field-label">密码 <span class="field-required">*</span></label>
          <div class="password-wrap">
            <input v-model="regForm.password" :type="showRegPwd ? 'text' : 'password'"
              class="field-input" placeholder="至少 6 位密码" />
            <button class="pwd-toggle" @click="showRegPwd = !showRegPwd" type="button">
              <svg v-if="showRegPwd" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            </button>
          </div>
        </div>
        <div class="field">
          <label class="field-label">确认密码 <span class="field-required">*</span></label>
          <input v-model="regForm.confirmPassword" :type="showRegPwd ? 'text' : 'password'"
            class="field-input" placeholder="再次输入密码" @keyup.enter="handleRegister" />
        </div>
        <button class="submit-btn" :disabled="regLoading" @click="handleRegister">
          {{ regLoading ? '注册中...' : '创建账号' }}
        </button>
        <p class="footnote">
          已有账号?
          <a href="#" class="footnote-link" @click.prevent="tab = 'login'">立即登录</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showPwd = ref(false)
const remember = ref(false)
const tab = ref('login')
const form = reactive({ username: '', password: '' })

const regLoading = ref(false)
const showRegPwd = ref(false)
const regForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/admin')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!regForm.username.trim() || !regForm.email.trim() || !regForm.password) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (regForm.password !== regForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (regForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  regLoading.value = true
  try {
    await register({ username: regForm.username.trim(), email: regForm.email.trim(), password: regForm.password })
    ElMessage.success('注册成功，请登录')
    tab.value = 'login'
    regForm.username = ''
    regForm.email = ''
    regForm.password = ''
    regForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #0A0A12;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 460px;
  background: #13131E;
  border-radius: 20px;
  overflow: hidden;
}

.card-top {
  padding: 40px 40px 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.logo { font-size: 22px; font-weight: 700; color: #E8A838; }
.heading { margin: 0; font-size: 26px; font-weight: 700; color: #F0F0F8; }
.sub { margin: 0; font-size: 14px; color: #6E6E82; line-height: 1.5; }

.tabs {
  display: flex;
  background: #0C0C10;
  border-radius: 8px;
  padding: 4px;
}
.tab {
  flex: 1;
  padding: 8px;
  border: none;
  background: transparent;
  color: #6E6E82;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.tab.active { background: #1C1C30; color: #F0F0F8; }

.card-form {
  padding: 0 40px 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 13px; color: #8A8A9E; }

.field-input {
  width: 100%;
  padding: 12px 16px;
  background: #0C0C10;
  border: 1px solid #252535;
  border-radius: 10px;
  color: #F0F0F8;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color var(--transition-fast);
}
.field-input::placeholder { color: #3A3A5C; }
.field-input:focus { border-color: #E8A838; }

.password-wrap { position: relative; }
.password-wrap .field-input { padding-right: 44px; }
.pwd-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6E6E82;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
}
.pwd-toggle:hover { color: #E8A838; }

.options-row { display: flex; align-items: center; justify-content: space-between; }
.remember { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #8A8A9E; cursor: pointer; }
.remember input { accent-color: #E8A838; }
.forgot { font-size: 13px; color: #E8A838; text-decoration: none; }
.forgot:hover { color: #F5BC50; }

.submit-btn {
  width: 100%;
  padding: 14px;
  background: #E8A838;
  color: #0C0C10;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.submit-btn:hover:not(:disabled) { background: #F5BC50; }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.divider-row { display: flex; align-items: center; gap: 12px; }
.divider-line { flex: 1; height: 1px; background: #252535; }
.divider-text { font-size: 12px; color: #3A3A5C; }

.github-btn {
  width: 100%;
  padding: 13px;
  background: #0C0C10;
  color: #F0F0F8;
  border: 1px solid #252535;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}
.github-btn:hover { border-color: #E8A838; color: #E8A838; }

.footnote { margin: 0; text-align: center; font-size: 13px; color: #6E6E82; }
.footnote-link { color: #E8A838; text-decoration: none; }
.footnote-link:hover { color: #F5BC50; }

.field-required { color: #EF4444; }
</style>
