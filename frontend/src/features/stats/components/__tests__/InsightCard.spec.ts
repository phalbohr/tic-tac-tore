import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import InsightCard from '../InsightCard.vue'
import type { PlayerInsight } from '@/services/insightService'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, values?: Record<string, unknown>) => {
        const map: Record<string, string> = {
          'insights.winStreak.title': 'On a Roll!',
          'insights.winStreak.description': 'You are currently on a {streak}-match winning streak.',
          'insights.bestPartnership.title': 'Dynamic Duo',
          'insights.bestPartnership.description': 'You and {partnerName} hold a stellar {winRate}% win rate across {matches} games.',
          'insights.category.streak': 'Streak',
          'insights.category.partnership': 'Partnership',
          'insights.drillDown': 'View Details',
        }
        if (map[key] && values) {
          let str = map[key]
          for (const [k, v] of Object.entries(values)) {
            str = str.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v))
          }
          return str
        }
        return map[key] || key
      },
    }),
  }
})

describe.skip('[Story 7.5 ATDD] InsightCard.vue Component Tests (RED PHASE)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const streakInsight: PlayerInsight = {
    id: 'insight-1',
    type: 'WIN_STREAK',
    category: 'STREAK',
    importance: 'HIGH',
    titleKey: 'insights.winStreak.title',
    descriptionKey: 'insights.winStreak.description',
    params: { streak: 4 },
    icon: 'local_fire_department',
    drillDownUrl: null,
  }

  const partnershipInsight: PlayerInsight = {
    id: 'insight-2',
    type: 'BEST_PARTNERSHIP',
    category: 'PARTNERSHIP',
    importance: 'MEDIUM',
    titleKey: 'insights.bestPartnership.title',
    descriptionKey: 'insights.bestPartnership.description',
    params: { partnerName: 'Max', winRate: 75, matches: 8 },
    icon: 'group',
    drillDownUrl: '/statistics?tab=teams',
  }

  it('[P0] [AC5] should render Material Symbols icon, localized title, category badge, and interpolated description', () => {
    const wrapper = mount(InsightCard, {
      props: {
        insight: streakInsight,
      },
    })

    expect(wrapper.find('[data-testid="insight-icon"]').text()).toContain('local_fire_department')
    expect(wrapper.find('[data-testid="insight-title"]').text()).toContain('On a Roll!')
    expect(wrapper.find('[data-testid="insight-description"]').text()).toContain('You are currently on a 4-match winning streak.')
    expect(wrapper.find('[data-testid="insight-category"]').text()).toContain('Streak')
    expect(wrapper.find('[data-testid="insight-drilldown-btn"]').exists()).toBe(false)
  })

  it('[P0] [AC5] should render drill-down CTA button when drillDownUrl is present and navigate on click', async () => {
    const wrapper = mount(InsightCard, {
      props: {
        insight: partnershipInsight,
      },
    })

    const drillDownBtn = wrapper.find('[data-testid="insight-drilldown-btn"]')
    expect(drillDownBtn.exists()).toBe(true)
    expect(drillDownBtn.text()).toContain('View Details')

    await drillDownBtn.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/statistics?tab=teams')
  })
})
