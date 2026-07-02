<template>
  <nav class="bg-white dark:bg-gray-900 shadow-sm border-b border-gray-100 dark:border-gray-700 sticky top-0 z-50 transition-colors">
    <div class="max-w-6xl mx-auto px-4 flex items-center justify-between h-16">
      <router-link to="/dashboard" class="flex items-center gap-2 font-bold text-xl text-blue-600 dark:text-blue-400">
        📚 AI 英语助手
      </router-link>

      <div class="flex items-center gap-1">
        <router-link v-for="item in navItems" :key="item.to" :to="item.to"
          class="px-4 py-2 rounded-lg text-sm font-medium transition"
          :class="$route.path === item.to
            ? 'bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400'
            : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800'">
          {{ item.icon }} {{ item.label }}
        </router-link>
      </div>

      <div class="flex items-center gap-3">
        <ThemeToggle />
        <span class="text-sm text-gray-500 dark:text-gray-400 hidden sm:inline">{{ userStore.username }}</span>
        <button @click="logout" class="text-sm text-gray-400 dark:text-gray-500 hover:text-red-500 transition">退出</button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import ThemeToggle from './ThemeToggle.vue'

const userStore = useUserStore()
const router = useRouter()

const navItems = [
  { to: '/dashboard',  icon: '📊', label: '概览' },
  { to: '/vocabulary', icon: '📝', label: '单词' },
  { to: '/wordbank',   icon: '📚', label: '词库' },
  { to: '/papers',     icon: '📋', label: '真题' },
  { to: '/writing',    icon: '✍️', label: '写作' },
  { to: '/reading',    icon: '📖', label: '阅读' },
]

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>
