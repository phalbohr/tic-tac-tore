<script setup lang="ts">
import { useStatsStore } from '../stores/useStatsStore'
import { onMounted } from 'vue'

const statsStore = useStatsStore()

onMounted(() => {
  statsStore.fetchStats()
})
</script>

<template>
  <div class="w-full flex flex-col gap-4 items-center">
    <div v-if="statsStore.stats" class="w-full bg-surface-container-low p-4 rounded-xl shadow-md">
      <h3 class="text-on-surface font-headline font-bold text-lg mb-2">My Statistics</h3>
      <div class="grid grid-cols-2 gap-4">
        <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
          <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">Matches</span>
          <span class="text-2xl font-bold text-primary">{{ statsStore.stats.overall.matches }}</span>
        </div>
        <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
          <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">Win Rate</span>
          <span class="text-2xl font-bold text-primary">{{ statsStore.stats.overall.winRate }}%</span>
        </div>
      </div>
      <div v-if="statsStore.isDemoModeEnabled" class="mt-3 text-center">
        <span class="text-[10px] text-orange-400 font-headline uppercase tracking-widest font-bold">Demo Data Active</span>
      </div>
    </div>
    <div v-else-if="statsStore.isLoading" class="animate-pulse flex flex-col items-center w-full gap-4">
      <div class="h-32 w-full bg-surface-container-highest rounded-xl"></div>
    </div>
    <div v-else class="text-on-surface-variant text-sm italic">
      Unable to load statistics.
    </div>
  </div>
</template>
