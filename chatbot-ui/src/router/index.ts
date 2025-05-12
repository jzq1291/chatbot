import { createRouter, createWebHistory } from 'vue-router'
import KnowledgeManagement from '@/views/KnowledgeManagement.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/ChatView.vue')
    },
    {
      path: '/knowledge',
      name: 'knowledge',
      component: () => import('../views/KnowledgeManagement.vue')
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UserManagement.vue')
    }
  ]
})

export default router 