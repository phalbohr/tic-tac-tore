import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import MatchGameRow from '../MatchGameRow.vue'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultMessage?: string) => defaultMessage || key
    })
  }
})

describe('MatchGameRow', () => {
  it('renders avatars and score correctly', () => {
    const wrapper = mount(MatchGameRow, {
      props: {
        teamADefender: { name: 'Defender A' },
        teamAAttacker: { name: 'Attacker A' },
        teamBDefender: { name: 'Defender B' },
        teamBAttacker: { name: 'Attacker B' },
        teamAScore: 5,
        teamBScore: 3,
        showScore: true
      }
    })

    expect(wrapper.find('[data-testid="score-vs-display"]').text()).toBe('5 : 3')
    expect(wrapper.find('[data-testid="team-a-defender-avatar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="team-a-attacker-avatar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="team-b-defender-avatar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="team-b-attacker-avatar"]').exists()).toBe(true)
  })

  it('renders VS when showScore is false', () => {
    const wrapper = mount(MatchGameRow, {
      props: {
        showScore: false
      }
    })

    expect(wrapper.find('[data-testid="score-vs-display"]').text()).toBe('VS')
  })

  it('does not render swap buttons when allowSwap is false', () => {
    const wrapper = mount(MatchGameRow, {
      props: {
        teamADefender: { name: 'Defender A' },
        teamAAttacker: { name: 'Attacker A' },
        allowSwap: false
      }
    })

    expect(wrapper.find('[data-testid="swap-team-a-btn"]').exists()).toBe(false)
  })

  it('renders swap buttons and emits swap event when allowSwap is true', async () => {
    const wrapper = mount(MatchGameRow, {
      props: {
        teamADefender: { name: 'Defender A' },
        teamAAttacker: { name: 'Attacker A' },
        allowSwap: true
      }
    })

    const swapBtn = wrapper.find('[data-testid="swap-team-a-btn"]')
    expect(swapBtn.exists()).toBe(true)
    await swapBtn.trigger('click')
    expect(wrapper.emitted('swap')?.[0]).toEqual([1])
  })
})
