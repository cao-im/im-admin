import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import ImUsers from '../views/ImUsers.vue'
import ImGroups from '../views/ImGroups.vue'
import ImMessages from '../views/ImMessages.vue'
import SensitiveWords from '../views/SensitiveWords.vue'
import SystemConfig from '../views/SystemConfig.vue'
import Admins from '../views/Admins.vue'
import Roles from '../views/Roles.vue'
import Menus from '../views/Menus.vue'
import Audits from '../views/Audits.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login, meta: { public: true } },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: Dashboard, meta: { permission: 'dashboard:view', title: '概览' } },
        { path: 'im/users', component: ImUsers, meta: { permission: 'im:user:list', title: '用户管理' } },
        { path: 'im/groups', component: ImGroups, meta: { permission: 'im:group:list', title: '群组管理' } },
        { path: 'im/messages', component: ImMessages, meta: { permission: 'im:message:list', title: '消息检索' } },
        { path: 'im/sensitive', component: SensitiveWords, meta: { permission: 'im:sensitive:list', title: '敏感词' } },
        { path: 'im/config', component: SystemConfig, meta: { permission: 'im:config:list', title: '系统配置' } },
        { path: 'system/admins', component: Admins, meta: { permission: 'system:admin:list', title: '管理员' } },
        { path: 'system/roles', component: Roles, meta: { permission: 'system:role:list', title: '角色' } },
        { path: 'system/menus', component: Menus, meta: { permission: 'system:menu:list', title: '菜单' } },
        { path: 'system/audit', component: Audits, meta: { permission: 'system:audit:list', title: '审计日志' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    return '/login'
  }
  if (to.meta.permission && auth.token && !auth.has(to.meta.permission)) {
    return '/dashboard'
  }
  return true
})

export default router
