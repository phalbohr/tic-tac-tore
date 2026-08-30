import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BadgeCard from '../BadgeCard.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'achievements.goose_egg.title': 'Goose Egg',
        'achievements.goose_egg.description': 'Lost a game with 0 points scored.',
        'achievements.generous_host.title': 'Generous Host',
        'achievements.generous_host.description': 'Conceded 10 or more points in a single game.',
        'achievements.sieve_defense.title': 'Sieve Defense',
        'achievements.sieve_defense.description': 'Conceded 15+ goals in a match while playing Defender.',
        'achievements.heartbreaker.title': 'Heartbreaker',
        'achievements.heartbreaker.description': 'Lost the match in the deciding game by exactly 1 goal.',
        'achievements.unlocked': 'Unlocked',
        'achievements.locked': 'Locked',
      }
      return map[key] || key
    },
    te: () => true,
  }),
}))

describe.skip('[Story 7.2 ATDD Red Phase] Anti-Achievement Badge Rendering (BadgeCard.vue)', () => {
  const gooseEggBadge = {
    id: 'anti-1',
    code: 'GOOSE_EGG',
    category: 'ANTI_ACHIEVEMENT',
    nameKey: 'achievements.goose_egg.title',
    descriptionKey: 'achievements.goose_egg.description',
    icon: 'egg',
    isUnlocked: true,
    unlockedAt: '2026-08-30T14:00:00Z',
  }

  const generousHostBadge = {
    id: 'anti-2',
    code: 'GENEROUS_HOST',
    category: 'ANTI_ACHIEVEMENT',
    nameKey: 'achievements.generous_host.title',
    descriptionKey: 'achievements.generous_host.description',
    icon: 'volunteer_activism',
    isUnlocked: true,
    unlockedAt: '2026-08-30T14:05:00Z',
  }

  const sieveDefenseBadge = {
    id: 'anti-3',
    code: 'SIEVE_DEFENSE',
    category: 'ANTI_ACHIEVEMENT',
    nameKey: 'achievements.sieve_defense.title',
    descriptionKey: 'achievements.sieve_defense.description',
    icon: 'water_drop',
    isUnlocked: false,
    unlockedAt: null,
  }

  const heartbreakerBadge = {
    id: 'anti-4',
    code: 'HEARTBREAKER',
    category: 'ANTI_ACHIEVEMENT',
    nameKey: 'achievements.heartbreaker.title',
    descriptionKey: 'achievements.heartbreaker.description',
    icon: 'heart_broken',
    isUnlocked: true,
    unlockedAt: '2026-08-30T14:10:00Z',
  }

  it('[P0] [AC6] should render GOOSE_EGG anti-achievement with egg icon and styling', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: gooseEggBadge,
      },
    })

    expect(wrapper.find('[data-testid="badge-card"]').attributes('data-unlocked')).toBe('true')
    expect(wrapper.text()).toContain('Goose Egg')
    expect(wrapper.text()).toContain('egg')
  })

  it('[P0] [AC6] should render GENEROUS_HOST anti-achievement with volunteer_activism icon', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: generousHostBadge,
      },
    })

    expect(wrapper.text()).toContain('Generous Host')
    expect(wrapper.text()).toContain('volunteer_activism')
  })

  it('[P0] [AC6] should render SIEVE_DEFENSE locked anti-achievement with water_drop icon', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: sieveDefenseBadge,
      },
    })

    expect(wrapper.find('[data-testid="badge-card"]').attributes('data-unlocked')).toBe('false')
    expect(wrapper.text()).toContain('Sieve Defense')
  })

  it('[P0] [AC6] should render HEARTBREAKER anti-achievement with heart_broken icon', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: heartbreakerBadge,
      },
    })

    expect(wrapper.text()).toContain('Heartbreaker')
    expect(wrapper.text()).toContain('heart_broken')
  })
})
