<script setup lang="ts">
import { onMounted, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useInsightStore } from '../stores/useInsightStore'
import InsightCard from './InsightCard.vue'

const props = defineProps<{
  playerId?: string
}>()

const { t } = useI18n()
const insightStore = useInsightStore()

onMounted(() => {
  insightStore.fetchInsights(props.playerId)
})

watch(
  () => props.playerId,
  (newId) => {
    insightStore.fetchInsights(newId)
  }
)

const hasInsights = computed(() => {
  const list = insightStore.topInsights
  if (list.length === 0) return false
  if (list.length === 1 && list[0]?.type === 'INSUFFICIENT_DATA') return false
  return true
})

const starterInsight = computed(() => {
  const list = insightStore.topInsights
  return list.find((i) => i.type === 'INSUFFICIENT_DATA') || null
})
</script>

<template>
  <section class="mt-6 mb-8" data-testid="insights-section">
    <div class="flex items-center justify-between gap-2 mb-4">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-ch-primary text-2xl">insights</span>
        <h3 class="text-lg font-bold text-ch-text-bright">
          {{ t('insights.title') }}
        </h3>
      </div>
    </div>

    <div v-if="insightStore.isLoading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="i in 2"
        :key="i"
        class="p-4 rounded-xl border border-ch-border bg-ch-surface-card animate-pulse h-32"
      ></div>
    </div>

    <div
      v-else-if="!hasInsights"
      class="p-6 rounded-xl border border-ch-border bg-ch-surface-card text-center flex flex-col items-center justify-center gap-2"
      data-testid="insight-empty-state"
    >
      <span class="material-symbols-outlined text-3xl text-ch-text-subtle">lightbulb</span>
      <p class="text-sm text-ch-text-secondary max-w-md">
        {{ starterInsight ? t('insights.empty') : t('insights.empty') }}
      </p>
    </div>

    <div
      v-else
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
      data-testid="insights-grid"
    >
      <InsightCard
        v-for="insight in insightStore.topInsights"
        :key="insight.id"
        :insight="insight"
      />
    </div>
  </section>
</template>
