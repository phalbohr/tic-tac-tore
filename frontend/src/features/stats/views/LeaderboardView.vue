<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLeaderboard, type LeaderboardParams } from '@/services/statisticsService'

const entries = ref<import('@/services/statisticsService').LeaderboardEntry[]>([])
const totalPages = ref(0)
const totalElements = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const isLoading = ref(false)

const filters = ref({
  matchFormat: '',
  matchType: '',
  period: 'ALL_TIME',
})

async function loadLeaderboard() {
  isLoading.value = true
  try {
    const params: LeaderboardParams = {
      page: currentPage.value,
      size: pageSize.value,
      minMatches: 5,
      ruleSystem: filters.value.matchFormat as 'STANDARD' | 'RANDOM' | undefined,
      matchType: filters.value.matchType as '1v1' | '2v2' | undefined,
      period: filters.value.period as 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'ALL_TIME',
    }
    const response = await getLeaderboard(params)
    entries.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (e) {
    console.error('Failed to load leaderboard', e)
  } finally {
    isLoading.value = false
  }
}

function setPage(page: number) {
  currentPage.value = page
  loadLeaderboard()
}

onMounted(() => {
  loadLeaderboard()
})
</script>

<template>
  <div class="w-full flex flex-col gap-4 items-center p-4">
    <h2 class="text-on-surface font-headline font-bold text-xl">Leaderboard</h2>

    <div class="flex flex-wrap gap-2 w-full max-w-2xl">
      <select v-model="filters.matchFormat" @change="setPage(0)" class="bg-surface-container-low text-on-surface rounded-lg px-3 py-2 border border-outline">
        <option value="">All Formats</option>
        <option value="STANDARD">Standard</option>
        <option value="RANDOM">Random</option>
      </select>
      <select v-model="filters.matchType" @change="setPage(0)" class="bg-surface-container-low text-on-surface rounded-lg px-3 py-2 border border-outline">
        <option value="">All Types</option>
        <option value="1v1">1v1</option>
        <option value="2v2">2v2</option>
      </select>
      <select v-model="filters.period" @change="setPage(0)" class="bg-surface-container-low text-on-surface rounded-lg px-3 py-2 border border-outline">
        <option value="ALL_TIME">All Time</option>
        <option value="WEEKLY">Weekly</option>
        <option value="MONTHLY">Monthly</option>
        <option value="YEARLY">Yearly</option>
      </select>
    </div>

    <div v-if="isLoading" class="animate-pulse w-full max-w-2xl space-y-2">
      <div v-for="i in 5" :key="i" class="h-12 bg-surface-container-highest rounded-lg"></div>
    </div>

    <div v-else-if="entries.length === 0" class="text-on-surface-variant text-center py-8">
      No players match the current filters.
    </div>

    <div v-else class="w-full max-w-2xl overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="border-b border-outline text-on-surface-variant text-sm">
            <th class="py-2 px-3">Rank</th>
            <th class="py-2 px-3">Player</th>
            <th class="py-2 px-3 text-right">Matches</th>
            <th class="py-2 px-3 text-right">Wins</th>
            <th class="py-2 px-3 text-right">Losses</th>
            <th class="py-2 px-3 text-right">Win Rate</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(entry, index) in entries" :key="entry.playerId" class="border-b border-outline/30 hover:bg-surface-container-low/50">
            <td class="py-3 px-3 font-bold text-primary">{{ currentPage * pageSize + index + 1 }}</td>
            <td class="py-3 px-3 font-medium">{{ entry.playerName }}</td>
            <td class="py-3 px-3 text-right">{{ entry.totalMatches }}</td>
            <td class="py-3 px-3 text-right text-green-400">{{ entry.wins }}</td>
            <td class="py-3 px-3 text-right text-red-400">{{ entry.losses }}</td>
            <td class="py-3 px-3 text-right">{{ (entry.winRate * 100).toFixed(1) }}%</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="totalPages > 1" class="flex gap-2">
      <button
        :disabled="currentPage === 0"
        @click="setPage(currentPage - 1)"
        class="px-4 py-2 bg-surface-container-low text-on-surface rounded-lg disabled:opacity-50"
      >
        Previous
      </button>
      <span class="text-on-surface-variant self-center">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
      <button
        :disabled="currentPage >= totalPages - 1"
        @click="setPage(currentPage + 1)"
        class="px-4 py-2 bg-surface-container-low text-on-surface rounded-lg disabled:opacity-50"
      >
        Next
      </button>
    </div>
  </div>
</template>
