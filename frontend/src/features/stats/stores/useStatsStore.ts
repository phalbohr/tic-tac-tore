import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { getPersonalStats, type PlayerStats, type PersonalStatsParams } from '@/services/statisticsService'
import { generateDemoData } from '../utils/demoDataGenerator'
import { useAuthStore } from '@/stores/auth'

export const useStatsStore = defineStore('stats', () => {
  const authStore = useAuthStore()
  
  const getDemoModeKey = () => `tictactore.demoModeEnabled_${authStore.profile?.nickname || 'guest'}`

  const stats = ref<PlayerStats | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const confirmedMatchesCount = ref<number | null>(null)

  const _isDemoModeEnabledExplicit = ref(localStorage.getItem(getDemoModeKey()) === 'true')
  const _isDemoModeDisabledExplicit = ref(localStorage.getItem(getDemoModeKey()) === 'false')

  watch(() => authStore.profile?.nickname, () => {
    _isDemoModeEnabledExplicit.value = localStorage.getItem(getDemoModeKey()) === 'true'
    _isDemoModeDisabledExplicit.value = localStorage.getItem(getDemoModeKey()) === 'false'
  })

  const shouldShowDemoData = computed(() => {
    const implicitDemo = confirmedMatchesCount.value !== null && confirmedMatchesCount.value < 5 && !_isDemoModeDisabledExplicit.value
    return _isDemoModeEnabledExplicit.value || implicitDemo
  })

  async function fetchStats(params: PersonalStatsParams = {}) {
    isLoading.value = true
    error.value = null

    try {
      const realStats = await getPersonalStats(params)
      
      confirmedMatchesCount.value = realStats.overall.matches
      
      if (confirmedMatchesCount.value >= 5) {
        localStorage.setItem(getDemoModeKey(), 'false')
        _isDemoModeEnabledExplicit.value = false
        _isDemoModeDisabledExplicit.value = true
      }

      if (shouldShowDemoData.value) {
        stats.value = generateDemoData()
      } else {
        stats.value = realStats
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
    localStorage.setItem(getDemoModeKey(), String(enabled))
    _isDemoModeEnabledExplicit.value = enabled
    _isDemoModeDisabledExplicit.value = !enabled
    fetchStats()
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
