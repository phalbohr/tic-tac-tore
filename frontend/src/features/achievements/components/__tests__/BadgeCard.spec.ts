import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BadgeCard from '../BadgeCard.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'achievements.first_win.title': 'First Win',
        'achievements.first_win.description': 'Win your first foosball match.',
        'achievements.unlocked': 'Unlocked',
        'achievements.locked': 'Locked',
      }
      return map[key] || key
    },
    te: () => true,
  }),
}))

describe('[Story 7.1] BadgeCard.vue', () => {
  const unlockedBadge = {
    id: 'b-1',
    code: 'FIRST_WIN',
    category: 'MILESTONE',
    nameKey: 'achievements.first_win.title',
    descriptionKey: 'achievements.first_win.description',
    icon: 'trophy',
    isUnlocked: true,
    unlockedAt: '2026-08-30T12:00:00Z',
  }

  const lockedBadge = {
    id: 'b-2',
    code: 'MATCHES_10',
    category: 'EXPERIENCE',
    nameKey: 'achievements.matches_10.title',
    descriptionKey: 'achievements.matches_10.description',
    icon: 'flame',
    isUnlocked: false,
    unlockedAt: null,
  }

  it('[P0] should render unlocked badge with icon and Clubhouse styles', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: unlockedBadge,
      },
    })

    expect(wrapper.find('[data-testid="badge-card"]').attributes('data-unlocked')).toBe('true')
    expect(wrapper.text()).toContain('First Win')
    expect(wrapper.text()).toContain('Unlocked')
  })

  it('[P1] should render locked state with visual indicator', () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: lockedBadge,
      },
    })

    expect(wrapper.find('[data-testid="badge-card"]').attributes('data-unlocked')).toBe('false')
    expect(wrapper.text()).toContain('Locked')
    expect(wrapper.text()).toContain('lock')
  })

  it('should emit select event when clicked', async () => {
    const wrapper = mount(BadgeCard, {
      props: {
        badge: unlockedBadge,
      },
    })

    await wrapper.find('[data-testid="badge-card"]').trigger('click')

    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')?.[0]).toEqual([unlockedBadge])
  })
})
