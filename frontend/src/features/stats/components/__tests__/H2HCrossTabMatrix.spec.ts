import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import H2HCrossTabMatrix from '@/features/stats/components/H2HCrossTabMatrix.vue'
import { useStatsStore } from '@/features/stats/stores/useStatsStore'
import * as statisticsService from '@/services/statisticsService'

const mockPush = vi.fn()
const mockRoute = { query: {} }

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  useRoute: () => mockRoute,
}))

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultMsg?: string) => defaultMsg || key,
    }),
  }
})

vi.mock('@/services/statisticsService', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/services/statisticsService')>()
  return {
    ...actual,
    searchPlayers: vi.fn(),
  }
})

describe('H2HCrossTabMatrix.vue component tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('updates router query parameter when selecting a new opponent from search', async () => {
    const wrapper = mount(H2HCrossTabMatrix, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
      },
    })

    const openSearchBtn = wrapper.find('[data-testid="select-opponent-btn"]')
    expect(openSearchBtn.exists()).toBe(true)
    await openSearchBtn.trigger('click')

    vi.mocked(statisticsService.searchPlayers).mockResolvedValueOnce([
      { id: 'player-99', nickname: 'NewRival' },
    ])

    const searchInput = wrapper.find('[data-testid="opponent-search-input"]')
    expect(searchInput.exists()).toBe(true)
    await searchInput.setValue('NewRival')
    await new Promise((r) => setTimeout(r, 350))
    await flushPromises()

    const playerResultItem = wrapper.find('[data-testid="opponent-search-result"]')
    expect(playerResultItem.exists()).toBe(true)
    await playerResultItem.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      query: {
        opponentId: 'player-99',
      },
    })
  })

  it('renders correctly formatted goal difference and color class', async () => {
    const wrapper = mount(H2HCrossTabMatrix, {
      props: {
        opponentId: 'opp-1',
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
      },
    })

    const store = useStatsStore()
    store.h2hStats = {
      opponent: { id: 'opp-1', nickname: 'Rival' },
      matches: {
        with: { matches: 2, wins: 1, losses: 1, draws: 0, winRate: 50 },
        vs: { matches: 4, wins: 3, losses: 1, draws: 0, winRate: 75 },
      },
      games: {
        with: { gamesWon: 4, gamesLost: 2, totalGames: 6, winRate: 66.7 },
        vs: { gamesWon: 8, gamesLost: 4, totalGames: 12, winRate: 66.7 },
      },
      goals: {
        attackerVsDefender: { scored: 10, conceded: 4 },
        attackerVsAttacker: { scored: 3, conceded: 7 },
        defenderVsAttacker: { scored: 5, conceded: 5 },
        defenderVsDefender: { scored: 2, conceded: 6 },
      },
    }
    store.isH2HLoading = false
    await wrapper.vm.$nextTick()

    const text = wrapper.text()
    expect(text).toContain('+6') // 10 - 4
    expect(text).toContain('-4') // 3 - 7
    expect(text).toContain('0')  // 5 - 5
  })
})
