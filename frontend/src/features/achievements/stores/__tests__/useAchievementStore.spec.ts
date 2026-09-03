import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAchievementStore } from '../useAchievementStore'
import * as achievementService from '@/services/achievementService'

vi.mock('@/services/achievementService')

describe('[Story 7.1] useAchievementStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('[P0] should fetch player achievements and populate unlocked and locked lists', async () => {
    const mockResponse: achievementService.PlayerAchievementsSummaryResponse = {
      playerId: 'user-123',
      totalUnlocked: 1,
      totalAvailable: 2,
      achievements: [
        {
          id: 'ach-1',
          code: 'FIRST_WIN',
          category: 'MILESTONE',
          nameKey: 'achievements.first_win.title',
          descriptionKey: 'achievements.first_win.description',
          icon: 'trophy',
          isUnlocked: true,
          unlockedAt: '2026-08-30T12:00:00Z',
          currentProgress: 1,
          targetValue: 1,
          hasProgress: true,
        },
        {
          id: 'ach-2',
          code: 'MATCHES_10',
          category: 'EXPERIENCE',
          nameKey: 'achievements.matches_10.title',
          descriptionKey: 'achievements.matches_10.description',
          icon: 'flame',
          isUnlocked: false,
          unlockedAt: null,
          currentProgress: 4,
          targetValue: 10,
          hasProgress: true,
        },
      ],
    }

    vi.mocked(achievementService.getPlayerAchievements).mockResolvedValueOnce(mockResponse)

    const store = useAchievementStore()
    expect(store.loading).toBe(false)
    expect(store.achievements).toEqual([])

    await store.fetchPlayerAchievements('user-123')

    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.totalUnlocked).toBe(1)
    expect(store.totalAvailable).toBe(2)
    expect(store.unlockedList).toHaveLength(1)
    expect(store.unlockedList[0]?.code).toBe('FIRST_WIN')
    expect(store.lockedList).toHaveLength(1)
    expect(store.lockedList[0]?.code).toBe('MATCHES_10')
  })

  it('[P0] [AC1] should separate badgesList and antiAchievementsList and compute category counts', () => {
    const store = useAchievementStore()
    store.achievements = [
      {
        id: 'ach-1',
        code: 'FIRST_WIN',
        category: 'MILESTONE',
        nameKey: 'achievements.first_win.title',
        descriptionKey: 'achievements.first_win.description',
        icon: 'trophy',
        isUnlocked: true,
        unlockedAt: null,
        currentProgress: 1,
        targetValue: 1,
        hasProgress: true,
      },
      {
        id: 'ach-2',
        code: 'MATCHES_10',
        category: 'EXPERIENCE',
        nameKey: 'achievements.matches_10.title',
        descriptionKey: 'achievements.matches_10.description',
        icon: 'flame',
        isUnlocked: false,
        unlockedAt: null,
        currentProgress: 0,
        targetValue: 10,
        hasProgress: true,
      },
      {
        id: 'ach-3',
        code: 'GOOSE_EGG',
        category: 'ANTI_ACHIEVEMENT',
        nameKey: 'achievements.goose_egg.title',
        descriptionKey: 'achievements.goose_egg.description',
        icon: 'egg',
        isUnlocked: false,
        unlockedAt: null,
        currentProgress: null,
        targetValue: null,
        hasProgress: false,
      },
    ]

    expect(store.badgesList).toHaveLength(2)
    expect(store.badgesList[0]?.code).toBe('FIRST_WIN')
    expect(store.badgesUnlockedCount).toBe(1)
    expect(store.badgesTotalCount).toBe(2)
    expect(store.antiAchievementsList).toHaveLength(1)
    expect(store.antiAchievementsList[0]?.code).toBe('GOOSE_EGG')
    expect(store.antiAchievementsUnlockedCount).toBe(0)
    expect(store.antiAchievementsTotalCount).toBe(1)
  })

  it('[P1] should handle error gracefully when achievement API fails', async () => {
    vi.mocked(achievementService.getPlayerAchievements).mockRejectedValueOnce(
      new Error('Network error'),
    )

    const store = useAchievementStore()
    await expect(store.fetchPlayerAchievements('user-123')).rejects.toThrow('Network error')

    expect(store.loading).toBe(false)
    expect(store.error).toBe('Network error')
  })
})
