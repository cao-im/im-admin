<template>
  <div class="login-page">
    <el-card class="card">
      <h1>曹操IM 管理后台</h1>
      <p class="hint">运营账号与客户端用户分开登录</p>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" placeholder="密码" size="large" show-password type="password" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="seed">初始账号 admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f6;
}
.card { width: 380px; padding: 12px 8px 4px; }
h1 { margin: 0 0 8px; font-size: 22px; }
.hint, .seed { color: #6b7280; font-size: 13px; }
.submit { width: 100%; }
</style>
