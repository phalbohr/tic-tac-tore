import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MatchScoring from '../MatchScoring.vue'

const mockPlayers = {
  creator: { id: 1, name: 'Alice' },
  teammate: { id: 2, name: 'Bob' },
  opponent1: { id: 3, name: 'Charlie' },
  opponent2: { id: 4, name: 'David' }
}

describe('MatchScoring', () => {
  it('renders Game 1 with position selection', () => {
    const wrapper = mount(MatchScoring, {
      props: { players: mockPlayers }
    })

    expect(wrapper.find('h3').text()).toContain('Game 1')
    expect(wrapper.findAll('select').length).toBe(2) // Positions for both teams
  })

  it('enforces position swap for Game 2', async () => {
    const wrapper = mount(MatchScoring, {
      props: { players: mockPlayers }
    })

    // Set Game 1 positions and score
    const selects = wrapper.findAll('select')
    await selects[0].setValue('Alice-Bob') // Alice Attacker, Bob Defender
    await selects[1].setValue('Charlie-David') // Charlie Attacker, David Defender
    
    await wrapper.find('input[name="team1Score"]').setValue(10)
    await wrapper.find('input[name="team2Score"]').setValue(5)
    
    await wrapper.find('button.next-game').trigger('click')

    expect(wrapper.find('h3').text()).toContain('Game 2')
    
    // Check if positions are swapped automatically and read-only
    const displayPositions = wrapper.findAll('.position-display')
    expect(displayPositions[0].text()).toContain('Bob') // Bob should be Attacker now
    expect(displayPositions[1].text()).toContain('Alice') // Alice should be Defender
  })

  it('allows finishing after 2 games if score is 2-0', async () => {
    const wrapper = mount(MatchScoring, {
      props: { players: mockPlayers }
    })

    // Game 1: 10-5
    await wrapper.find('input[name="team1Score"]').setValue(10)
    await wrapper.find('input[name="team2Score"]').setValue(5)
    await wrapper.find('button.next-game').trigger('click')

    // Game 2: 10-8
    await wrapper.find('input[name="team1Score"]').setValue(10)
    await wrapper.find('input[name="team2Score"]').setValue(8)

    expect(wrapper.find('button.finish-match').exists()).toBe(true)
  })

  it('requires 3rd game if score is 1-1', async () => {
    const wrapper = mount(MatchScoring, {
      props: { players: mockPlayers }
    })

    // Game 1: 10-5 (Team 1 wins)
    await wrapper.find('input[name="team1Score"]').setValue(10)
    await wrapper.find('input[name="team2Score"]').setValue(5)
    await wrapper.find('button.next-game').trigger('click')

    // Game 2: 5-10 (Team 2 wins)
    await wrapper.find('input[name="team1Score"]').setValue(5)
    await wrapper.find('input[name="team2Score"]').setValue(10)

    expect(wrapper.find('button.next-game').text()).toContain('Game 3')
  })
})
