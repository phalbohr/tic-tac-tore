import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MicroCelebrationBanner from '../MicroCelebrationBanner.vue'
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
      t: (key: string, _values?: Record<string, unknown>) => {
        const map: Record<string, string> = {
          'insights.winStreak.title': 'On a Roll!',
          'insights.winStreak.description': 'You are on a 5-match winning streak.',
          'insights.milestoneProximity.title': 'Milestone in Reach',
          'insights.milestoneProximity.description': 'Only 2 more matches to unlock the badge!',
          'insights.drillDown': 'View Details',
          'common.close': 'Close',
        }
        return map[key] || key
      },
    }),
  }
})

describe('[Story 7.5 ATDD] MicroCelebrationBanner.vue Component Tests', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  const celebrationInsight: PlayerInsight = {
    id: 'insight-celeb-1',
    type: 'WIN_STREAK',
    category: 'STREAK',
    importance: 'HIGH',
    titleKey: 'insights.winStreak.title',
    descriptionKey: 'insights.winStreak.description',
    params: { streak: 5 },
    icon: 'local_fire_department',
    drillDownUrl: '/cabinet',
  }

  it('[P0] [AC6] should render with role="status" and aria-live="polite" displaying insight details', () => {
    const wrapper = mount(MicroCelebrationBanner, {
      props: {
        insight: celebrationInsight,
      },
    })

    const banner = wrapper.find('[data-testid="micro-celebration-banner"]')
    expect(banner.exists()).toBe(true)
    expect(banner.attributes('role')).toBe('status')
    expect(banner.attributes('aria-live')).toBe('polite')
    expect(wrapper.text()).toContain('On a Roll!')
    expect(wrapper.text()).toContain('You are on a 5-match winning streak.')
  })

  it('[P0] [AC6] should auto-dismiss after 4000ms', async () => {
    const wrapper = mount(MicroCelebrationBanner, {
      props: {
        insight: celebrationInsight,
      },
    })

    expect(wrapper.find('[data-testid="micro-celebration-banner"]').exists()).toBe(true)

    vi.advanceTimersByTime(4000)
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('dismiss')).toBeTruthy()
  })

  it('[P1] [AC6] should emit dismiss on close button click and navigate on drill-down click', async () => {
    const wrapper = mount(MicroCelebrationBanner, {
      props: {
        insight: celebrationInsight,
      },
    })

    const closeBtn = wrapper.find('[data-testid="celebration-close-btn"]')
    expect(closeBtn.exists()).toBe(true)
    await closeBtn.trigger('click')
    expect(wrapper.emitted('dismiss')).toBeTruthy()

    const drillDownBtn = wrapper.find('[data-testid="celebration-drilldown-btn"]')
    expect(drillDownBtn.exists()).toBe(true)
    await drillDownBtn.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/cabinet')
  })
})
