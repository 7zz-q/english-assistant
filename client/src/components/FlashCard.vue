<template>
  <div class="bg-white dark:bg-gray-900 rounded-xl p-8 w-full max-w-md shadow-2xl">
    <div class="flex items-center justify-between mb-6">
      <span class="text-sm text-gray-400 dark:text-gray-500">{{ current }} / {{ total }}</span>
      <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 text-lg">✕</button>
    </div>

    <div v-if="!flipped" class="text-center py-10 cursor-pointer" @click="flipped = true">
      <div class="text-5xl font-bold text-gray-800 dark:text-gray-100 mb-4">{{ word.word }}</div>
      <div class="text-gray-400 dark:text-gray-500 text-sm">点击翻转查看释义</div>
    </div>

    <div v-else class="text-center py-6">
      <div class="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-2">{{ word.word }}</div>
      <div class="text-lg text-gray-500 dark:text-gray-400 mb-1">{{ word.phonetic }}</div>
      <div class="text-xl font-semibold text-blue-600 dark:text-blue-400 mb-4">{{ word.meaning }}</div>
      <div v-if="word.example" class="text-sm italic text-gray-500 dark:text-gray-400 bg-gray-50 dark:bg-gray-800 rounded-lg p-3 mb-6">
        📖 {{ word.example }}
      </div>

      <div class="text-sm text-gray-500 dark:text-gray-400 mb-3">你对这个单词的掌握程度：</div>
      <div class="grid grid-cols-2 gap-2">
        <button @click="rate(0)" class="py-3 px-4 rounded-lg bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 font-medium hover:bg-red-100 dark:hover:bg-red-900/50 transition text-sm">😰 完全忘了</button>
        <button @click="rate(1)" class="py-3 px-4 rounded-lg bg-orange-50 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 font-medium hover:bg-orange-100 dark:hover:bg-orange-900/50 transition text-sm">🤔 有点模糊</button>
        <button @click="rate(2)" class="py-3 px-4 rounded-lg bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 font-medium hover:bg-yellow-100 dark:hover:bg-yellow-900/50 transition text-sm">🙂 还记得</button>
        <button @click="rate(3)" class="py-3 px-4 rounded-lg bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400 font-medium hover:bg-green-100 dark:hover:bg-green-900/50 transition text-sm">😎 很熟练</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ word: Object, total: Number, current: Number })
const emit = defineEmits(['rate', 'close'])
const flipped = ref(false)
function rate(quality) { emit('rate', quality); flipped.value = false }
</script>
