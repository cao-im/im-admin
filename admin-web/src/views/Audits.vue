<template>
  <div class="page-panel">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="管理员 / 动作" clearable style="width: 260px" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
    </div>
    <el-table :data="records">
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="管理员" width="140">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="avatar-dot admin">{{ String(row.adminName || '?').slice(0, 1) }}</div>
            <div class="user-meta">
              <span class="name">{{ row.adminName }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="140" />
      <el-table-column prop="targetType" label="对象" width="110" />
      <el-table-column prop="targetId" label="对象 ID" width="120" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip min-width="180" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <span class="status-pill" :class="row.result === 0 ? 'danger' : 'ok'">
            {{ row.result === 0 ? '失败' : '成功' }}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const keyword = ref('')
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 20

async function load() {
  const body = await http.get('/api/system/audits', { params: { keyword: keyword.value, page: page.value, size } })
  records.value = body.data.records || []
  total.value = body.data.total || 0
}
function changePage(value) { page.value = value; load() }
onMounted(load)
</script>
