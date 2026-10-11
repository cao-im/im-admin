<template>
  <el-container class="shell">
    <el-aside width="220px" class="aside">
      <div class="brand">曹操IM</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
          <span>{{ item.name }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title || '管理后台' }}</span>
        <div class="actions">
          <el-button link @click="passwordVisible = true">修改密码</el-button>
          <span>{{ profile?.nickname || profile?.username }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    <el-dialog v-model="passwordVisible" title="修改密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="原密码"><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="changePassword">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const profile = computed(() => auth.profile)
const menus = computed(() => (auth.profile?.menus || []).filter((item) => item.path))
const passwordVisible = ref(false)
const password = reactive({ oldPassword: '', newPassword: '' })

async function logout() {
  try { await http.post('/api/auth/logout') } catch { /* 忽略退出失败 */ }
  auth.logout()
  router.push('/login')
}

async function changePassword() {
  await http.put('/api/auth/password', password)
  ElMessage.success('密码已更新')
  passwordVisible.value = false
  password.oldPassword = ''
  password.newPassword = ''
}
</script>

<style scoped>
.shell { min-height: 100vh; }
.aside { background: #fff; border-right: 1px solid #e6e8eb; }
.brand { height: 56px; display: flex; align-items: center; padding: 0 20px; font-weight: 700; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e6e8eb; }
.actions { display: flex; align-items: center; gap: 12px; }
</style>
