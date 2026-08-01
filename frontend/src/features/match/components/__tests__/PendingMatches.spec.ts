import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingMatches, { type PendingMatchItem } from '../PendingMatches.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: Record<string, unknown>) => {
      if (key === 'match.pendingMatch') return `Match ${params?.number ?? 1}`
      if (key === 'match.matchConfirmedTapUndo') return `Match ${params?.number ?? 1} confirmed. Tap to undo.`
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

  it('renders sequential match numbers for cards in queue order', () => {
    const sampleMatches: PendingMatchItem[] = [
      { id: 'm1', teamANames: ['A1'], teamBNames: ['B1'], games: [{ teamAScore: 10, teamBScore: 5 }] },
      { id: 'm2', teamANames: ['A2'], teamBNames: ['B2'], games: [{ teamAScore: 10, teamBScore: 8 }] },
      { id: 'm3', teamANames: ['A3'], teamBNames: ['B3'], games: [{ teamAScore: 7, teamBScore: 10 }] }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    expect(wrapper.text()).toContain('Match 1')
    expect(wrapper.text()).toContain('Match 2')
    expect(wrapper.text()).toContain('Match 3')
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

  it('emits confirm event with matchId and matchNumber when confirm button is clicked', async () => {
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
    expect(wrapper.emitted('confirm')![0]).toEqual(['match-99', 1])
  })

  it('displays confirmed status when match ID is in pendingConfirmationIds prop array', () => {
    const sampleMatches: PendingMatchItem[] = [
      { id: 'match-1', teamANames: ['P1'], teamBNames: ['P2'], games: [{ teamAScore: 10, teamBScore: 2 }] },
      { id: 'match-2', teamANames: ['P3'], teamBNames: ['P4'], games: [{ teamAScore: 10, teamBScore: 5 }] }
    ]

    const wrapper = mount(PendingMatches, {
      props: {
        pendingMatches: sampleMatches,
        pendingConfirmationIds: ['match-1']
      }
    })

    const card1 = wrapper.find('[data-testid="pending-match-card-match-1"]')
    expect(card1.text()).toContain('Match confirmed. Tap to undo.')

    const btn2 = wrapper.find('[data-testid="confirm-match-btn-match-2"]')
    expect(btn2.exists()).toBe(true)
  })

  it('displays rejection reason text and hides action buttons when match status is REJECTED', () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-rej',
        status: 'REJECTED',
        rejectionReason: 'Wrong score',
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const rejectionReasonEl = wrapper.find('[data-testid="rejection-reason-match-rej"]')
    expect(rejectionReasonEl.exists()).toBe(true)
    expect(rejectionReasonEl.text()).toContain('Wrong score')

    const confirmBtn = wrapper.find('[data-testid="confirm-match-btn-match-rej"]')
    const rejectBtn = wrapper.find('[data-testid="reject-match-btn-match-rej"]')
    expect(confirmBtn.exists()).toBe(false)
    expect(rejectBtn.exists()).toBe(false)

    const editBtn = wrapper.find('[data-testid="edit-rejection-btn-match-rej"]')
    const deleteBtn = wrapper.find('[data-testid="delete-rejection-btn-match-rej"]')
    expect(editBtn.exists()).toBe(true)
    expect(deleteBtn.exists()).toBe(true)
  })

  it('emits edit-rejection event when Edit Match button is clicked', async () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-edit-1',
        status: 'REJECTED',
        rejectionReason: 'Wrong score',
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const editBtn = wrapper.find('[data-testid="edit-rejection-btn-match-edit-1"]')
    await editBtn.trigger('click')

    const editEmitted = wrapper.emitted('edit-rejection')
    expect(editEmitted).toBeTruthy()
    expect(editEmitted?.[0]?.[0]).toEqual(sampleMatches[0])
  })

  it('emits delete-rejection event when Delete Match button is clicked', async () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-del-1',
        status: 'REJECTED',
        rejectionReason: 'Wrong score',
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const deleteBtn = wrapper.find('[data-testid="delete-rejection-btn-match-del-1"]')
    await deleteBtn.trigger('click')

    const deleteEmitted = wrapper.emitted('delete-rejection')
    expect(deleteEmitted).toBeTruthy()
    expect(deleteEmitted?.[0]?.[0]).toBe('match-del-1')
  })
})
