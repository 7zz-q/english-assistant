<template>
  <div>
    <h1 class="page-title">📊 学习概览</h1>

    <!-- 词库选择 + 统计卡片 -->
    <div class="flex items-center gap-3 mb-4">
      <span class="text-sm text-gray-500 dark:text-gray-400 shrink-0">查看：</span>
      <select v-model="cardLevel" class="input-field w-auto py-1.5 text-sm">
        <option value="">📝 我的词本</option>
        <option v-for="lv in allLevels" :key="lv.level" :value="lv.level">{{ lv.label }}</option>
      </select>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div class="card text-center">
        <div class="text-3xl font-bold text-blue-600 dark:text-blue-400">{{ cardStats.total }}</div>
        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">词汇总量</div>
      </div>
      <div class="card text-center">
        <div class="text-3xl font-bold text-green-600 dark:text-green-400">{{ cardStats.mastered }}</div>
        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">已掌握</div>
      </div>
      <div class="card text-center">
        <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-400">{{ cardStats.learning }}</div>
        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">学习中</div>
      </div>
      <div class="card text-center">
        <div class="text-3xl font-bold text-orange-600 dark:text-orange-400">{{ streak }}🔥</div>
        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">连续打卡</div>
      </div>
    </div>

    <!-- ====== 我的词本学习进度 ====== -->
    <div class="card mb-8">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100">📚 我的词本进度</h2>
        <select v-model="dashboardLevel" class="input-field w-auto py-1.5 text-sm">
          <option value="">全部词库</option>
          <option v-for="lv in allLevels" :key="lv.level" :value="lv.level">{{ lv.label }}</option>
        </select>
      </div>

      <div v-if="levelStats.length === 0" class="text-center text-gray-400 dark:text-gray-500 py-8">
        加载中...
      </div>
      <div v-else class="space-y-4">
        <div v-for="lv in levelStats" :key="lv.level" class="space-y-2">
          <div class="flex items-center justify-between text-sm">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-gray-700 dark:text-gray-200">{{ lv.label }}</span>
              <span class="text-xs text-gray-400" v-if="lv.total > 0">{{ lv.mastered }}/{{ lv.total }} 已掌握</span>
              <span class="text-xs text-gray-400" v-else>词库 {{ lv.bankTotal }} 词，尚未开始</span>
              <span v-if="lv.learning > 0" class="text-xs text-yellow-500">· {{ lv.learning }} 学习中</span>
              <span v-if="lv.newCount > 0" class="text-xs text-gray-400">· {{ lv.newCount }} 新学</span>
            </div>
            <span class="text-xs font-bold" :class="lv.percent >= 80 ? 'text-green-500' : lv.percent >= 40 ? 'text-yellow-500' : 'text-gray-400'">
              {{ lv.percent }}%
            </span>
          </div>
          <div class="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-4 overflow-hidden">
            <div class="flex h-full">
              <div v-if="lv.mastered > 0" class="h-full bg-green-500 transition-all duration-500"
                :style="{ width: barWidth(lv.mastered, lv.total) }"
                :title="'已掌握: ' + lv.mastered"></div>
              <div v-if="lv.learning > 0" class="h-full bg-blue-400 transition-all duration-500"
                :style="{ width: barWidth(lv.learning, lv.total) }"
                :title="'学习中: ' + lv.learning"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日待复习 -->
    <div class="card mb-8">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100">📅 今日待复习</h2>
        <router-link to="/vocabulary" class="text-sm text-blue-500 hover:underline">去背诵 →</router-link>
      </div>
      <div v-if="reviewWords.length === 0" class="text-center text-gray-400 dark:text-gray-500 py-8">
        暂无待复习单词，去背新词吧～
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div v-for="w in reviewWords.slice(0, 6)" :key="w.id"
          class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <div>
            <span class="font-semibold text-gray-800 dark:text-gray-100">{{ w.word }}</span>
            <span class="text-sm text-gray-500 dark:text-gray-400 ml-2">{{ w.meaning }}</span>
          </div>
          <span class="text-xs text-gray-400">{{ w.review_count }}次</span>
        </div>
      </div>
    </div>

    <!-- 写作水平 -->
    <div class="card mb-8" v-if="overview.totalEssays > 0">
      <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">✍️ 写作水平</h2>
      <div class="flex items-center gap-6">
        <div class="text-5xl font-bold" :class="scoreColor">{{ overview.avgScore }}</div>
        <div>
          <div class="text-sm text-gray-500 dark:text-gray-400 mb-1">作文平均分</div>
          <div class="w-48 bg-gray-200 dark:bg-gray-700 rounded-full h-3">
            <div class="h-3 rounded-full transition-all duration-500" :class="scoreBarColor"
              :style="{ width: (overview.avgScore || 0) + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 30 天热力图 -->
    <div class="card mt-8">
      <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">🔥 近 30 天学习热度</h2>
      <div class="flex flex-wrap gap-1.5">
        <div v-for="day in heatmap" :key="day.date" class="w-3.5 h-3.5 rounded-sm" :class="getHeatClass(day.count)"
          :title="day.date + ': ' + day.count + ' 次学习'"></div>
      </div>
      <div class="flex items-center gap-2 mt-3 text-xs text-gray-400 dark:text-gray-500">
        <span>少</span>
        <div class="w-3 h-3 bg-gray-100 dark:bg-gray-800 rounded-sm"></div>
        <div class="w-3 h-3 bg-green-200 dark:bg-green-900 rounded-sm"></div>
        <div class="w-3 h-3 bg-green-400 dark:bg-green-700 rounded-sm"></div>
        <div class="w-3 h-3 bg-green-600 dark:bg-green-500 rounded-sm"></div>
        <span>多</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { statsAPI, wordAPI } from '../api'

const overview = ref({ totalWords: 0, mastered: 0, totalEssays: 0, avgScore: 0 })
const streak = ref(0)
const reviewWords = ref([])
const dailyStats = ref([])
const levelStats = ref([])
const dashboardLevel = ref('')
const cardLevel = ref('')     // 顶部统计卡片的词库选择
const allLevels = ref([])
const allLevelData = ref([])

onMounted(async () => {
  try {
    const [ov, st, rv, dl, lv] = await Promise.all([
      statsAPI.overview(), statsAPI.streak(), wordAPI.reviewList(), statsAPI.daily(), statsAPI.byLevel(),
    ])
    overview.value = ov.data; streak.value = st.data.streak
    reviewWords.value = rv.data.words; dailyStats.value = dl.data.stats

    const raw = lv.data.levels || []
    allLevelData.value = raw
    allLevels.value = raw.map(l => ({ level: l.level, label: l.label }))
    filterLevelStats()
  } catch { /* */ }
})

function filterLevelStats() {
  const raw = allLevelData.value
  if (!dashboardLevel.value) {
    levelStats.value = raw.map(l => ({ ...l, percent: l.total > 0 ? Math.round(l.mastered / l.total * 100) : 0 }))
  } else {
    const found = raw.find(l => l.level === dashboardLevel.value)
    levelStats.value = found ? [{ ...found, percent: found.total > 0 ? Math.round(found.mastered / found.total * 100) : 0 }] : []
  }
}

const cardStats = computed(() => {
  if (!cardLevel.value) {
    // 我的词本 → 个人总统计
    return {
      total: overview.value.totalWords,
      mastered: overview.value.mastered,
      learning: overview.value.totalWords - overview.value.mastered,
    }
  }
  // 具体某个词库
  const found = allLevelData.value.find(l => l.level === cardLevel.value)
  return {
    total: found ? found.bankTotal : 0,
    mastered: found ? found.mastered : 0,
    learning: found ? (found.learning + found.newCount) : 0,
  }
})

watch(dashboardLevel, () => filterLevelStats())

const heatmap = computed(() => {
  const map = new Map()
  for (let i = 29; i >= 0; i--) {
    const d = new Date(); d.setDate(d.getDate() - i)
    map.set(d.toISOString().slice(0, 10), 0)
  }
  for (const s of dailyStats.value) {
    const count = (s.words_learned || 0) + (s.words_reviewed || 0) + (s.essays_written || 0)
    if (map.has(s.date)) map.set(s.date, count)
  }
  return Array.from(map, ([date, count]) => ({ date, count }))
})

function getHeatClass(count) {
  if (count === 0) return 'bg-gray-100 dark:bg-gray-800'
  if (count <= 2) return 'bg-green-200 dark:bg-green-900'
  if (count <= 5) return 'bg-green-400 dark:bg-green-700'
  return 'bg-green-600 dark:bg-green-500'
}

function barWidth(val, total) { return total > 0 ? Math.max(1, val / total * 100) + '%' : '0%' }

const scoreColor = computed(() => overview.value.avgScore >= 80 ? 'text-green-500' : overview.value.avgScore >= 60 ? 'text-yellow-500' : 'text-red-500')
const scoreBarColor = computed(() => overview.value.avgScore >= 80 ? 'bg-green-500' : overview.value.avgScore >= 60 ? 'bg-yellow-500' : 'bg-red-500')
</script>
