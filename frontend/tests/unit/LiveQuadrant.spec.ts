import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import LiveQuadrant from '@/features/match/LiveQuadrant.vue'

describe('LiveQuadrant', () => {
  it('emits score event with correct playerId and role on touch', async () => {
    const wrapper = mount(LiveQuadrant, {
      props: {
        playerId: 'p1',
        playerName: 'Alice',
        role: 'teamA.attacker'
      }
    })

    await wrapper.trigger('touchstart')
    
    expect(wrapper.emitted()).toHaveProperty('score')
    const scoreEvent = wrapper.emitted('score')
    expect(scoreEvent![0]).toEqual(['p1', 'teamA.attacker'])
  })

  it('flashes temporarily when touched', async () => {
    const wrapper = mount(LiveQuadrant, {
      props: {
        playerId: 'p1',
        playerName: 'Alice',
        role: 'teamA.attacker'
      }
    })

    expect(wrapper.classes()).not.toContain('ch-bg-green-500')
    await wrapper.trigger('touchstart')
    expect(wrapper.classes()).toContain('ch-bg-green-500')
  })
})
