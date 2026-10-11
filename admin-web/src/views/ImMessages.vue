<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.userId" placeholder="用户 ID" style="width: 140px" />
      <el-input v-model="query.groupId" placeholder="群 ID" style="width: 140px" />
      <el-input v-model="query.keyword" placeholder="内容" style="width: 180px" />
      <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="开始" end-placeholder="结束" />
      <el-button type="primary" @click="load">检索</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="发送者" width="160">
        <template #default="{ row }">{{ row.fromName || row.fromId }}</template>
      </el-table-column>
      <el-table-column prop="toId" label="接收者" width="100" />
      <el-table-column prop="groupId" label="群" width="100" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ typeText(row.msgType) }}</template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'

const query = reactive({ userId: '', groupId: '', keyword: '' })
const range = ref([])
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)

function typeText(type) {
  return ['文本', '图片', '文件', '语音', '视频'][type] || '其他'
}

async function load() {
  loading.value = true
  try {
    const body = await http.get('/api/im/messages', {
      params: {
        userId: query.userId || undefined,
        groupId: query.groupId || undefined,
        keyword: query.keyword,
        startTime: range.value?.[0],
        endTime: range.value?.[1],
        page: page.value,
        size
      }
    })
    records.value = body.data.records || []
    total.value = body.data.total || 0
  } finally {
    loading.value = false
  }
}
function changePage(value) { page.value = value; load() }
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
