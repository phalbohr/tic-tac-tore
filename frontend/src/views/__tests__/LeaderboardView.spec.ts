import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LeaderboardView from '../LeaderboardView.vue'
import * as statisticsService from '../../services/statisticsService'
import { useAuthStore } from '@/stores/auth'

vi.mock('../../services/statisticsService', () => ({
  getLeaderboard: vi.fn()
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
    
    const input = wrapper.find('input#min-matches-input')
    await input.setValue(5)

    expect(statisticsService.getLeaderboard).toHaveBeenCalledWith(expect.objectContaining({ 
        minMatches: 5,
        page: 0 
    }))
  })
})
