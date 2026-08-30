import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BadgeCard from '../BadgeCard.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, values?: Record<string, unknown>) => {
      const map: Record<string, string> = {
        'achievements.matches_10.title': '10 Matches Played',
        'achievements.matches_10.description': 'Play 10 total foosball matches.',
        'achievements.clean_sheet.title': 'Clean Sheet',
        'achievements.unlocked': 'Unlocked',
        'achievements.locked': 'Locked',
      }
      if (key === 'achievements.progress' && values) {
        return `Progress: ${values.current} / ${values.target}`
      }
      return map[key] || key
    },
    te: () => true,
  }),
}))

describe.skip('[Story 7.3 ATDD Red Phase] BadgeCard.vue Progress Bar', () => {
  const lockedProgressiveBadge = {
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
  }

  const unlockedProgressiveBadge = {
    id: 'b-2',
    code: 'MATCHES_10',
    category: 'EXPERIENCE',
    nameKey: 'achievements.matches_10.title',
    descriptionKey: 'achievements.matches_10.description',
    icon: 'flame',
    isUnlocked: true,
    unlockedAt: '2026-08-30T12:00:00Z',
    currentProgress: 10,
    targetValue: 10,
    hasProgress: true,
  }

  const nonProgressiveBadge = {
    id: 'b-3',
    code: 'CLEAN_SHEET',
    category: 'SKILL',
    nameKey: 'achievements.clean_sheet.title',
    descriptionKey: 'achievements.clean_sheet.description',
    icon: 'shield',
    isUnlocked: false,
    unlockedAt: null,
    currentProgress: null,
    targetValue: null,
    hasProgress: false,
  }

  it('[P0] [AC5] should render mini progress bar and ratio text (4 / 10) when locked and hasProgress is true', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: lockedProgressiveBadge as any,
      },
    })

    const progressBar = wrapper.find('[data-testid="badge-progress-bar"]')
    expect(progressBar.exists()).toBe(true)

    const progressRatio = wrapper.find('[data-testid="badge-progress-ratio"]')
    expect(progressRatio.exists()).toBe(true)
    expect(progressRatio.text()).toContain('4 / 10')
  })

  it('[P0] [AC3, AC5] should NOT render progress bar when badge is already unlocked', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: unlockedProgressiveBadge as any,
      },
    })

    expect(wrapper.find('[data-testid="badge-progress-bar"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="badge-progress-ratio"]').exists()).toBe(false)
  })

  it('[P0] [AC3, AC5] should NOT render progress bar for non-progressive badges', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: nonProgressiveBadge as any,
      },
    })

    expect(wrapper.find('[data-testid="badge-progress-bar"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="badge-progress-ratio"]').exists()).toBe(false)
  })
})
