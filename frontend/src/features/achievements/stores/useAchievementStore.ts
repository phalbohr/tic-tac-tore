import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AchievementDto,
  PlayerAchievementsSummaryResponse,
} from '@/services/achievementService'
import * as achievementService from '@/services/achievementService'

export const useAchievementStore = defineStore('achievement', () => {
  const achievements = ref<AchievementDto[]>([])
  const totalUnlocked = ref<number>(0)
  const totalAvailable = ref<number>(0)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  const unlockedList = computed(() => achievements.value.filter((a) => a.isUnlocked))
  const lockedList = computed(() => achievements.value.filter((a) => !a.isUnlocked))
  const badgesList = computed(() =>
    achievements.value.filter((a) => a.category !== 'ANTI_ACHIEVEMENT'),
  )
  const antiAchievementsList = computed(() =>
    achievements.value.filter((a) => a.category === 'ANTI_ACHIEVEMENT'),
  )

  const badgesUnlockedCount = computed(() => badgesList.value.filter((a) => a.isUnlocked).length)
  const badgesTotalCount = computed(() => badgesList.value.length)
  const antiAchievementsUnlockedCount = computed(
    () => antiAchievementsList.value.filter((a) => a.isUnlocked).length,
  )
  const antiAchievementsTotalCount = computed(() => antiAchievementsList.value.length)

  async function fetchPlayerAchievements(
    playerId: string,
  ): Promise<PlayerAchievementsSummaryResponse> {
    loading.value = true
    error.value = null
    try {
      const res = await achievementService.getPlayerAchievements(playerId)
      achievements.value = res.achievements || []
      totalUnlocked.value = res.totalUnlocked || 0
      totalAvailable.value = res.totalAvailable || 0
      return res
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : 'Failed to load achievements'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    achievements,
    totalUnlocked,
    totalAvailable,
    loading,
    error,
    unlockedList,
    lockedList,
    badgesList,
    antiAchievementsList,
    badgesUnlockedCount,
    badgesTotalCount,
    antiAchievementsUnlockedCount,
    antiAchievementsTotalCount,
    fetchPlayerAchievements,
  }
})
