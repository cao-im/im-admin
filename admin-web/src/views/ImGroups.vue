<template>
  <div class="page-panel">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="群名称" clearable style="width: 260px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="群聊" min-width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="avatar-dot group">{{ (row.name || '群').slice(0, 1) }}</div>
            <div class="user-meta">
              <span class="name">{{ row.name }}</span>
              <span class="sub">群主 ID {{ row.ownerId }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="maxMembers" label="人数上限" width="110" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <span class="status-pill" :class="row.status === 0 ? 'info' : 'ok'">
            {{ row.status === 0 ? '已解散' : '正常' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" v-if="auth.has('im:group:dismiss')">
        <template #default="{ row }">
          <el-button v-if="row.status !== 0" link type="danger" @click="dismiss(row)">解散</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
  </div>
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

async function load() {
  loading.value = true
  try {
    const body = await http.get('/api/im/groups', { params: { keyword: keyword.value, page: page.value, size } })
    records.value = body.data.records || []
    total.value = body.data.total || 0
  } finally {
    loading.value = false
  }
}
function changePage(value) { page.value = value; load() }
async function dismiss(row) {
  await ElMessageBox.confirm(`解散后成员不能再在「${row.name}」发言`, '解散群')
  await http.put(`/api/im/groups/${row.id}/dismiss`)
  load()
}
onMounted(load)
</script>
