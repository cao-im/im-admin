<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="用户名 / 昵称" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column label="在线" width="90">
        <template #default="{ row }">{{ onlineText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="账号" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 0 ? 'danger' : 'success'">{{ row.enabled === 0 ? '禁用' : '启用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="120" v-if="auth.has('im:user:enable')">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggle(row)">{{ row.enabled === 0 ? '启用' : '禁用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const keyword = ref('')
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)

function onlineText(status) {
  return { 0: '离线', 1: '在线', 2: '忙碌' }[status] || '离线'
}

async function load() {
  loading.value = true
  try {
    const body = await http.get('/api/im/users', { params: { keyword: keyword.value, page: page.value, size } })
    records.value = body.data.records || []
    total.value = body.data.total || 0
  } finally {
    loading.value = false
  }
}

function changePage(value) {
  page.value = value
  load()
}

async function toggle(row) {
  const enabled = row.enabled === 0 ? 1 : 0
  await ElMessageBox.confirm(enabled === 0 ? '禁用后该用户无法登录和建连' : '确认启用该用户？', '账号启停')
  await http.put(`/api/im/users/${row.id}/enabled`, null, { params: { enabled } })
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
