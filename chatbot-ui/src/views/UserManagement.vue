<template>
  <div class="user-management">
    <div class="header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog">添加用户</el-button>
    </div>

    <div class="user-list">
      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="roles" label="角色">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" class="role-tag">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑用户' : '添加用户'"
      width="50%"
    >
      <el-form :model="userForm" label-width="80px" :rules="rules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-else>
          <el-input v-model="userForm.password" type="password" placeholder="留空表示不修改密码" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="userForm.roles" multiple placeholder="请选择角色">
            <el-option
              v-for="option in roleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance } from 'element-plus';

interface User {
  id?: number;
  username: string;
  email: string;
  password?: string;
  roles: string[];
}

const userList = ref<User[]>([]);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const userFormRef = ref<FormInstance>();

const userForm = ref<User>({
  username: '',
  email: '',
  password: '',
  roles: []
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  roles: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
};

// 添加角色选项
const roleOptions = [
  { label: '管理员', value: 'ROLE_ADMIN' },
  { label: '普通用户', value: 'ROLE_USER' },
  { label: '知识库管理员', value: 'ROLE_KNOWLEDGEMANAGER' }
];

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await fetch('http://localhost:8082/ai/users');
    userList.value = await response.json();
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
  }
};

// 显示添加对话框
const showAddDialog = () => {
  userForm.value = {
    username: '',
    email: '',
    password: '',
    roles: []
  };
  isEditMode.value = false;
  dialogVisible.value = true;
};

// 显示编辑对话框
const showEditDialog = (row: User) => {
  userForm.value = { ...row, password: '' };
  isEditMode.value = true;
  dialogVisible.value = true;
};

// 保存用户
const handleSave = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = isEditMode.value
          ? `http://localhost:8082/ai/users/${userForm.value.id}`
          : 'http://localhost:8082/ai/users';
        
        const method = isEditMode.value ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(userForm.value)
        });

        if (!response.ok) {
          throw new Error('保存失败');
        }

        ElMessage.success(isEditMode.value ? '更新成功' : '添加成功');
        dialogVisible.value = false;
        loadUsers();
      } catch (error) {
        console.error('保存失败:', error);
        ElMessage.error('保存失败');
      }
    }
  });
};

// 删除用户
const handleDelete = async (row: User) => {
  if (!row.id) return;
  
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
      type: 'warning'
    });
    
    const response = await fetch(`http://localhost:8082/ai/users/${row.id}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      throw new Error('删除失败');
    }

    ElMessage.success('删除成功');
    loadUsers();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.user-list {
  margin-top: 20px;
}

.role-tag {
  margin-right: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 