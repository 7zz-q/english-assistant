<template>
  <button @click="theme.toggle()"
    class="relative w-14 h-7 rounded-full transition-colors duration-300 focus:outline-none"
    :class="theme.isDark ? 'bg-gray-600' : 'bg-yellow-400'"
    :title="theme.isDark ? '切换白天模式' : '切换夜间模式'">
    <span class="absolute top-0.5 left-0.5 w-6 h-6 rounded-full bg-white shadow-md transition-transform duration-300 flex items-center justify-center text-xs"
      :class="theme.isDark ? 'translate-x-7' : 'translate-x-0'">
      {{ theme.isDark ? '🌙' : '☀️' }}
    </span>
  </button>
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
const theme = useThemeStore()
</script>
