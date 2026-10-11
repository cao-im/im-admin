<template>
  <el-card>
    <div class="toolbar">
      <el-button v-if="auth.has('system:role:edit')" type="primary" @click="openCreate">新增角色</el-button>
    </div>
    <el-table :data="roles">
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="remark" label="说明" />
      <el-table-column label="操作" width="220" v-if="auth.has('system:role:edit')">
        <template #default="{ row }">
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link @click="openMenus(row)">菜单</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑角色' : '新增角色'" width="420px">
      <el-form label-width="70px">
        <el-form-item label="编码"><el-input v-model="form.code" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" title="分配菜单" width="420px">
      <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" :props="{ label: 'name', children: 'children' }" default-expand-all />
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const roles = ref([])
const menuTree = ref([])
const visible = ref(false)
const menuVisible = ref(false)
const currentRoleId = ref(null)
const treeRef = ref()
const form = reactive({ id: null, code: '', name: '', remark: '' })

async function load() {
  roles.value = (await http.get('/api/system/roles')).data || []
  menuTree.value = (await http.get('/api/system/menus')).data || []
}
function openCreate() {
  Object.assign(form, { id: null, code: '', name: '', remark: '' })
  visible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  visible.value = true
}
async function save() {
  if (form.id) await http.put(`/api/system/roles/${form.id}`, form)
  else await http.post('/api/system/roles', form)
  visible.value = false
  ElMessage.success('已保存')
  load()
}
async function openMenus(row) {
  currentRoleId.value = row.id
  menuVisible.value = true
  const checked = (await http.get(`/api/system/roles/${row.id}/menus`)).data || []
  await nextTick()
  treeRef.value.setCheckedKeys(checked)
}
async function saveMenus() {
  const menuIds = [...treeRef.value.getCheckedKeys(), ...treeRef.value.getHalfCheckedKeys()]
  await http.put(`/api/system/roles/${currentRoleId.value}/menus`, { menuIds })
  menuVisible.value = false
  ElMessage.success('菜单已更新')
}
async function remove(row) {
  await ElMessageBox.confirm(`删除角色 ${row.name}？`)
  await http.delete(`/api/system/roles/${row.id}`)
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
