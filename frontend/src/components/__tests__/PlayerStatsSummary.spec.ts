import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PlayerStatsSummary from '../PlayerStatsSummary.vue'

const mockStats = {
  playerId: 'user-uuid',
  playerName: 'Current Player',
  overall: { matches: 20, wins: 15, losses: 5, winRate: 75.0 },
  attacker: { matches: 10, wins: 8, losses: 2, winRate: 80.0 },
  defender: { matches: 10, wins: 7, losses: 3, winRate: 70.0 }
}

describe('PlayerStatsSummary', () => {
  it('renders overall stats correctly', () => {
    const wrapper = mount(PlayerStatsSummary, {
      props: { stats: mockStats }
    })

    expect(wrapper.text()).toContain('20') // Overall Matches
    expect(wrapper.text()).toContain('15') // Overall Wins
    expect(wrapper.text()).toContain('5')  // Overall Losses
    expect(wrapper.text()).toContain('75.0%') // Overall Win Rate
  })

  it('renders position breakdown correctly', () => {
    const wrapper = mount(PlayerStatsSummary, {
      props: { stats: mockStats }
    })

    const attackerSection = wrapper.find('[data-test="attacker-stats"]')
    expect(attackerSection.text()).toContain('80.0%')
    expect(attackerSection.text()).toContain('8 wins')

    const defenderSection = wrapper.find('[data-test="defender-stats"]')
    expect(defenderSection.text()).toContain('70.0%')
    expect(defenderSection.text()).toContain('7 wins')
  })

  it('shows empty state when no matches played', () => {
    const emptyStats = {
      ...mockStats,
      overall: { matches: 0, wins: 0, losses: 0, winRate: 0.0 }
    }
    const wrapper = mount(PlayerStatsSummary, {
      props: { stats: emptyStats }
    })

    expect(wrapper.text()).toContain('0.0%')
    expect(wrapper.text()).toContain('0 matches')
  })
})
