<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100 transition-colors">
    <NavBar v-if="userStore.isLoggedIn" />
    <main :class="userStore.isLoggedIn ? 'max-w-6xl mx-auto px-4 py-6' : ''">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import NavBar from './components/NavBar.vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.token) userStore.fetchUser()
})
</script>
