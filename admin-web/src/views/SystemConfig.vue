<template>
  <el-card>
    <el-form label-width="180px" style="max-width: 640px">
      <el-form-item v-for="item in configs" :key="item.configKey" :label="item.remark || item.configKey">
        <el-switch v-if="isSwitch(item.configKey)" v-model="form[item.configKey]" active-value="1" inactive-value="0" />
        <el-input-number v-else v-model="numbers[item.configKey]" :min="1" :max="10000" />
      </el-form-item>
      <el-form-item v-if="auth.has('im:config:edit')">
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
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
