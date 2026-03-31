<script setup lang="ts">
import { ref, computed } from 'vue'
import type { H2HStats } from '@/services/statisticsService'

const props = defineProps<{
  h2hRecords: H2HStats[]
}>()

const sortKey = ref<keyof H2HStats | null>(null)
const sortOrder = ref<'asc' | 'desc'>('asc')

function toggleSort(key: keyof H2HStats) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
}

const sortedRecords = computed(() => {
  if (!sortKey.value) return props.h2hRecords

  return [...props.h2hRecords].sort((a, b) => {
    const aVal = a[sortKey.value!]
    const bVal = b[sortKey.value!]
    const modifier = sortOrder.value === 'asc' ? 1 : -1
    
    if (typeof aVal === 'string' && typeof bVal === 'string') {
      return aVal.localeCompare(bVal) * modifier
    }
    
    return ((aVal as number) - (bVal as number)) * modifier
  })
})
</script>

<template>
  <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
    <div class="p-6 border-b border-gray-100 flex justify-between items-center">
      <h2 class="text-lg font-black text-gray-900 uppercase tracking-tighter">Head-to-Head Analytics</h2>
      <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">
        {{ h2hRecords.length }} Opponents
      </span>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-left min-w-[600px]">
        <thead>
          <tr class="bg-gray-50 border-b border-gray-100">
            <th 
              @click="toggleSort('opponentName')"
              :aria-sort="sortKey === 'opponentName' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'"
              class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest cursor-pointer hover:text-gray-900 transition-colors"
            >
              Opponent
              <span v-if="sortKey === 'opponentName'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th 
              @click="toggleSort('matches')"
              :aria-sort="sortKey === 'matches' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'"
              class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right cursor-pointer hover:text-gray-900 transition-colors"
            >
              Matches
              <span v-if="sortKey === 'matches'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th 
              @click="toggleSort('wins')"
              :aria-sort="sortKey === 'wins' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'"
              class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right cursor-pointer hover:text-gray-900 transition-colors"
            >
              Wins
              <span v-if="sortKey === 'wins'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th 
              @click="toggleSort('losses')"
              :aria-sort="sortKey === 'losses' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'"
              class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right cursor-pointer hover:text-gray-900 transition-colors"
            >
              Losses
              <span v-if="sortKey === 'losses'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th 
              @click="toggleSort('winRate')"
              :aria-sort="sortKey === 'winRate' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'"
              class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest text-right cursor-pointer hover:text-gray-900 transition-colors"
            >
              Win Rate
              <span v-if="sortKey === 'winRate'" class="ml-1">{{ sortOrder === 'asc' ? '↑' : '↓' }}</span>
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="record in sortedRecords" :key="record.opponentId" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-6 py-4 font-bold text-gray-900">{{ record.opponentName }}</td>
            <td class="px-6 py-4 text-right text-gray-600 font-medium">{{ record.matches }}</td>
            <td class="px-6 py-4 text-right text-green-600 font-bold">{{ record.wins }}</td>
            <td class="px-6 py-4 text-right text-red-600 font-bold">{{ record.losses }}</td>
            <td class="px-6 py-4 text-right">
              <span class="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-black">
                {{ record.winRate.toFixed(1) }}%
              </span>
            </td>
          </tr>
          <tr v-if="h2hRecords.length === 0">
            <td colspan="5" class="px-6 py-20 text-center text-gray-400 font-medium italic">
              No head-to-head records yet.
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
