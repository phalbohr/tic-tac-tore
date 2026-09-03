import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import {
  getPersonalStats,
  getTeamPairStats,
  getHeadToHeadStats,
  type PlayerStats,
  type PersonalStatsParams,
  type TeamPairStats,
  type TeamPairStatsParams,
  type H2HStatsResponse,
  type H2HParams,
  type Page,
} from '@/services/statisticsService'
import {
  generateDemoData,
  generateDemoTeamPairStats,
  generateDemoH2HStats,
} from '../utils/demoDataGenerator'
import { useAuthStore } from '@/stores/auth'

export const useStatsStore = defineStore('stats', () => {
  const authStore = useAuthStore()

  const getDemoModeKey = () =>
    `tictactore.demoModeEnabled_${authStore.profile?.nickname || 'guest'}`

  const stats = ref<PlayerStats | null>(null)
  const realStats = ref<PlayerStats | null>(null)
  const teamPairStats = ref<TeamPairStats[]>([])
  const realTeamPairStats = ref<TeamPairStats[]>([])
  const teamPairPage = ref<Page<TeamPairStats> | null>(null)
  const realTeamPairPage = ref<Page<TeamPairStats> | null>(null)
  const h2hStats = ref<H2HStatsResponse | null>(null)
  const realH2HStats = ref<H2HStatsResponse | null>(null)
  const selectedOpponentId = ref<string | null>(null)
  const isLoading = ref(false)
  const isTeamPairsLoading = ref(false)
  const isH2HLoading = ref(false)
  const error = ref<string | null>(null)
  const h2hError = ref<string | null>(null)
  const confirmedMatchesCount = ref<number | null>(null)

  const rawDemoModeSetting = ref<string | null>(localStorage.getItem(getDemoModeKey()))

  watch(
    () => authStore.profile?.nickname,
    () => {
      rawDemoModeSetting.value = localStorage.getItem(getDemoModeKey())
    },
  )

  const shouldShowDemoData = computed(() => {
    const isExplicitlyDisabled = rawDemoModeSetting.value === 'false'
    const isExplicitlyEnabled = rawDemoModeSetting.value === 'true'
    const implicitDemo =
      confirmedMatchesCount.value !== null &&
      confirmedMatchesCount.value < 5 &&
      !isExplicitlyDisabled
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
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

  async function fetchTeamPairStats(params: TeamPairStatsParams = {}) {
    isTeamPairsLoading.value = true
    error.value = null

    try {
      const pagedResult = await getTeamPairStats(params)
      realTeamPairStats.value = pagedResult.content || []
      realTeamPairPage.value = pagedResult
      teamPairPage.value = pagedResult

      if (shouldShowDemoData.value && (!pagedResult.content || pagedResult.content.length === 0)) {
        const demoPaged = generateDemoTeamPairStats()
        teamPairStats.value = demoPaged.content
        teamPairPage.value = demoPaged
      } else {
        teamPairStats.value = pagedResult.content || []
      }
      return pagedResult
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch team pair statistics'
      if (shouldShowDemoData.value) {
        const demoPaged = generateDemoTeamPairStats()
        teamPairStats.value = demoPaged.content
        teamPairPage.value = demoPaged
      } else {
        teamPairStats.value = []
        teamPairPage.value = null
        realTeamPairPage.value = null
      }
    } finally {
      isTeamPairsLoading.value = false
    }
  }

  async function fetchH2HStats(opponentId: string, params: H2HParams = {}) {
    isH2HLoading.value = true
    h2hError.value = null
    h2hStats.value = null
    selectedOpponentId.value = opponentId

    try {
      const fetchedStats = await getHeadToHeadStats(opponentId, params)
      realH2HStats.value = fetchedStats

      if (shouldShowDemoData.value) {
        h2hStats.value = generateDemoH2HStats(opponentId)
      } else {
        h2hStats.value = fetchedStats
      }
      return fetchedStats
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      h2hError.value = err.message || 'Failed to fetch head-to-head statistics'
      if (shouldShowDemoData.value) {
        h2hStats.value = generateDemoH2HStats(opponentId)
      } else {
        h2hStats.value = null
      }
    } finally {
      isH2HLoading.value = false
    }
  }

  function toggleDemoMode(enabled: boolean) {
    const stringVal = String(enabled)
    localStorage.setItem(getDemoModeKey(), stringVal)
    rawDemoModeSetting.value = stringVal
    if (shouldShowDemoData.value) {
      stats.value = generateDemoData()
      const demoPairs = generateDemoTeamPairStats()
      teamPairStats.value = demoPairs.content
      teamPairPage.value = demoPairs
      h2hStats.value = generateDemoH2HStats(selectedOpponentId.value || undefined)
    } else {
      stats.value = realStats.value
      teamPairStats.value = realTeamPairStats.value
      teamPairPage.value = realTeamPairPage.value
      h2hStats.value = realH2HStats.value
    }
  }

  return {
    stats,
    teamPairStats,
    teamPairPage,
    h2hStats,
    selectedOpponentId,
    isLoading,
    isTeamPairsLoading,
    isH2HLoading,
    error,
    h2hError,
    confirmedMatchesCount,
    shouldShowDemoData,
    isDemoModeEnabled: shouldShowDemoData,
    fetchStats,
    fetchTeamPairStats,
    fetchH2HStats,
    toggleDemoMode,
  }
})
