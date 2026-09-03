import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LeaderboardView from '@/features/stats/views/LeaderboardView.vue'
import * as statisticsService from '@/services/statisticsService'
import { useAuthStore } from '@/stores/auth'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  }
})

vi.mock('@/services/statisticsService', () => ({
  getLeaderboard: vi.fn(),
}))

describe('[Story 4.2] LeaderboardView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('[P0] should render leaderboard table with ranked entries and win rate', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        { playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 8, losses: 2, winRate: 0.8 },
        { playerId: 'p2', playerName: 'Bob', totalMatches: 5, wins: 2, losses: 3, winRate: 0.4 },
      ],
      totalPages: 1,
      totalElements: 2,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('Leaderboard')
    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).toContain('80.0%')
    expect(wrapper.text()).toContain('40.0%')
    expect(wrapper.find('tbody tr').exists()).toBe(true)
  })

  it('[P0] should display empty state when no players match filters', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('No players match the current filters.')
    expect(wrapper.find('tbody tr').exists()).toBe(false)
  })

  it('[P0] should display loading skeleton while fetching', async () => {
    let resolveGet: (value: any) => void
    const pending = new Promise<any>((resolve) => {
      resolveGet = resolve
    })
    vi.mocked(statisticsService.getLeaderboard).mockReturnValue(pending as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(wrapper.find('.animate-pulse').exists()).toBe(true)

    resolveGet!({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    })
    await flushPromises()
  })

  it('[P0] should display error gracefully without crashing when fetch fails', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockRejectedValue(new Error('Network error'))

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(wrapper.find('.animate-pulse').exists()).toBe(false)
    expect(wrapper.text()).toContain('Failed to load leaderboard')
  })

  it('[P1] should call getLeaderboard with matchFormat (ruleSystem) when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const matchFormatSelect = wrapper.findAll('select')[0]!
    await matchFormatSelect.setValue('RANDOM')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ ruleSystem: 'RANDOM', page: 0 }),
    )
  })

  it('[P1] should call getLeaderboard with matchType when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const matchTypeSelect = wrapper.findAll('select')[1]!
    await matchTypeSelect.setValue('1v1')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ matchType: '1v1', page: 0 }),
    )
  })

  it('[P1] should call getLeaderboard with period when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const periodSelect = wrapper.findAll('select')[2]!
    await periodSelect.setValue('WEEKLY')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ period: 'WEEKLY', page: 0 }),
    )
  })

  it('[P1] should pass minMatches=5 by default', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0,
    } as any)

    mount(LeaderboardView)
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({ minMatches: 5 }),
    )
  })

  it('[P1] should render pagination controls and Page indicator when totalPages > 1', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        { playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 8, losses: 2, winRate: 0.8 },
      ],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('Previous')
    expect(wrapper.text()).toContain('Next')
    expect(wrapper.text()).toContain('Page 1 of 3')
  })

  it('[P1] should navigate to next page when Next is clicked', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const nextBtn = wrapper.findAll('button').filter((b) => b.text() === 'Next')[0]!
    await nextBtn.trigger('click')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1 }),
    )
  })

  it('[P1] should navigate to previous page when Previous is clicked', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const nextBtn = wrapper.findAll('button').filter((b) => b.text() === 'Next')[0]!
    await nextBtn.trigger('click')
    await flushPromises()

    const prevBtn = wrapper.findAll('button').filter((b) => b.text() === 'Previous')[0]!
    await prevBtn.trigger('click')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 0 }),
    )
  })

  it('[P2] should reset to page 0 when a filter changes', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const nextBtn = wrapper.findAll('button').filter((b) => b.text() === 'Next')[0]!
    await nextBtn.trigger('click')
    await flushPromises()

    const matchFormatSelect = wrapper.findAll('select')[0]!
    await matchFormatSelect.setValue('STANDARD')
    await flushPromises()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 0, ruleSystem: 'STANDARD' }),
    )
  })

  it('renders challenge button for authenticated user on other players rows', async () => {
    const authStore = useAuthStore()
    authStore.setAuthenticated(true)
    authStore.profile = { id: 'my-user-id', nickname: 'Me', avatar: 'avatar-1' }

    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        {
          playerId: 'my-user-id',
          playerName: 'Me',
          totalMatches: 10,
          wins: 8,
          losses: 2,
          winRate: 0.8,
        },
        {
          playerId: 'opponent-id',
          playerName: 'Opponent',
          totalMatches: 5,
          wins: 2,
          losses: 3,
          winRate: 0.4,
        },
      ],
      totalPages: 1,
      totalElements: 2,
      size: 20,
      number: 0,
    } as any)

    const wrapper = mount(LeaderboardView, {
      global: {
        stubs: {
          ChallengeModal: true,
        },
      },
    })
    await flushPromises()

    const challengeBtns = wrapper.findAll('[data-testid="challenge-player-btn"]')
    expect(challengeBtns).toHaveLength(1)
  })
})
