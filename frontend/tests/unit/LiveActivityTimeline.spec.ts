import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import LiveActivityTimeline from '@/features/match/LiveActivityTimeline.vue'

describe('[Story 5.2] LiveActivityTimeline Component', () => {
  it.skip('[P0] displays empty state indicator when no goals are provided', () => {
    const wrapper = mount(LiveActivityTimeline, {
      props: {
        goals: [],
      },
    })

    const empty = wrapper.find('[data-testid="timeline-empty"]')
    expect(empty.exists()).toBe(true)
    expect(empty.text()).toMatch(/no goals/i)
  })

  it.skip('[P0] renders goal events in reverse chronological order with scorer name and role', () => {
    const mockGoals = [
      {
        id: 'g1',
        playerId: 'p1',
        playerName: 'Alice',
        quadrantRole: 'teamA.attacker',
        timestamp: 1000,
      },
      {
        id: 'g2',
        playerId: 'p3',
        playerName: 'Charlie',
        quadrantRole: 'teamB.attacker',
        timestamp: 2000,
      },
    ]

    const wrapper = mount(LiveActivityTimeline, {
      props: {
        goals: mockGoals,
      },
    })

    const items = wrapper.findAll('[data-testid="timeline-goal-item"]')
    expect(items).toHaveLength(2)
    // Reverse chronological order: newest (Charlie) at the top
    expect(items[0].text()).toContain('Charlie')
    expect(items[0].text()).toContain('teamB.attacker')
    expect(items[1].text()).toContain('Alice')
    expect(items[1].text()).toContain('teamA.attacker')
  })

  it.skip('[P1] contains scroll and height constraint classes for landscape HUD layout', () => {
    const wrapper = mount(LiveActivityTimeline, {
      props: {
        goals: [],
      },
    })

    const container = wrapper.find('[data-testid="live-activity-timeline"]')
    expect(container.exists()).toBe(true)
    expect(container.classes()).toEqual(expect.arrayContaining(['overflow-y-auto']))
  })
})
