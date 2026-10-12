<template>
  <div class="page-panel">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="账号 / 昵称" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button v-if="auth.has('system:admin:edit')" type="primary" plain @click="openCreate">新增</el-button>
    </div>
    <el-table :data="records">
      <el-table-column label="账号" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="avatar-dot admin">{{ (row.nickname || row.username || '?').slice(0, 1) }}</div>
            <div class="user-meta">
              <span class="name">{{ row.nickname || row.username }}</span>
              <span class="sub">{{ row.username }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">{{ (row.roleNames || []).join('、') || '—' }}</template>
      </el-table-column>
      <el-table-column label="超级管理员" width="120">
        <template #default="{ row }">
          <span class="status-pill" :class="row.superAdmin === 1 ? 'ok' : 'info'">
            {{ row.superAdmin === 1 ? '是' : '否' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="status-pill" :class="row.status === 0 ? 'danger' : 'ok'">
            {{ row.status === 0 ? '停用' : '正常' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" v-if="auth.has('system:admin:edit')">
        <template #default="{ row }">
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link @click="openReset(row)">重置密码</el-button>
          <el-button link @click="toggle(row)">{{ row.status === 0 ? '启用' : '停用' }}</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />

    <el-dialog v-model="visible" :title="form.id ? '编辑管理员' : '新增管理员'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item v-if="!form.id" label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="超级管理员"><el-switch v-model="form.superAdmin" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const keyword = ref('')
const records = ref([])
const roles = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const visible = ref(false)
const form = reactive({ id: null, username: '', password: '', nickname: '', superAdmin: 0, roleIds: [] })

async function load() {
  const body = await http.get('/api/system/admins', { params: { keyword: keyword.value, page: page.value, size } })
  records.value = body.data.records || []
  total.value = body.data.total || 0
}
function changePage(value) { page.value = value; load() }
async function loadRoles() {
  const body = await http.get('/api/system/roles')
  roles.value = body.data || []
}
function openCreate() {
  Object.assign(form, { id: null, username: '', password: '', nickname: '', superAdmin: 0, roleIds: [] })
  visible.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, username: row.username, password: '', nickname: row.nickname, superAdmin: row.superAdmin, roleIds: row.roleIds || [] })
  visible.value = true
}
async function save() {
  if (form.id) {
    await http.put(`/api/system/admins/${form.id}`, form)
    await http.put(`/api/system/admins/${form.id}/roles`, { roleIds: form.roleIds })
  } else {
    await http.post('/api/system/admins', form)
  }
  visible.value = false
  ElMessage.success('已保存')
  load()
}
async function openReset(row) {
  const { value } = await ElMessageBox.prompt('输入新密码', '重置密码', { inputType: 'password' })
  await http.put(`/api/system/admins/${row.id}/password`, { password: value })
  ElMessage.success('密码已重置')
}
async function toggle(row) {
  await http.put(`/api/system/admins/${row.id}/status`, null, { params: { status: row.status === 0 ? 1 : 0 } })
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`删除管理员 ${row.username}？`)
  await http.delete(`/api/system/admins/${row.id}`)
  load()
}
onMounted(async () => { await loadRoles(); await load() })
</script>
