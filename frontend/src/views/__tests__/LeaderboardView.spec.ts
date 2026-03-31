import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import LeaderboardView from '../LeaderboardView.vue'
import * as statisticsService from '../../services/statisticsService'
import { useAuthStore } from '@/stores/auth'

vi.mock('../../services/statisticsService', () => ({
  getLeaderboard: vi.fn(),
  getPersonalStats: vi.fn(),
  getH2HStats: vi.fn()
}))

describe('LeaderboardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
    
    // Mock user login
    const authStore = useAuthStore()
    authStore.login('test-token', { id: 'user-1', name: 'Test User', email: 'test@example.com' })
  })

  it('renders correctly and fetches data on mount', async () => {
    const mockData = {
      content: [
        {
          rank: 1,
          playerId: 'uuid-1',
          playerName: 'Best Player',
          totalMatches: 20,
          wins: 18,
          losses: 2,
          winRate: 90.0
        }
      ],
      totalPages: 1,
      totalElements: 1,
      size: 10,
      number: 0
    }

    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue(mockData)
    vi.mocked(statisticsService.getPersonalStats).mockResolvedValue({
      playerId: 'user-1',
      playerName: 'Test User',
      overall: { matches: 0, wins: 0, losses: 0, winRate: 0 },
      attacker: { matches: 0, wins: 0, losses: 0, winRate: 0 },
      defender: { matches: 0, wins: 0, losses: 0, winRate: 0 }
    })
    vi.mocked(statisticsService.getH2HStats).mockResolvedValue({
      content: [], totalPages: 0, totalElements: 0, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)

    expect(wrapper.find('h1').text()).toBe('Leaderboards')
    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ 
        type: 'OVERALL',
        token: 'test-token',
        signal: expect.any(AbortSignal)
    }))

    // Wait for the async data to load
    await vi.waitFor(() => {
        expect(wrapper.text()).toContain('Best Player')
    })

    expect(wrapper.findAll('tr').length).toBe(2) // Header + 1 data row
    expect(statisticsService.getLeaderboard).toHaveBeenCalledTimes(1)
  })

  it('switches to personal stats and H2H when My Stats tab is clicked', async () => {
    const mockPersonalStats = {
      playerId: 'user-1',
      playerName: 'Test User',
      overall: { matches: 20, wins: 15, losses: 5, winRate: 75.0 },
      attacker: { matches: 10, wins: 8, losses: 2, winRate: 80.0 },
      defender: { matches: 10, wins: 7, losses: 3, winRate: 70.0 }
    }
    const mockH2H = {
      content: [{ opponentId: 'opp-1', opponentName: 'Rival 1', matches: 5, wins: 3, losses: 2, winRate: 60.0 }],
      totalPages: 1, totalElements: 1, size: 10, number: 0
    }
    
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 0, totalElements: 0, size: 10, number: 0
    })
    vi.mocked(statisticsService.getPersonalStats).mockResolvedValue(mockPersonalStats)
    vi.mocked(statisticsService.getH2HStats).mockResolvedValue(mockH2H)

    const wrapper = mount(LeaderboardView)
    
    // Switch to My Stats tab
    const myStatsTab = wrapper.findAll('button').find(b => b.text().includes('My Stats'))
    await myStatsTab?.trigger('click')

    expect(statisticsService.getPersonalStats).toHaveBeenCalledWith(expect.objectContaining({ 
        token: 'test-token',
        period: 'ALL_TIME'
    }))
    expect(statisticsService.getH2HStats).toHaveBeenCalledWith(expect.objectContaining({ 
        token: 'test-token',
        period: 'ALL_TIME'
    }))

    await vi.waitFor(() => {
        expect(wrapper.text()).toContain('75.0%')
        expect(wrapper.text()).toContain('Rival 1')
    })
    expect(wrapper.text()).toContain('Attacker')
    expect(wrapper.text()).toContain('Defender')
  })

  it('changes type when tab is clicked', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 0, totalElements: 0, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    
    const attackerTab = wrapper.findAll('button').find(b => b.text().includes('Attacker'))
    await attackerTab?.trigger('click')

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ 
        type: 'ATTACKER',
        signal: expect.any(AbortSignal)
    }))
  })

  it('handles pagination correctly', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 3, totalElements: 30, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('Page 1 of 3'))

    const nextButton = wrapper.findAll('button').find(b => b.text().includes('Next'))
    await nextButton?.trigger('click')

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ page: 1 }))
  })

  it('handles page size change correctly', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 1, totalElements: 5, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    const select = wrapper.find('select#page-size-selector')
    await select.setValue(20)

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ size: 20, page: 0 }))
  })

  it('displays error message on fetch failure', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockRejectedValue(new Error('API Error'))

    const wrapper = mount(LeaderboardView)
    await vi.waitFor(() => expect(wrapper.text()).toContain('API Error'))

    const retryButton = wrapper.find('button.bg-red-600')
    await retryButton.trigger('click')
    expect(statisticsService.getLeaderboard).toHaveBeenCalledTimes(2)
  })

  it('filters by time period correctly', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 1, totalElements: 0, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    
    const select = wrapper.find('select#period-selector')
    await select.setValue('MONTHLY')

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ 
        period: 'MONTHLY',
        page: 0 
    }))
  })

  it('filters by minimum matches threshold correctly', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 1, totalElements: 0, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    
    const select = wrapper.find('select#min-matches-selector')
    await select.setValue(10)

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ 
        minMatches: 10,
        page: 0 
    }))
  })

  it('filters H2H statistics by positions correctly', async () => {
    vi.mocked(statisticsService.getLeaderboard).mockResolvedValue({
      content: [], totalPages: 0, totalElements: 0, size: 10, number: 0
    })
    vi.mocked(statisticsService.getPersonalStats).mockResolvedValue({
      playerId: 'user-1', playerName: 'Test User', 
      overall: { matches: 0, wins: 0, losses: 0, winRate: 0 },
      attacker: { matches: 0, wins: 0, losses: 0, winRate: 0 },
      defender: { matches: 0, wins: 0, losses: 0, winRate: 0 }
    })
    vi.mocked(statisticsService.getH2HStats).mockResolvedValue({
      content: [], totalPages: 0, totalElements: 0, size: 10, number: 0
    })

    const wrapper = mount(LeaderboardView)
    
    // Switch to My Stats tab
    const myStatsTab = wrapper.findAll('button').find(b => b.text().includes('My Stats'))
    await myStatsTab?.trigger('click')
    
    // Wait for initial H2H fetch
    await vi.waitFor(() => expect(statisticsService.getH2HStats).toHaveBeenCalled())
    vi.mocked(statisticsService.getH2HStats).mockClear()

    // Find and change "Me as" filter
    const myPosSelect = wrapper.find('select#my-pos-selector')
    await myPosSelect.setValue('ATTACKER')

    await vi.waitFor(() => {
        expect(statisticsService.getH2HStats).toHaveBeenCalledWith(expect.objectContaining({ 
            myPosition: 'ATTACKER',
            opponentPosition: 'OVERALL'
        }))
    })
    vi.mocked(statisticsService.getH2HStats).mockClear()

    // Find and change "Opponent as" filter
    const oppPosSelect = wrapper.find('select#opp-pos-selector')
    await oppPosSelect.setValue('DEFENDER')

    await vi.waitFor(() => {
        expect(statisticsService.getH2HStats).toHaveBeenCalledWith(expect.objectContaining({ 
            myPosition: 'ATTACKER',
            opponentPosition: 'DEFENDER'
        }))
    })
  })
})
