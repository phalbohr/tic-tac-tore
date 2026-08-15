import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LeaderboardView from '@/features/stats/views/LeaderboardView.vue'
import * as statisticsService from '@/services/statisticsService'

vi.mock('@/services/statisticsService', () => ({
  getLeaderboard: vi.fn()
}))

describe('[Story 4.2] LeaderboardView.vue (ATDD RED PHASE)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('[P0] should render leaderboard table with entries', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        { rank: 1, playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 8, losses: 2, winRate: 0.8 },
        { rank: 2, playerId: 'p2', playerName: 'Bob', totalMatches: 5, wins: 2, losses: 3, winRate: 0.4 }
      ],
      totalPages: 1,
      totalElements: 2,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Leaderboard')
    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).toContain('80.0%')
    expect(wrapper.text()).toContain('40.0%')
  })

  it('[P0] should display empty state when no players match filters', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('No players match the current filters.')
  })

  it('[P0] should display loading skeleton while fetching', async () => {
    let resolvePromise: (value: any) => void
    const promise = new Promise<any>((resolve) => { resolvePromise = resolve })
    vi.mocked(statisticsService.getLeaderboard).mockReturnValue(promise as any)

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.animate-pulse').exists()).toBe(true)
    resolvePromise!({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })
    await wrapper.vm.$nextTick()
  })

  it('[P1] should call getLeaderboard with matchFormat filter when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const matchFormatSelect = wrapper.find('select:nth-of-type(1)')
    await matchFormatSelect.setValue('STANDARD')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        ruleSystem: 'STANDARD'
      })
    )
  })

  it('[P1] should call getLeaderboard with matchType filter when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const matchTypeSelect = wrapper.find('select:nth-of-type(2)')
    await matchTypeSelect.setValue('1v1')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        matchType: '1v1'
      })
    )
  })

  it('[P1] should call getLeaderboard with period filter when changed', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const periodSelect = wrapper.find('select:nth-of-type(3)')
    await periodSelect.setValue('WEEKLY')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        period: 'WEEKLY'
      })
    )
  })

  it('[P1] should render pagination controls when totalPages > 1', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        { rank: 1, playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 8, losses: 2, winRate: 0.8 }
      ],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Previous')
    expect(wrapper.text()).toContain('Next')
    expect(wrapper.text()).toContain('Page 1 of 3')
  })

  it('[P1] should navigate to next page when Next button is clicked', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const nextButton = wrapper.find('button:contains("Next")')
    await nextButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 1
      })
    )
  })

  it('[P1] should navigate to previous page when Previous button is clicked', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 3,
      totalElements: 30,
      size: 10,
      number: 1
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const prevButton = wrapper.find('button:contains("Previous")')
    await prevButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0
      })
    )
  })

  it('[P1] should display correct rank based on pagination offset', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [
        { rank: 0, playerId: 'p1', playerName: 'Alice', totalMatches: 10, wins: 8, losses: 2, winRate: 0.8 }
      ],
      totalPages: 2,
      totalElements: 11,
      size: 10,
      number: 1
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('11')
  })

  it('[P2] should pass minMatches=5 by default', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 0
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(
      expect.objectContaining({
        minMatches: 5
      })
    )
  })

  it('[P2] should reset to page 0 when filters change', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: 20,
      number: 1
    })

    const wrapper = mount(LeaderboardView)
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const matchFormatSelect = wrapper.find('select:nth-of-type(1)')
    await matchFormatSelect.setValue('RANDOM')
    await wrapper.vm.$nextTick()

    expect(statisticsService.getLeaderboard).toHaveBeenLastCalledWith(
      expect.objectContaining({
        page: 0
      })
    )
  })
})
