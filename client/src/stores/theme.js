import { defineStore } from 'pinia'
import { ref, watchEffect } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const saved = localStorage.getItem('theme')
  const isDark = ref(saved === 'dark')

  function toggle() {
    isDark.value = !isDark.value
  }

  watchEffect(() => {
    const root = document.documentElement
    if (isDark.value) {
      root.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      root.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  })

  // init
  if (saved === 'dark') {
    document.documentElement.classList.add('dark')
  }

  return { isDark, toggle }
})
