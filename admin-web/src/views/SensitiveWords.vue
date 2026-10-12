<template>
  <div class="page-panel">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="敏感词" clearable style="width: 220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
      <div class="spacer" />
      <el-input v-if="auth.has('im:sensitive:edit')" v-model="word" placeholder="新增敏感词" style="width: 220px" />
      <el-button v-if="auth.has('im:sensitive:edit')" type="primary" plain @click="create">添加</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="word" label="敏感词" min-width="200">
        <template #default="{ row }">
          <span class="word-chip">{{ row.word }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <span class="status-pill" :class="row.enabled === 0 ? 'info' : 'ok'">
            {{ row.enabled === 0 ? '停用' : '启用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" v-if="auth.has('im:sensitive:edit')">
        <template #default="{ row }">
          <el-button link @click="toggle(row)">{{ row.enabled === 0 ? '启用' : '停用' }}</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const keyword = ref('')
const word = ref('')
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const body = await http.get('/api/im/sensitive-words', { params: { keyword: keyword.value, page: page.value, size } })
    records.value = body.data.records || []
    total.value = body.data.total || 0
  } finally {
    loading.value = false
  }
}
function changePage(value) { page.value = value; load() }
async function create() {
  await http.post('/api/im/sensitive-words', { word: word.value })
  word.value = ''
  load()
}
async function toggle(row) {
  await http.put(`/api/im/sensitive-words/${row.id}/enabled`, null, { params: { enabled: row.enabled === 0 ? 1 : 0 } })
  load()
}
async function remove(row) {
  await http.delete(`/api/im/sensitive-words/${row.id}`)
  load()
}
onMounted(load)
</script>

<style scoped>
.spacer { flex: 1; min-width: 8px; }
.word-chip {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(244, 63, 94, 0.08);
  color: #be123c;
  font-weight: 600;
  font-size: 13px;
}
</style>
