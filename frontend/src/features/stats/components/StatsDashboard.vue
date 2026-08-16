<script setup lang="ts">
import { useStatsStore } from '../stores/useStatsStore'

const statsStore = useStatsStore()

</script>

<template>
  <div class="w-full flex flex-col gap-4 items-center">
    <div v-if="statsStore.stats" class="w-full bg-surface-container-low p-4 rounded-xl shadow-md">
      <h3 class="text-on-surface font-headline font-bold text-lg mb-2">My Statistics</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
          <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">Overall</span>
          <span class="text-2xl font-bold text-primary">{{ statsStore.stats.overall.matches }}</span>
          <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.overall.wins }} L: {{ statsStore.stats.overall.losses }}</span>
          <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
            <div class="ch-stat-bar-fill h-full rounded bg-primary" :style="{ width: Math.min((statsStore.stats.overall.winRate ?? 0), 100) + '%' }"></div>
          </div>
          <span class="text-xs font-bold text-primary mt-1">{{ (statsStore.stats.overall.winRate ?? 0).toFixed(1) }}%</span>
        </div>
        <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
          <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">Attacker</span>
          <span class="text-2xl font-bold text-secondary">{{ statsStore.stats.attacker.matches }}</span>
          <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.attacker.wins }} L: {{ statsStore.stats.attacker.losses }}</span>
          <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
            <div class="ch-stat-bar-fill h-full rounded bg-secondary" :style="{ width: Math.min((statsStore.stats.attacker.winRate ?? 0), 100) + '%' }"></div>
          </div>
          <span class="text-xs font-bold text-secondary mt-1">{{ (statsStore.stats.attacker.winRate ?? 0).toFixed(1) }}%</span>
        </div>
        <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
          <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">Defender</span>
          <span class="text-2xl font-bold text-primary">{{ statsStore.stats.defender.matches }}</span>
          <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.defender.wins }} L: {{ statsStore.stats.defender.losses }}</span>
          <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
            <div class="ch-stat-bar-fill h-full rounded bg-primary" :style="{ width: Math.min((statsStore.stats.defender.winRate ?? 0), 100) + '%' }"></div>
          </div>
          <span class="text-xs font-bold text-primary mt-1">{{ (statsStore.stats.defender.winRate ?? 0).toFixed(1) }}%</span>
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
