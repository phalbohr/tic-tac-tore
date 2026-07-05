import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPersonalStats, type PlayerStats, type PersonalStatsParams } from '@/services/statisticsService'
import { generateDemoData } from '../utils/demoDataGenerator'

const DEMO_MODE_KEY = 'tictactore.demoModeEnabled'

export const useStatsStore = defineStore('stats', () => {
  const stats = ref<PlayerStats | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const confirmedMatchesCount = ref(0) 

  // Make demo mode reactive by using a ref
  const _isDemoModeEnabled = ref(localStorage.getItem(DEMO_MODE_KEY) === 'true')

  // AC 1 & PRD Decision: serve demo data when matches < 5 OR explicit demo mode toggle
  const shouldShowDemoData = computed(() => {
    // If we have < 5 confirmed matches AND demo mode is NOT explicitly disabled
    const implicitDemo = confirmedMatchesCount.value < 5 && localStorage.getItem(DEMO_MODE_KEY) !== 'false'
    return _isDemoModeEnabled.value || implicitDemo
  })

  async function fetchStats(params: PersonalStatsParams = {}) {
    isLoading.value = true
    error.value = null

    try {
      // First, try to fetch real stats to check real matches count
      const realStats = await getPersonalStats(params)
      
      // Update lifetime matches 
      confirmedMatchesCount.value = realStats.overall.matches
      
      // Auto-disable demo data if lifetime matches >= 5
      if (confirmedMatchesCount.value >= 5) {
        _isDemoModeEnabled.value = false
        localStorage.setItem(DEMO_MODE_KEY, 'false')
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
    _isDemoModeEnabled.value = enabled
    localStorage.setItem(DEMO_MODE_KEY, String(enabled))
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
