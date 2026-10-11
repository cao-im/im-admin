<template>
  <div class="cards">
    <el-card><div class="num">{{ stats.userCount }}</div><div>用户数</div></el-card>
    <el-card><div class="num">{{ stats.groupCount }}</div><div>正常群数</div></el-card>
    <el-card><div class="num">{{ stats.todayMessageCount }}</div><div>今日消息</div></el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import http from '../api/http'

const stats = reactive({ userCount: 0, groupCount: 0, todayMessageCount: 0 })

onMounted(async () => {
  const body = await http.get('/api/im/stats')
  Object.assign(stats, body.data || {})
})
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.num { font-size: 32px; font-weight: 700; margin-bottom: 6px; }
</style>
