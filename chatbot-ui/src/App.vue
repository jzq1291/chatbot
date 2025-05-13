<script setup lang="ts">
import { ref } from 'vue'
import { ChatDotRound, Collection, Fold, Expand, User } from '@element-plus/icons-vue'

const isCollapse = ref(true)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <router-view></router-view>
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
</style>
