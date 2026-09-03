import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import EmptyStateCTA from '@/features/stats/components/EmptyStateCTA.vue'

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
      t: (
        key: string,
        namedOrDefault?: string | Record<string, unknown>,
        named?: Record<string, unknown>,
      ) => {
        if (key === 'h2h.emptyState' && namedOrDefault && typeof namedOrDefault === 'object') {
          return `You haven't played ${(namedOrDefault as any).opponent} yet — start a match?`
        }
        if (typeof namedOrDefault === 'string' && named) {
          return namedOrDefault.replace('{opponent}', String(named.opponent))
        }
        if (typeof namedOrDefault === 'string') return namedOrDefault
        return key
      },
    }),
  }
})

describe('EmptyStateCTA.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders opponent empty state text without rendering [object Object]', () => {
    const wrapper = mount(EmptyStateCTA, {
      props: {
        opponentId: 'opp-1',
        opponentNickname: 'Sonic',
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
      },
    })

    expect(wrapper.text()).toContain("You haven't played Sonic yet — start a match?")
    expect(wrapper.text()).not.toContain('[object Object]')
  })

  it('navigates to match creation with opponentId query param when clicked', async () => {
    const wrapper = mount(EmptyStateCTA, {
      props: {
        opponentId: 'opp-123',
        opponentNickname: 'Sonic',
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
      },
    })

    const button = wrapper.find('button')
    await button.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      path: '/matches/new',
      query: { opponentId: 'opp-123' },
    })
  })
})
