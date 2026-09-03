import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type PlayerInsight, getPlayerInsights, getMyInsights } from '@/services/insightService'
import { generateDemoInsights } from '../utils/demoDataGenerator'
import { useStatsStore } from './useStatsStore'

export const useInsightStore = defineStore('insight', () => {
  const statsStore = useStatsStore()

  const insights = ref<PlayerInsight[]>([])
  const realInsights = ref<PlayerInsight[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const dismissedCelebrationIds = ref<Set<string>>(new Set())

  const shouldShowDemoData = computed(() => {
    return statsStore.shouldShowDemoData
  })

  const topInsights = computed<PlayerInsight[]>(() => {
    if (
      shouldShowDemoData.value &&
      (realInsights.value.length === 0 ||
        (realInsights.value.length === 1 && realInsights.value[0]?.type === 'INSUFFICIENT_DATA'))
    ) {
      return generateDemoInsights().slice(0, 5)
    }
    return insights.value.slice(0, 5)
  })

  const isCelebrationActive = ref(true)

  const latestCelebrationInsight = computed<PlayerInsight | null>(() => {
    if (!isCelebrationActive.value) return null
    const list = realInsights.value
    const celebrationCandidate = list.find(
      (insight) =>
        insight.importance === 'HIGH' &&
        insight.type !== 'INSUFFICIENT_DATA' &&
        !dismissedCelebrationIds.value.has(insight.id),
    )
    return celebrationCandidate || null
  })

  async function fetchInsights(playerId?: string) {
    isLoading.value = true
    error.value = null

    try {
      const response = playerId ? await getPlayerInsights(playerId) : await getMyInsights()

      realInsights.value = response.insights || []
      insights.value = response.insights || []
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to load insights'
      realInsights.value = []
      insights.value = []
    } finally {
      isLoading.value = false
    }
  }

  function dismissCelebration(id: string) {
    dismissedCelebrationIds.value.add(id)
  }

  function reset() {
    insights.value = []
    realInsights.value = []
    isLoading.value = false
    error.value = null
    dismissedCelebrationIds.value.clear()
  }

  return {
    insights,
    realInsights,
    isLoading,
    error,
    topInsights,
    latestCelebrationInsight,
    shouldShowDemoData,
    fetchInsights,
    dismissCelebration,
    reset,
  }
})
