<template>
  <div class="page-panel config-panel">
    <div class="config-intro">
      <h3>运行参数</h3>
      <p>开关与容量配置会直接影响客户端注册、内容过滤与群聊上限。</p>
    </div>
    <el-form label-width="200px" class="config-form">
      <el-form-item v-for="item in configs" :key="item.configKey" :label="item.remark || item.configKey">
        <el-switch v-if="isSwitch(item.configKey)" v-model="form[item.configKey]" active-value="1" inactive-value="0" />
        <el-input-number v-else v-model="numbers[item.configKey]" :min="1" :max="10000" />
      </el-form-item>
      <el-form-item v-if="auth.has('im:config:edit')">
        <el-button type="primary" @click="save">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const configs = ref([])
const form = reactive({})
const numbers = reactive({})

function isSwitch(key) {
  return key === 'register_enabled' || key === 'sensitive_filter_enabled'
}

async function load() {
  const body = await http.get('/api/im/configs')
  configs.value = body.data || []
  for (const item of configs.value) {
    if (isSwitch(item.configKey)) {
      form[item.configKey] = item.configValue
    } else {
      numbers[item.configKey] = Number(item.configValue)
    }
  }
}

async function save() {
  const payload = { ...form }
  for (const item of configs.value) {
    if (!isSwitch(item.configKey)) {
      payload[item.configKey] = String(numbers[item.configKey])
    }
  }
  await http.put('/api/im/configs', payload)
  ElMessage.success('已保存')
}

onMounted(load)
</script>

<style scoped>
.config-intro {
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--im-border);
}

.config-intro h3 {
  margin: 0;
  font-family: var(--im-display);
  font-size: 18px;
}

.config-intro p {
  margin: 8px 0 0;
  color: var(--im-text-muted);
  font-size: 13px;
}

.config-form {
  max-width: 680px;
}
</style>
