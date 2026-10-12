<template>
  <div class="page-panel">
    <div class="toolbar">
      <el-button v-if="auth.has('system:menu:edit')" type="primary" @click="openCreate()">新增菜单</el-button>
    </div>
    <el-table :data="tree" row-key="id" default-expand-all>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="path" label="路径" min-width="140" />
      <el-table-column prop="permission" label="权限" min-width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <span class="status-pill" :class="row.menuType === 3 ? 'busy' : 'ok'">
            {{ row.menuType === 3 ? '按钮' : '菜单' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="180" v-if="auth.has('system:menu:edit')">
        <template #default="{ row }">
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" :title="form.id ? '编辑菜单' : '新增菜单'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="上级 ID"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="权限"><el-input v-model="form.permission" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
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
const tree = ref([])
const visible = ref(false)
const form = reactive({ id: null, parentId: 0, name: '', path: '', permission: '', icon: '', sortOrder: 0, menuType: 2 })

async function load() {
  tree.value = (await http.get('/api/system/menus')).data || []
}
function openCreate() {
  Object.assign(form, { id: null, parentId: 0, name: '', path: '', permission: '', icon: '', sortOrder: 0, menuType: 2 })
  visible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  visible.value = true
}
async function save() {
  if (form.id) await http.put(`/api/system/menus/${form.id}`, form)
  else await http.post('/api/system/menus', form)
  visible.value = false
  ElMessage.success('已保存')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`删除菜单 ${row.name}？`)
  await http.delete(`/api/system/menus/${row.id}`)
  load()
}
onMounted(load)
</script>
