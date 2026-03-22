<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { getLeaderboard, type LeaderboardEntry, type LeaderboardType, type LeaderboardParams } from '@/services/statisticsService'

const leaderboard = ref<LeaderboardEntry[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const type = ref<LeaderboardType>('OVERALL')
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)

let controller: AbortController | null = null

const fetchLeaderboard = async () => {
  if (controller) controller.abort()
  controller = new AbortController()

  loading.value = true
  error.value = null
  try {
    const params: LeaderboardParams = {
      type: type.value,
      page: page.value,
      size: size.value,
      signal: controller.signal
    }
    const response = await getLeaderboard(params)
    leaderboard.value = response.content
    totalPages.value = response.totalPages
  } catch (err: unknown) {
    if (err instanceof Error && err.name === 'AbortError') return
    error.value = err instanceof Error ? err.message : 'Unknown error occurred'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (controller) controller.abort()
})

const setType = (newType: LeaderboardType) => {
  type.value = newType
  page.value = 0
}

watch([type, page, size], fetchLeaderboard)

onMounted(fetchLeaderboard)

const tabs: { label: string, value: LeaderboardType }[] = [
  { label: 'Overall', value: 'OVERALL' },
  { label: 'Attacker', value: 'ATTACKER' },
  { label: 'Defender', value: 'DEFENDER' }
]
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4 font-sans">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8 bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex justify-between items-center">
        <h1 class="text-2xl font-black text-gray-900 uppercase tracking-tighter">
          Leaderboards
        </h1>
        <router-link to="/" class="text-xs font-bold text-indigo-600 hover:text-indigo-700 uppercase tracking-widest">
          Back to Dashboard
        </router-link>
      </div>

      <!-- Tabs -->
      <div class="flex gap-2 mb-6 bg-white p-2 rounded-2xl shadow-sm border border-gray-100 overflow-x-auto">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="setType(tab.value)"
          :class="[
            'px-6 py-3 rounded-xl font-bold text-sm transition-all whitespace-nowrap',
            type === tab.value 
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200' 
              : 'text-gray-500 hover:bg-gray-50'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Content -->
      <div v-if="loading && leaderboard.length === 0" class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>

      <div v-else class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div v-if="error" class="p-4 bg-red-50 text-red-600 text-sm font-bold border-b border-red-100 flex justify-between items-center">
          <span>{{ error }}</span>
          <button @click="fetchLeaderboard" class="px-3 py-1 bg-red-600 text-white rounded-lg text-xs uppercase font-black">Retry</button>
        </div>

        <table class="w-full text-left">
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
            <tr v-if="leaderboard.length === 0">
              <td colspan="6" class="px-6 py-20 text-center text-gray-400 font-medium italic">
                No rankings available yet.
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="p-6 border-t border-gray-100 flex items-center justify-between">
          <div class="flex gap-2">
            <button 
              @click="page--" 
              :disabled="page === 0"
              class="px-4 py-2 border border-gray-200 rounded-xl text-sm font-bold text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition-colors"
            >
              Previous
            </button>
            <button 
              @click="page++" 
              :disabled="page >= totalPages - 1"
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

      <!-- Page Size Selector -->
      <div class="mt-6 flex justify-end items-center gap-4">
        <label class="text-xs font-bold text-gray-400 uppercase tracking-widest">Page Size:</label>
        <select 
          v-model="size" 
          @change="page = 0"
          class="bg-white border border-gray-200 rounded-xl px-4 py-2 text-sm font-bold text-gray-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
        >
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
          <option :value="100">100</option>
        </select>
      </div>
    </div>
  </div>
</template>
