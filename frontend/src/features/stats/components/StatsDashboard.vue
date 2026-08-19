<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useStatsStore } from '../stores/useStatsStore'
import H2HCrossTabMatrix from './H2HCrossTabMatrix.vue'
import TeamStatsView from './TeamStatsView.vue'

const route = useRoute()
const statsStore = useStatsStore()
const { t } = useI18n()

const currentTab = computed<'personal' | 'h2h' | 'teams'>(() => {
  const t = route?.query?.tab
  if (t === 'h2h') return 'h2h'
  if (t === 'teams') return 'teams'
  return 'personal'
})
</script>

<template>
  <div class="w-full flex flex-col gap-4 items-center">
    <!-- H2H Tab View -->
    <div v-if="currentTab === 'h2h'" class="w-full">
      <H2HCrossTabMatrix :opponent-id="(route.query.opponentId as string) || undefined" />
    </div>

    <!-- Teams Tab View -->
    <div v-else-if="currentTab === 'teams'" class="w-full">
      <TeamStatsView />
    </div>

    <!-- Personal Stats Tab View (Default) -->
    <div v-else class="w-full max-w-4xl mx-auto flex flex-col gap-4">
      <div v-if="statsStore.stats" class="w-full bg-surface-container-low p-4 rounded-xl shadow-md">
        <h3 class="text-on-surface font-headline font-bold text-lg mb-2">{{ t('stats.myStatistics', 'My Statistics') }}</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
            <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">{{ t('stats.overall', 'Overall') }}</span>
            <span class="text-2xl font-bold text-primary">{{ statsStore.stats.overall.matches }}</span>
            <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.overall.wins }} L: {{ statsStore.stats.overall.losses }}</span>
            <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
              <div class="ch-stat-bar-fill h-full rounded bg-primary" :style="{ width: Math.min((statsStore.stats.overall.winRate ?? 0), 100) + '%' }"></div>
            </div>
            <span class="text-xs font-bold text-primary mt-1">{{ (statsStore.stats.overall.winRate ?? 0).toFixed(1) }}%</span>
          </div>
          <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
            <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">{{ t('stats.attacker', 'Attacker') }}</span>
            <span class="text-2xl font-bold text-secondary">{{ statsStore.stats.attacker.matches }}</span>
            <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.attacker.wins }} L: {{ statsStore.stats.attacker.losses }}</span>
            <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
              <div class="ch-stat-bar-fill h-full rounded bg-secondary" :style="{ width: Math.min((statsStore.stats.attacker.winRate ?? 0), 100) + '%' }"></div>
            </div>
            <span class="text-xs font-bold text-secondary mt-1">{{ (statsStore.stats.attacker.winRate ?? 0).toFixed(1) }}%</span>
          </div>
          <div class="flex flex-col bg-surface-container-highest p-3 rounded-lg text-center">
            <span class="text-xs text-on-surface-variant uppercase tracking-wider font-semibold">{{ t('stats.defender', 'Defender') }}</span>
            <span class="text-2xl font-bold text-primary">{{ statsStore.stats.defender.matches }}</span>
            <span class="text-xs text-on-surface-variant">W: {{ statsStore.stats.defender.wins }} L: {{ statsStore.stats.defender.losses }}</span>
            <div class="ch-stat-bar-bg w-full h-2 bg-surface-container-low rounded mt-2">
              <div class="ch-stat-bar-fill h-full rounded bg-primary" :style="{ width: Math.min((statsStore.stats.defender.winRate ?? 0), 100) + '%' }"></div>
            </div>
            <span class="text-xs font-bold text-primary mt-1">{{ (statsStore.stats.defender.winRate ?? 0).toFixed(1) }}%</span>
          </div>
        </div>
        <div v-if="statsStore.isDemoModeEnabled" class="mt-3 text-center">
          <span class="text-[10px] text-orange-400 font-headline uppercase tracking-widest font-bold">{{ t('stats.demoDataActive', 'Demo Data Active') }}</span>
        </div>
        <div class="mt-4 flex justify-between items-center">
          <RouterLink
            to="/statistics?tab=h2h"
            class="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
          >
            {{ t('h2h.title', 'Head-to-Head') }} &rarr;
          </RouterLink>
          <RouterLink
            to="/statistics/teams"
            class="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
          >
            {{ t('teamStats.title', 'Team Statistics') }} &rarr;
          </RouterLink>
        </div>
      </div>
      <div v-else-if="statsStore.isLoading" class="animate-pulse flex flex-col items-center w-full gap-4">
        <div class="h-32 w-full bg-surface-container-highest rounded-xl"></div>
      </div>
      <div v-else class="text-on-surface-variant text-sm italic">
        {{ t('stats.errorLoading', 'Unable to load statistics.') }}
      </div>
    </div>
  </div>
</template>
