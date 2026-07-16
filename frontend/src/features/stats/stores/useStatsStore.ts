import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { getPersonalStats, type PlayerStats, type PersonalStatsParams } from '@/services/statisticsService'
import { generateDemoData } from '../utils/demoDataGenerator'
import { useAuthStore } from '@/stores/auth'

export const useStatsStore = defineStore('stats', () => {
  const authStore = useAuthStore()
  
  const getDemoModeKey = () => `tictactore.demoModeEnabled_${authStore.profile?.nickname || 'guest'}`

  const stats = ref<PlayerStats | null>(null)
  const realStats = ref<PlayerStats | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const confirmedMatchesCount = ref<number | null>(null)

  const rawDemoModeSetting = ref<string | null>(localStorage.getItem(getDemoModeKey()))

  watch(() => authStore.profile?.nickname, () => {
    rawDemoModeSetting.value = localStorage.getItem(getDemoModeKey())
  })

  const shouldShowDemoData = computed(() => {
    const isExplicitlyDisabled = rawDemoModeSetting.value === 'false'
    const isExplicitlyEnabled = rawDemoModeSetting.value === 'true'
    const implicitDemo = confirmedMatchesCount.value !== null && confirmedMatchesCount.value < 5 && !isExplicitlyDisabled
    return isExplicitlyEnabled || implicitDemo
  })

  async function fetchStats(params: PersonalStatsParams = {}) {
    isLoading.value = true
    error.value = null

    try {
      const fetchedStats = await getPersonalStats(params)
      realStats.value = fetchedStats
      
      confirmedMatchesCount.value = fetchedStats.overall.matches
      
      if (confirmedMatchesCount.value >= 5) {
        localStorage.setItem(getDemoModeKey(), 'false')
        rawDemoModeSetting.value = 'false'
      }

      if (shouldShowDemoData.value) {
        stats.value = generateDemoData()
      } else {
        stats.value = fetchedStats
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch statistics'
      if (shouldShowDemoData.value) {
        stats.value = generateDemoData()
      } else {
        stats.value = null
      }
    } finally {
      isLoading.value = false
    }
  }

  function toggleDemoMode(enabled: boolean) {
    const stringVal = String(enabled)
    localStorage.setItem(getDemoModeKey(), stringVal)
    rawDemoModeSetting.value = stringVal
    if (shouldShowDemoData.value) {
      stats.value = generateDemoData()
    } else {
      stats.value = realStats.value
    }
  }

  return {
    stats,
    isLoading,
    error,
    confirmedMatchesCount,
    shouldShowDemoData,
    isDemoModeEnabled: shouldShowDemoData,
    fetchStats,
    toggleDemoMode
  }
})
