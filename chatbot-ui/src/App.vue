<script setup lang="ts">
import { ref } from 'vue'
import { ChatDotRound, Collection, Fold, Expand, User } from '@element-plus/icons-vue'

const isCollapse = ref(true)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="sidebar-header">
        <el-icon class="toggle-icon" @click="toggleSidebar">
          <component :is="isCollapse ? Expand : Fold" />
        </el-icon>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        class="el-menu-vertical"
        :collapse="isCollapse"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>聊天</template>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          <template #title>知识库管理</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.sidebar {
  transition: width 0.3s;
  background-color: #f5f7fa;
  border-right: 1px solid #e6e6e6;
}

.sidebar-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e6e6e6;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
}

.toggle-icon:hover {
  color: #409EFF;
}

.el-menu-vertical {
  height: calc(100% - 50px);
  border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}
</style>
