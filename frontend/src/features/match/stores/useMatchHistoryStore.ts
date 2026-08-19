import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMatchHistory, type MatchResponse } from '@/services/matchService'
import { generateDemoMatchHistory } from '@/features/stats/utils/demoDataGenerator'
import { useAuthStore } from '@/stores/auth'

export interface MatchHistoryFilters {
  playerId: string | null
  matchType: '1v1' | '2v2' | null
  ruleConfigId: string | null
}

export interface PaginationState {
  page: number
  size: number
  totalElements: number
  totalPages: number
  first?: boolean
  last?: boolean
}

export const useMatchHistoryStore = defineStore('matchHistory', () => {
  const activeTab = ref<'confirmed' | 'pending'>('confirmed')
  const confirmedMatches = ref<MatchResponse[]>([])
  const pendingMatches = ref<any[]>([])
  const isConfirmedLoading = ref(false)
  const isPendingLoading = ref(false)
  const loading = computed(() => (activeTab.value === 'confirmed' ? isConfirmedLoading.value : isPendingLoading.value))
  const error = ref<string | null>(null)
  const isDemoMode = ref(false)

  const pagination = ref<PaginationState>({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
  })

  const filters = ref<MatchHistoryFilters>({
    playerId: null,
    matchType: null,
    ruleConfigId: null
  })

  let confirmedAbortController: AbortController | null = null
  let pendingAbortController: AbortController | null = null

  const hasFilters = computed(() => {
    return !!(filters.value.playerId || filters.value.matchType || filters.value.ruleConfigId)
  })

  async function fetchConfirmedHistory(): Promise<void> {
    if (confirmedAbortController) {
      confirmedAbortController.abort()
    }
    const abortController = new AbortController()
    confirmedAbortController = abortController

    isConfirmedLoading.value = true
    error.value = null

    try {
      if (isDemoMode.value) {
        const authStore = useAuthStore()
        const demo = generateDemoMatchHistory(authStore.profile ? { id: authStore.profile.id, nickname: authStore.profile.nickname } : undefined)
        confirmedMatches.value = demo.content
        pagination.value = {
          page: demo.page,
          size: demo.size,
          totalElements: demo.totalElements,
          totalPages: demo.totalPages,
          first: demo.first ?? true,
          last: demo.last ?? true
        }
        return
      }

      const res = await getMatchHistory({
        status: 'CONFIRMED',
        playerId: filters.value.playerId,
        matchType: filters.value.matchType,
        ruleConfigId: filters.value.ruleConfigId,
        page: pagination.value.page,
        size: pagination.value.size,
        signal: abortController.signal
      })

      if (abortController.signal.aborted) {
        return
      }

      confirmedMatches.value = res.content || []
      pagination.value = {
        page: res.page ?? 0,
        size: res.size ?? 10,
        totalElements: res.totalElements ?? 0,
        totalPages: res.totalPages ?? 0,
        first: res.first ?? (res.page === 0),
        last: res.last ?? ((res.page ?? 0) >= (res.totalPages ?? 1) - 1)
      }
    } catch (err: any) {
      if (err.name === 'AbortError' || abortController.signal.aborted) {
        return
      }
      error.value = err.message || 'Failed to load match history'
      confirmedMatches.value = []
    } finally {
      if (confirmedAbortController === abortController) {
        isConfirmedLoading.value = false
      }
    }
  }

  async function fetchPendingMatches(): Promise<void> {
    if (pendingAbortController) {
      pendingAbortController.abort()
    }
    const abortController = new AbortController()
    pendingAbortController = abortController

    isPendingLoading.value = true
    error.value = null
    try {
      const res = await fetch('/api/v1/matches/pending', {
        signal: abortController.signal
      })
      if (abortController.signal.aborted) {
        return
      }
      if (res.ok) {
        const data = await res.json()
        if (abortController.signal.aborted) return
        pendingMatches.value = data.matches || []
      } else {
        error.value = `Failed to load pending matches (${res.status})`
        pendingMatches.value = []
      }
    } catch (err: any) {
      if (err.name === 'AbortError' || abortController.signal.aborted) {
        return
      }
      error.value = err.message || 'Failed to load pending matches'
      pendingMatches.value = []
    } finally {
      if (pendingAbortController === abortController) {
        isPendingLoading.value = false
      }
    }
  }

  function setFilter<K extends keyof MatchHistoryFilters>(key: K, value: MatchHistoryFilters[K]): void {
    filters.value[key] = value
    pagination.value.page = 0
    fetchConfirmedHistory()
  }

  function resetFilters(): void {
    filters.value = {
      playerId: null,
      matchType: null,
      ruleConfigId: null
    }
    pagination.value.page = 0
    fetchConfirmedHistory()
  }

  function setTab(tab: 'confirmed' | 'pending'): void {
    activeTab.value = tab
    if (tab === 'confirmed') {
      if (pendingAbortController) {
        pendingAbortController.abort()
      }
      fetchConfirmedHistory()
    } else {
      if (confirmedAbortController) {
        confirmedAbortController.abort()
      }
      fetchPendingMatches()
    }
  }

  function setPage(page: number): void {
    pagination.value.page = page
    fetchConfirmedHistory()
  }

  function enableDemoMode(): void {
    isDemoMode.value = true
    pagination.value.page = 0
    fetchConfirmedHistory()
  }

  function toggleDemoMode(enable?: boolean): void {
    isDemoMode.value = enable !== undefined ? enable : !isDemoMode.value
    pagination.value.page = 0
    fetchConfirmedHistory()
  }

  return {
    activeTab,
    confirmedMatches,
    pendingMatches,
    isConfirmedLoading,
    isPendingLoading,
    loading,
    error,
    isDemoMode,
    pagination,
    filters,
    hasFilters,
    fetchConfirmedHistory,
    fetchPendingMatches,
    setFilter,
    resetFilters,
    setTab,
    setPage,
    enableDemoMode,
    toggleDemoMode
  }
})
