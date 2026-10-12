<template>
  <div class="login-page">
    <div class="ambient" aria-hidden="true">
      <span class="orb o1" />
      <span class="orb o2" />
      <span class="orb o3" />
      <div class="chat-trail">
        <i /><i /><i /><i /><i />
      </div>
    </div>

    <section class="hero">
      <div class="brand-row">
        <div class="brand-mark">
          <span class="bubble b1" />
          <span class="bubble b2" />
        </div>
        <div>
          <h1>曹操IM</h1>
          <p class="eyebrow">Instant Messaging Ops</p>
        </div>
      </div>
      <p class="lead">为运营而生的会话控制台 —— 用户、群聊、消息与风控，尽在一目。</p>
      <ul class="points">
        <li>实时掌握消息脉搏</li>
        <li>细粒度角色权限</li>
        <li>内容安全与审计闭环</li>
      </ul>
    </section>

    <el-card class="card" shadow="never">
      <div class="card-head">
        <h2>登录控制台</h2>
        <p>运营账号与客户端用户分开认证</p>
      </div>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            placeholder="密码"
            size="large"
            show-password
            type="password"
            :prefix-icon="Lock"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
          进入控制台
        </el-button>
      </el-form>
      <p class="seed">初始账号 admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function submit() {
  loading.value = true
  try {
    const body = await http.post('/api/auth/login', form)
    auth.setSession(body.data.token, body.data.profile)
    router.push('/dashboard')
  } catch {
    // http 已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 420px);
  gap: 48px;
  align-items: center;
  padding: 48px clamp(24px, 6vw, 96px);
  position: relative;
  overflow: hidden;
}

.ambient {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.55;
}

.o1 {
  width: 420px;
  height: 420px;
  left: -80px;
  top: -120px;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.55), transparent 68%);
  animation: drift 12s ease-in-out infinite;
}

.o2 {
  width: 360px;
  height: 360px;
  right: 8%;
  bottom: -80px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.45), transparent 68%);
  animation: drift 14s ease-in-out infinite reverse;
}

.o3 {
  width: 220px;
  height: 220px;
  left: 42%;
  top: 28%;
  background: radial-gradient(circle, rgba(15, 23, 42, 0.08), transparent 70%);
}

.chat-trail {
  position: absolute;
  left: 8%;
  bottom: 12%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: min(360px, 40vw);
}

.chat-trail i {
  display: block;
  height: 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  animation: trail 4.8s ease-in-out infinite;
}

.chat-trail i:nth-child(1) { width: 72%; animation-delay: 0s; }
.chat-trail i:nth-child(2) { width: 48%; margin-left: auto; background: rgba(45, 212, 191, 0.35); animation-delay: 0.4s; }
.chat-trail i:nth-child(3) { width: 64%; animation-delay: 0.8s; }
.chat-trail i:nth-child(4) { width: 40%; margin-left: auto; background: rgba(56, 189, 248, 0.35); animation-delay: 1.2s; }
.chat-trail i:nth-child(5) { width: 56%; animation-delay: 1.6s; }

.hero {
  position: relative;
  z-index: 1;
  max-width: 520px;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.brand-mark {
  position: relative;
  width: 56px;
  height: 56px;
}

.bubble {
  position: absolute;
  border-radius: 16px;
  background: linear-gradient(135deg, #2dd4bf, #0ea5e9);
  box-shadow: 0 12px 28px rgba(45, 212, 191, 0.35);
}

.bubble.b1 { inset: 0 10px 10px 0; }
.bubble.b2 {
  inset: 18px 0 0 18px;
  opacity: 0.75;
  background: linear-gradient(135deg, #38bdf8, #6366f1);
}

.hero h1 {
  margin: 0;
  font-family: var(--im-display);
  font-size: clamp(40px, 6vw, 56px);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--im-text);
  line-height: 1.05;
}

.eyebrow {
  margin: 6px 0 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--im-teal-deep);
  font-weight: 700;
}

.lead {
  margin: 0 0 24px;
  font-size: 17px;
  line-height: 1.7;
  color: var(--im-text-secondary);
  max-width: 28em;
}

.points {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.points li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--im-text);
  font-weight: 500;
}

.points li::before {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2dd4bf, #0ea5e9);
  box-shadow: 0 0 0 4px rgba(45, 212, 191, 0.18);
}

.card {
  position: relative;
  z-index: 1;
  width: 100%;
  border-radius: 24px !important;
  border: 1px solid rgba(255, 255, 255, 0.7) !important;
  background: rgba(255, 255, 255, 0.82) !important;
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12) !important;
  animation: im-fade-up 0.45s ease both;
}

.card :deep(.el-card__body) {
  padding: 28px 28px 22px;
}

.card-head h2 {
  margin: 0;
  font-family: var(--im-display);
  font-size: 24px;
  font-weight: 700;
}

.card-head p {
  margin: 8px 0 22px;
  color: var(--im-text-muted);
  font-size: 13px;
}

.submit {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.seed {
  margin: 16px 0 0;
  text-align: center;
  color: var(--im-text-muted);
  font-size: 12px;
}

@keyframes drift {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(18px, 12px); }
}

@keyframes trail {
  0%, 100% { opacity: 0.35; transform: translateX(0); }
  50% { opacity: 0.85; transform: translateX(6px); }
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 32px 20px 40px;
    gap: 28px;
  }
  .chat-trail { display: none; }
  .lead { font-size: 15px; }
  .hero h1 { font-size: 40px; }
}
</style>
