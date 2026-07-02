<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="page-title mb-0">📚 词库</h1>
      <span class="text-sm text-gray-400 dark:text-gray-500">{{ total.toLocaleString() }} 个单词</span>
    </div>

    <!-- 筛选栏 -->
    <div class="card mb-4">
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex flex-wrap gap-2">
          <button v-for="lv in levels" :key="lv.level"
            @click="filter.level = lv.level; loadPage(1)"
            class="px-3 py-1.5 rounded-full text-sm font-medium transition"
            :class="filter.level === lv.level ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            {{ lv.label }} <span class="opacity-70 text-xs">({{ lv.count }})</span>
          </button>
          <button @click="filter.level = ''; loadPage(1)"
            class="px-3 py-1.5 rounded-full text-sm font-medium transition"
            :class="!filter.level ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            全部 ({{ total }})
          </button>
        </div>
        <input v-model="filter.search" @keyup.enter="loadPage(1)" class="input-field w-48 py-1.5 text-sm" placeholder="搜索单词..." />
        <button @click="loadPage(1)" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">搜索</button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="flex items-center gap-3 mb-3" v-if="selected.size > 0">
      <span class="text-sm text-gray-600 dark:text-gray-400">已选 {{ selected.size }} 个</span>
      <button @click="importSelected" class="btn-primary text-sm py-1.5" :disabled="importing">{{ importing ? '导入中...' : `📥 导入 (${selected.size})` }}</button>
      <button @click="importAll" class="bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 px-4 py-1.5 rounded-lg text-sm font-medium border border-green-200 dark:border-green-800 hover:bg-green-100 dark:hover:bg-green-900/50 transition">
        📦 导入本页全部 ({{ pageWords.filter(w => !w.imported).length }})
      </button>
      <button @click="selected.clear()" class="text-sm text-gray-400 dark:text-gray-500 hover:text-gray-600">取消</button>
    </div>

    <!-- 单词表格 -->
    <div class="card overflow-hidden !p-0">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400 text-xs uppercase">
              <th class="px-4 py-3 w-10"><input type="checkbox" @change="toggleAll" :checked="allSelected" /></th>
              <th class="px-4 py-3">单词</th>
              <th class="px-4 py-3 hidden md:table-cell">释义</th>
              <th class="px-4 py-3 w-24">等级</th>
              <th class="px-4 py-3 w-24 text-center">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="w in pageWords" :key="w.id"
              class="border-t border-gray-50 dark:border-gray-700 hover:bg-blue-50/30 dark:hover:bg-blue-900/20 transition"
              :class="{ 'opacity-50 bg-gray-50 dark:bg-gray-800/50': w.imported }">
              <td class="px-4 py-2.5"><input type="checkbox" :checked="selected.has(w.id) || w.imported" :disabled="w.imported" @change="toggleWord(w)" /></td>
              <td class="px-4 py-2.5 font-medium text-gray-800 dark:text-gray-100">{{ w.word }}</td>
              <td class="px-4 py-2.5 text-gray-500 dark:text-gray-400 hidden md:table-cell max-w-xs truncate">{{ w.meaning }}</td>
              <td class="px-4 py-2.5"><span class="text-xs px-2 py-0.5 rounded-full" :class="levelBadge(w.level)">{{ levelLabel(w.level) }}</span></td>
              <td class="px-4 py-2.5 text-center">
                <span v-if="w.imported" class="text-green-500 text-xs">✓ 已导入</span>
                <span v-else class="text-gray-300 dark:text-gray-600 text-xs">未导入</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="pageWords.length === 0" class="text-center text-gray-400 dark:text-gray-500 py-16">📭 没有匹配的单词</div>

      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 p-4 border-t dark:border-gray-700">
        <button @click="loadPage(page - 1)" :disabled="page <= 1" class="px-3 py-1.5 rounded text-sm border dark:border-gray-600 disabled:opacity-30 hover:bg-gray-50 dark:hover:bg-gray-800">上一页</button>
        <span class="text-sm text-gray-500 dark:text-gray-400 px-2">{{ page }} / {{ totalPages }}</span>
        <button @click="loadPage(page + 1)" :disabled="page >= totalPages" class="px-3 py-1.5 rounded text-sm border dark:border-gray-600 disabled:opacity-30 hover:bg-gray-50 dark:hover:bg-gray-800">下一页</button>
      </div>
    </div>

    <!-- 导入结果 -->
    <div v-if="result" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center" @click.self="result = null">
      <div class="bg-white dark:bg-gray-900 rounded-xl p-6 w-full max-w-sm shadow-xl text-center">
        <div class="text-4xl mb-3">✅</div>
        <div class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-1">导入完成</div>
        <div class="text-sm text-gray-500 dark:text-gray-400">成功导入 <span class="font-bold text-green-600">{{ result.imported }}</span> 个
          <span v-if="result.skipped > 0">，跳过 {{ result.skipped }} 个（已存在）</span></div>
        <button @click="result = null" class="btn-primary mt-4 w-full">知道了</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wordAPI } from '../api'

const words = ref([])
const total = ref(0)
const page = ref(1)
const limit = 100
const selected = ref(new Set())
const importing = ref(false)
const result = ref(null)
const levels = ref([])
const filter = ref({ level: '', search: '' })

onMounted(() => { loadPage(); loadLevels() })

const pageWords = computed(() => words.value)
const totalPages = computed(() => Math.ceil(total.value / limit) || 1)
const allSelected = computed(() => {
  const notImported = pageWords.value.filter(w => !w.imported)
  return notImported.length > 0 && notImported.every(w => selected.value.has(w.id))
})

async function loadPage(p) {
  if (p) page.value = p
  try { const { data } = await wordAPI.bank({ level: filter.value.level || undefined, search: filter.value.search || undefined, page: page.value, limit }); words.value = data.words; total.value = data.total } catch { /* */ }
}
async function loadLevels() {
  try { const { data } = await wordAPI.bankLevels(); const map = { cet4: 'CET-4', cet6: 'CET-6', cet46: '四六级', toefl: 'TOEFL', gre: 'GRE', tem4: '专四', tem8: '专八', junior: '中考' }; levels.value = data.stats.map(s => ({ level: s.level, count: s.count, label: map[s.level] || s.level })) } catch { /* */ }
}
function toggleWord(w) {
  if (w.imported) return
  if (selected.value.has(w.id)) selected.value.delete(w.id); else selected.value.add(w.id)
  selected.value = new Set(selected.value)
}
function toggleAll(e) {
  if (e.target.checked) pageWords.value.filter(w => !w.imported).forEach(w => selected.value.add(w.id))
  else pageWords.value.forEach(w => selected.value.delete(w.id))
  selected.value = new Set(selected.value)
}
async function importSelected() {
  importing.value = true
  try { const { data } = await wordAPI.bankImport({ wordIds: [...selected.value] }); result.value = data; selected.value.clear(); loadPage() } catch { /* */ }
  importing.value = false
}
async function importAll() {
  const ids = pageWords.value.filter(w => !w.imported).map(w => w.id)
  if (ids.length === 0) return
  importing.value = true
  try { const { data } = await wordAPI.bankImport({ wordIds: ids }); result.value = data; loadPage() } catch { /* */ }
  importing.value = false
}
function levelLabel(lv) { const m = { cet4: 'CET-4', cet6: 'CET-6', cet46: '四六级', toefl: 'TOEFL', gre: 'GRE', tem4: '专四', tem8: '专八', junior: '中考' }; return m[lv] || lv }
function levelBadge(lv) { const m = { cet4: 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400', cet6: 'bg-purple-100 dark:bg-purple-900/40 text-purple-700 dark:text-purple-400', cet46: 'bg-indigo-100 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-400', toefl: 'bg-orange-100 dark:bg-orange-900/40 text-orange-700 dark:text-orange-400', gre: 'bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-400', tem4: 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-700 dark:text-yellow-400', tem8: 'bg-pink-100 dark:bg-pink-900/40 text-pink-700 dark:text-pink-400', junior: 'bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-400' }; return m[lv] || 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400' }
</script>
