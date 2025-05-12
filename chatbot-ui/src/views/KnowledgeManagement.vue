<template>
  <div class="knowledge-management">
    <div class="header">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="showAddDialog">添加知识</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索知识..."
        @input="handleSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="knowledge-list">
      <el-table :data="knowledgeList" style="width: 100%">
        <el-table-column prop="title" label="标题">
          <template #default="{ row }">
            <el-link type="primary" @click="showEditDialog(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加知识对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑知识' : '添加知识'"
      width="50%"
    >
      <el-form :model="newKnowledge" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="newKnowledge.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="newKnowledge.category" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="newKnowledge.content"
            type="textarea"
            :rows="6"
          />
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
import { Search } from '@element-plus/icons-vue';
import { knowledgeApi, type KnowledgeBase } from '@/api/knowledge';

const searchKeyword = ref('');
const knowledgeList = ref<KnowledgeBase[]>([]);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const newKnowledge = ref<KnowledgeBase>({
  title: '',
  content: '',
  category: ''
});

// 加载知识列表
const loadKnowledge = async () => {
  try {
    const response = await knowledgeApi.searchKnowledge('');
    knowledgeList.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('加载知识列表失败:', error);
    ElMessage.error('加载知识列表失败');
    knowledgeList.value = [];
  }
};

// 搜索知识
const handleSearch = async () => {
  try {
    const response = await knowledgeApi.searchKnowledge(searchKeyword.value);
    knowledgeList.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('搜索失败:', error);
    ElMessage.error('搜索失败');
    knowledgeList.value = [];
  }
};

// 显示添加对话框
const showAddDialog = () => {
  newKnowledge.value = {
    title: '',
    content: '',
    category: ''
  };
  isEditMode.value = false;
  dialogVisible.value = true;
};

// 显示编辑对话框
const showEditDialog = (row: KnowledgeBase) => {
  newKnowledge.value = { ...row };
  isEditMode.value = true;
  dialogVisible.value = true;
};

// 添加或编辑知识
const handleSave = async () => {
  try {
    if (isEditMode.value) {
      await knowledgeApi.updateKnowledge(newKnowledge.value);
      ElMessage.success('保存成功');
    } else {
      await knowledgeApi.addKnowledge(newKnowledge.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    loadKnowledge();
  } catch (error) {
    ElMessage.error(isEditMode.value ? '保存失败' : '添加失败');
  }
};

// 删除知识
const handleDelete = async (row: KnowledgeBase) => {
  if (!row.id) return;
  
  try {
    await ElMessageBox.confirm('确定要删除这条知识吗？', '提示', {
      type: 'warning'
    });
    
    await knowledgeApi.deleteKnowledge(row.id);
    ElMessage.success('删除成功');
    loadKnowledge();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  loadKnowledge();
});
</script>

<style scoped>
.knowledge-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.knowledge-list {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 