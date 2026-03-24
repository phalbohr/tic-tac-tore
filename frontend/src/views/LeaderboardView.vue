<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getLeaderboard, getPersonalStats, getH2HStats, type LeaderboardEntry, type LeaderboardType, type LeaderboardParams, type TimePeriod, type PlayerStats, type H2HStats } from '@/services/statisticsService'
import PlayerStatsSummary from '@/components/PlayerStatsSummary.vue'
import H2HAnalyticsTable from '@/components/H2HAnalyticsTable.vue'

const authStore = useAuthStore()

const leaderboard = ref<LeaderboardEntry[]>([])
const personalStats = ref<PlayerStats | null>(null)
const h2hRecords = ref<H2HStats[]>([])
const showPersonalStats = ref(false)
const loading = ref(true)
const error = ref<string | null>(null)

const type = ref<LeaderboardType>('OVERALL')
const period = ref<TimePeriod>('ALL_TIME')
const minMatches = ref(0)
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)

const PAGE_SIZES = [10, 20, 50, 100]
const MIN_MATCHES_OPTIONS = [0, 10, 20, 50, 100]

let controller: AbortController | null = null

async function fetchData() {
  if (controller) controller.abort()
  controller = new AbortController()

  loading.value = true
  error.value = null
  try {
    if (showPersonalStats.value) {
      const statsPromise = getPersonalStats({
        period: period.value,
        token: authStore.token || undefined,
        signal: controller.signal
      })
      const h2hPromise = getH2HStats({
        period: period.value,
        token: authStore.token || undefined,
        signal: controller.signal
      })

      const [statsResult, h2hResult] = await Promise.all([statsPromise, h2hPromise])
      personalStats.value = statsResult
      h2hRecords.value = h2hResult
    } else {
      const params: LeaderboardParams = {
        type: type.value,
        period: period.value,
        minMatches: minMatches.value > 0 ? minMatches.value : undefined,
        page: page.value,
        size: size.value,
        signal: controller.signal,
        token: authStore.token || undefined
      }
      const response = await getLeaderboard(params)
      leaderboard.value = response.content || []
      totalPages.value = response.totalPages || 0
    }
  } catch (err: unknown) {
    if (err instanceof Error && err.name === 'AbortError') return
    
    console.error('Fetch error:', err)
    error.value = err instanceof Error ? err.message : 'Unknown error occurred'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (controller) controller.abort()
})

watch([type, period, minMatches, size, showPersonalStats], () => {
  if (page.value !== 0) {
    page.value = 0
  } else {
    fetchData()
  }
})

watch(page, fetchData)

fetchData()

const tabs: { label: string, value: LeaderboardType }[] = [
  { label: 'Overall', value: 'OVERALL' },
  { label: 'Attacker', value: 'ATTACKER' },
  { label: 'Defender', value: 'DEFENDER' }
]

const periods: { label: string, value: TimePeriod }[] = [
  { label: 'All Time', value: 'ALL_TIME' },
  { label: 'Weekly', value: 'WEEKLY' },
  { label: 'Monthly', value: 'MONTHLY' },
  { label: 'Yearly', value: 'YEARLY' }
]
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4 font-sans">
    <div class="max-w-4xl mx-auto">
      <div class="mb-8 bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex justify-between items-center">
        <h1 class="text-2xl font-black text-gray-900 uppercase tracking-tighter">
          Leaderboards
        </h1>
        <router-link to="/" class="text-xs font-bold text-indigo-600 hover:text-indigo-700 uppercase tracking-widest">
          Back to Dashboard
        </router-link>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="md:col-span-2 flex gap-2 bg-white p-2 rounded-2xl shadow-sm border border-gray-100 overflow-x-auto">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            @click="type = tab.value; showPersonalStats = false"
            :class="[
              'flex-1 px-4 py-3 rounded-xl font-bold text-sm transition-all whitespace-nowrap',
              !showPersonalStats && type === tab.value 
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200' 
                : 'text-gray-500 hover:bg-gray-50'
            ]"
          >
            {{ tab.label }}
          </button>
          <button
            v-if="authStore.isAuthenticated"
            @click="showPersonalStats = true"
            :class="[
              'flex-1 px-4 py-3 rounded-xl font-bold text-sm transition-all whitespace-nowrap',
              showPersonalStats
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200' 
                : 'text-gray-500 hover:bg-gray-50'
            ]"
          >
            My Stats
          </button>
        </div>

        <div class="bg-white p-2 rounded-2xl shadow-sm border border-gray-100 flex items-center px-4">
          <label for="period-selector" class="text-[10px] font-black text-gray-400 uppercase tracking-widest mr-3">Period</label>
          <select
            id="period-selector"
            v-model="period"
            class="flex-1 bg-transparent border-none text-sm font-bold text-gray-700 focus:ring-0 cursor-pointer"
          >
            <option v-for="p in periods" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
        </div>
      </div>

      <div v-if="!showPersonalStats" class="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div class="bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 flex items-center gap-3">
          <label for="min-matches-selector" class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Min Matches</label>
          <select
            id="min-matches-selector"
            v-model="minMatches"
            class="bg-gray-50 border border-gray-100 rounded-lg px-2 py-1 text-xs font-bold text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 cursor-pointer"
          >
            <option v-for="opt in MIN_MATCHES_OPTIONS" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>

        <div class="flex items-center gap-3">
          <label for="page-size-selector" class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Page Size</label>
          <select 
            id="page-size-selector"
            v-model="size" 
            class="bg-white border border-gray-100 rounded-xl px-3 py-2 text-xs font-bold text-gray-600 focus:outline-none shadow-sm cursor-pointer"
          >
            <option v-for="ps in PAGE_SIZES" :key="ps" :value="ps">{{ ps }}</option>
          </select>
        </div>
      </div>

      <div v-if="showPersonalStats && personalStats" class="animate-in fade-in duration-300 space-y-8">
        <PlayerStatsSummary :stats="personalStats" />
        <H2HAnalyticsTable :h2hRecords="h2hRecords" />
      </div>
      <div v-else class="relative bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden min-h-[400px]">
        <div 
          v-if="loading" 
          class="absolute inset-0 bg-white/60 backdrop-blur-[1px] flex justify-center items-center z-10 transition-opacity duration-200"
        >
          <div class="flex flex-col items-center gap-3">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
            <span class="text-[10px] font-black text-indigo-600 uppercase tracking-widest">Updating...</span>
          </div>
        </div>

        <div v-if="error" class="p-4 bg-red-50 text-red-600 text-sm font-bold border-b border-red-100 flex justify-between items-center">
          <span>{{ error }}</span>
          <button @click="fetchData" class="px-3 py-1 bg-red-600 text-white rounded-lg text-xs uppercase font-black">Retry</button>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left min-w-[600px]">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-100">
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest">Rank</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest">Player</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right">Matches</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right">Wins</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right">Losses</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right">Win Rate</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="entry in leaderboard" :key="entry.playerId" class="hover:bg-gray-50/50 transition-colors">
                <td class="px-6 py-4">
                  <span :class="[
                    'inline-flex items-center justify-center w-8 h-8 rounded-lg text-xs font-black',
                    entry.rank === 1 ? 'bg-yellow-100 text-yellow-700' : 
                    entry.rank === 2 ? 'bg-gray-100 text-gray-700' : 
                    entry.rank === 3 ? 'bg-orange-100 text-orange-700' : 'text-gray-400'
                  ]">
                    #{{ entry.rank }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <div class="font-bold text-gray-900">{{ entry.playerName }}</div>
                </td>
                <td class="px-6 py-4 text-right text-gray-600 font-medium">{{ entry.totalMatches }}</td>
                <td class="px-6 py-4 text-right text-green-600 font-bold">{{ entry.wins }}</td>
                <td class="px-6 py-4 text-right text-red-600 font-bold">{{ entry.losses }}</td>
                <td class="px-6 py-4 text-right">
                  <span class="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-black">
                    {{ entry.winRate.toFixed(1) }}%
                  </span>
                </td>
              </tr>
              <tr v-if="leaderboard.length === 0 && !loading">
                <td colspan="6" class="px-6 py-20 text-center text-gray-400 font-medium italic">
                  No rankings available yet.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="totalPages > 1" class="p-6 border-t border-gray-100 flex items-center justify-between">
          <div class="flex gap-2">
            <button 
              @click="page--" 
              :disabled="page === 0 || loading"
              class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition-colors"
            >
              Previous
            </button>
            <button 
              @click="page++" 
              :disabled="page >= totalPages - 1 || loading"
              class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition-colors"
            >
              Next
            </button>
          </div>
          <div class="text-xs font-bold text-gray-400 uppercase tracking-widest">
            Page {{ page + 1 }} of {{ totalPages }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
