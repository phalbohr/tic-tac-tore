import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingMatches, { type PendingMatchItem } from '../PendingMatches.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'match.pending': 'Pending Confirmation',
        'match.confirm': 'Confirm',
        'match.confirmedTapUndo': 'Match confirmed. Tap to undo.',
        'match.teamA': 'Team A',
        'match.teamB': 'Team B',
        'match.scores': 'Scores'
      }
      return translations[key] || key
    }
  })
}))

describe('PendingMatches.vue', () => {
  it('renders nothing when pendingMatches is empty', () => {
    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: [] }
    })
    expect(wrapper.html()).toBe('<!--v-if-->')
  })

  it('renders 3-column match details with Team A roster, scores, and Team B roster', () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-1',
        creatorNickname: 'Alice',
        teamANames: ['Alice', 'Bob'],
        teamBNames: ['Charlie', 'Dave'],
        games: [
          { teamAScore: 10, teamBScore: 8 },
          { teamAScore: 7, teamBScore: 10 },
          { teamAScore: 10, teamBScore: 5 }
        ],
        createdAt: new Date().toISOString()
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    expect(wrapper.text()).toContain('Team A')
    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('Bob')

    expect(wrapper.text()).toContain('Team B')
    expect(wrapper.text()).toContain('Charlie')
    expect(wrapper.text()).toContain('Dave')

    expect(wrapper.text()).toContain('10 : 8')
    expect(wrapper.text()).toContain('7 : 10')
    expect(wrapper.text()).toContain('10 : 5')
  })

  it('emits confirm event when confirm button is clicked', async () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-99',
        teamANames: ['Player 1'],
        teamBNames: ['Player 2'],
        games: [{ teamAScore: 10, teamBScore: 6 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const btn = wrapper.find('[data-testid="confirm-match-btn-match-99"]')
    expect(btn.exists()).toBe(true)
    await btn.trigger('click')

    expect(wrapper.emitted('confirm')).toBeTruthy()
    expect(wrapper.emitted('confirm')![0]).toEqual(['match-99'])
  })
})
