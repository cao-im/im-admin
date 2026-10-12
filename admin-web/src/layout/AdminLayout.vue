<template>
  <el-container class="shell">
    <el-aside :width="asideWidth" class="aside">
      <div class="brand" :class="{ collapsed }">
        <div class="brand-mark" aria-hidden="true">
          <span class="bubble b1" />
          <span class="bubble b2" />
        </div>
        <div v-if="!collapsed" class="brand-text">
          <strong>曹操IM</strong>
          <span>Ops Console</span>
        </div>
      </div>

      <nav class="nav">
        <template v-for="group in menuGroups" :key="group.key">
          <div v-if="group.items.length" class="nav-group">
            <div class="nav-label">{{ group.label }}</div>
            <el-menu :default-active="route.path" router :collapse="collapsed">
              <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
                <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
                <template #title>
                  <span>{{ item.name }}</span>
                </template>
              </el-menu-item>
            </el-menu>
          </div>
        </template>
      </nav>

      <div class="aside-foot">
        <button class="collapse-btn" type="button" @click="collapsed = !collapsed">
          <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
          <span v-if="!collapsed">收起侧栏</span>
        </button>
      </div>
    </el-aside>

    <el-container class="main-wrap">
      <el-header class="header" height="64px">
        <div class="title-block">
          <h1>{{ route.meta.title || '管理后台' }}</h1>
          <p>{{ pageHint }}</p>
        </div>
        <div class="actions">
          <el-button class="ghost-btn" @click="passwordVisible = true">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
          <div class="profile">
            <div class="avatar-dot admin">{{ initials }}</div>
            <div class="user-meta">
              <span class="name">{{ profile?.nickname || profile?.username }}</span>
              <span class="sub">{{ profile?.superAdmin ? '超级管理员' : '运营账号' }}</span>
            </div>
          </div>
          <el-button class="logout-btn" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <div class="route-enter" :key="route.fullPath">
          <router-view />
        </div>
      </el-main>
    </el-container>

    <el-dialog v-model="passwordVisible" title="修改密码" width="420px" class="im-dialog">
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
import { Lock } from '@element-plus/icons-vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const profile = computed(() => auth.profile)
const menus = computed(() => (auth.profile?.menus || []).filter((item) => item.path))
const passwordVisible = ref(false)
const collapsed = ref(false)
const password = reactive({ oldPassword: '', newPassword: '' })

const asideWidth = computed(() => (collapsed.value ? '84px' : '248px'))

const initials = computed(() => {
  const name = profile.value?.nickname || profile.value?.username || '管'
  return String(name).slice(0, 1).toUpperCase()
})

const menuGroups = computed(() => {
  const items = menus.value
  const overview = items.filter((m) => m.path === '/dashboard')
  const im = items.filter((m) => m.path.startsWith('/im'))
  const system = items.filter((m) => m.path.startsWith('/system'))
  const other = items.filter((m) => !overview.includes(m) && !im.includes(m) && !system.includes(m))
  return [
    { key: 'overview', label: '总览', items: overview },
    { key: 'im', label: '即时通讯', items: im },
    { key: 'system', label: '系统', items: system },
    { key: 'other', label: '其他', items: other }
  ]
})

const pageHint = computed(() => {
  const map = {
    '/dashboard': '实时掌握用户、群聊与消息脉搏',
    '/im/users': '检索并管理客户端用户启停状态',
    '/im/groups': '查看群聊健康度，必要时解散群',
    '/im/messages': '按人、群与关键词回溯会话内容',
    '/im/sensitive': '维护敏感词库，守护内容安全',
    '/im/config': '调整注册、过滤与容量等运行参数',
    '/system/admins': '管理运营账号与角色绑定',
    '/system/roles': '配置角色与菜单权限',
    '/system/menus': '维护后台导航与按钮权限',
    '/system/audit': '审计每一次关键运营操作'
  }
  return map[route.path] || '曹操IM 运营控制台'
})

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
.shell {
  min-height: 100vh;
}

.aside {
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(420px 280px at 20% 0%, rgba(18, 184, 168, 0.22), transparent 60%),
    linear-gradient(180deg, #101826 0%, #0b1220 100%);
  color: #e8eef8;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width 0.22s ease;
  overflow: hidden;
  z-index: 20;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.brand.collapsed {
  justify-content: center;
  padding: 0;
}

.brand-mark {
  position: relative;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.bubble {
  position: absolute;
  border-radius: 14px;
  background: linear-gradient(135deg, #2dd4bf, #0ea5e9);
  box-shadow: 0 8px 20px rgba(45, 212, 191, 0.35);
}

.bubble.b1 {
  inset: 0 8px 8px 0;
  animation: pulse-soft 3.2s ease-in-out infinite;
}

.bubble.b2 {
  inset: 14px 0 0 14px;
  opacity: 0.72;
  background: linear-gradient(135deg, #38bdf8, #6366f1);
  animation: pulse-soft 3.2s ease-in-out 0.6s infinite;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  min-width: 0;
}

.brand-text strong {
  font-family: var(--im-display);
  font-size: 17px;
  letter-spacing: 0.02em;
}

.brand-text span {
  font-size: 11px;
  color: rgba(232, 238, 248, 0.55);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav {
  flex: 1;
  overflow: auto;
  padding: 14px 10px 8px;
}

.nav-group + .nav-group {
  margin-top: 10px;
}

.nav-label {
  padding: 6px 12px 8px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(232, 238, 248, 0.38);
}

.aside :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: 12px;
  color: rgba(232, 238, 248, 0.78) !important;
}

.aside :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}

.aside :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(18, 184, 168, 0.28), rgba(18, 184, 168, 0.08)) !important;
  color: #fff !important;
  box-shadow: inset 3px 0 0 #2dd4bf;
}

.aside :deep(.el-menu-item .el-icon) {
  color: inherit;
}

.aside-foot {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(232, 238, 248, 0.7);
  cursor: pointer;
  font: inherit;
  transition: background 0.2s ease, color 0.2s ease;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.main-wrap {
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--im-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.title-block h1 {
  margin: 0;
  font-family: var(--im-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--im-text);
}

.title-block p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--im-text-muted);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--im-border);
}

.ghost-btn {
  border: 1px solid var(--im-border);
  background: #fff;
  color: var(--im-text-secondary);
}

.logout-btn {
  border: none;
  background: rgba(244, 63, 94, 0.1);
  color: #be123c;
  font-weight: 600;
}

.logout-btn:hover {
  background: rgba(244, 63, 94, 0.16);
  color: #9f1239;
}

.main {
  padding: 22px 28px 32px;
}

@keyframes pulse-soft {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.04); opacity: 0.9; }
}

@media (max-width: 960px) {
  .title-block p,
  .profile .user-meta {
    display: none;
  }
  .header,
  .main {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
