import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProfileBadgesSection from '../ProfileBadgesSection.vue'
import { useAchievementStore } from '../../stores/useAchievementStore'
import { useAuthStore } from '@/stores/auth'

import type { AchievementDto } from '@/services/achievementService'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, values?: Record<string, unknown>) => {
        const map: Record<string, string> = {
          'achievements.title': 'Achievements',
          'achievements.filterAll': 'All',
          'achievements.filterBadges': 'Badges',
          'achievements.filterAnti': 'Anti-Achievements',
          'achievements.unlocked': 'Unlocked',
          'achievements.locked': 'Locked',
          'common.close': 'Close',
        }
        if (key === 'achievements.progress' && values) {
          return `Progress: ${values.current} / ${values.target}`
        }
        if (key === 'achievements.remaining' && values) {
          return `${values.count} remaining`
        }
        return map[key] || key
      },
      te: () => true,
    }),
  }
})

describe('[Story 7.3 ATDD] ProfileBadgesSection.vue Category Filtering & Modal Progress', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = { id: 'p-1', nickname: 'Alex', avatar: 'avatar1' }
  })

  const mockAchievements: AchievementDto[] = [
    {
      id: 'b-1',
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
      id: 'b-2',
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
    {
      id: 'b-3',
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

  it('[P0] [AC1] should render category filter tabs (All, Badges, Anti-Achievements)', async () => {
    const store = useAchievementStore()
    store.achievements = mockAchievements
    store.totalUnlocked = 1
    store.totalAvailable = 3

    const wrapper = mount(ProfileBadgesSection)

    const tabs = wrapper.findAll('[data-testid^="category-filter-tab-"]')
    expect(tabs.length).toBe(3)
    expect(tabs[0]?.text()).toContain('All')
    expect(tabs[1]?.text()).toContain('Badges')
    expect(tabs[2]?.text()).toContain('Anti-Achievements')
  })

  it('[P0] [AC1] should filter badges list when clicking Anti-Achievements tab', async () => {
    const store = useAchievementStore()
    store.achievements = mockAchievements
    store.totalUnlocked = 1
    store.totalAvailable = 3

    const wrapper = mount(ProfileBadgesSection)

    const antiTab = wrapper.find('[data-testid="category-filter-tab-anti"]')
    await antiTab.trigger('click')

    const displayedCards = wrapper.findAll('[data-testid="badge-card"]')
    expect(displayedCards.length).toBe(1)
  })

  it('[P0] [AC5] should render modal progress bar with remaining count when clicking locked progressive badge', async () => {
    const store = useAchievementStore()
    store.achievements = mockAchievements
    store.totalUnlocked = 1
    store.totalAvailable = 3

    const wrapper = mount(ProfileBadgesSection)

    // Select the locked progressive badge (MATCHES_10)
    const cards = wrapper.findAll('[data-testid="badge-card"]')
    await cards[1]?.trigger('click')

    const modal = wrapper.find('[data-testid="badge-modal"]')
    expect(modal.exists()).toBe(true)

    const modalProgressBar = wrapper.find('[data-testid="modal-progress-bar"]')
    expect(modalProgressBar.exists()).toBe(true)
    expect(wrapper.text()).toContain('Progress: 4 / 10')
    expect(wrapper.text()).toContain('6 remaining')
  })
})
