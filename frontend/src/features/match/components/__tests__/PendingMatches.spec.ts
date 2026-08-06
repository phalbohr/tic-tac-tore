import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingMatches, { type PendingMatchItem } from '../PendingMatches.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: Record<string, unknown>) => {
      if (key === 'match.pendingMatch') return `Match ${params?.number ?? 1}`
       if (key === 'match.matchConfirmedTapUndo') return `Match ${params?.number ?? 1} confirmed. Tap to undo.`
       if (key === 'match.partialConfirmation') return `${params?.confirmed ?? 1} of ${params?.required ?? 2} confirmed`
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
    expect(wrapper.html()).toContain('Alice')
    expect(wrapper.html()).toContain('Bob')

    expect(wrapper.text()).toContain('Team B')
    expect(wrapper.html()).toContain('Charlie')
    expect(wrapper.html()).toContain('Dave')

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

  it('displays partial confirmation progress text for PARTIALLY_CONFIRMED matches', () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-partial',
        status: 'PARTIALLY_CONFIRMED',
        confirmedByOpponentIds: ['opp-1'],
        requiredConfirmations: 2,
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const badge = wrapper.find('[data-testid="partially-confirmed-badge-match-partial"]')
    expect(badge.exists()).toBe(true)
    expect(badge.text()).toContain('1 of 2 confirmed')
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

  it('correctly passes defender fallback name (teamBNames[1]) to MatchGameRow in rejected matches', () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-rej-names',
        status: 'REJECTED',
        rejectionReason: 'Wrong score',
        teamANames: ['Attacker A', 'Defender A'],
        teamBNames: ['Attacker B', 'Defender B'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const gameRow = wrapper.findComponent({ name: 'MatchGameRow' })
    expect(gameRow.exists()).toBe(true)
    expect(gameRow.props('teamBDefender')).toEqual({ name: 'Defender B' })
    expect(gameRow.props('teamBAttacker')).toEqual({ name: 'Attacker B' })
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

  it('opens confirmation modal when Delete Match is clicked, and emits delete-rejection only after Confirm', async () => {
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

    // Modal should be visible
    let modal = wrapper.find('[data-testid="delete-confirmation-modal"]')
    expect(modal.exists()).toBe(true)
    expect(modal.text()).toContain('Cancel Match')
    expect(modal.text()).toContain('Are you sure you want to delete this match? All recorded scores will be lost.')
    expect(wrapper.emitted('delete-rejection')).toBeFalsy()

    // Test Keep Editing button
    const keepEditingBtn = wrapper.find('[data-testid="keep-editing-btn"]')
    await keepEditingBtn.trigger('click')

    modal = wrapper.find('[data-testid="delete-confirmation-modal"]')
    expect(modal.exists()).toBe(false)
    expect(wrapper.emitted('delete-rejection')).toBeFalsy()

    // Open modal again
    await deleteBtn.trigger('click')
    modal = wrapper.find('[data-testid="delete-confirmation-modal"]')
    expect(modal.exists()).toBe(true)

    // Click Confirm button
    const confirmBtn = wrapper.find('[data-testid="confirm-delete-btn"]')
    await confirmBtn.trigger('click')

    modal = wrapper.find('[data-testid="delete-confirmation-modal"]')
    expect(modal.exists()).toBe(false)

    const deleteEmitted = wrapper.emitted('delete-rejection')
    expect(deleteEmitted).toBeTruthy()
    expect(deleteEmitted?.[0]?.[0]).toBe('match-del-1')
  })

  it('renders 3 equal buttons for confirmation requests and emits close when close is clicked', async () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-close-1',
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const rejectBtn = wrapper.find('[data-testid="reject-match-btn-match-close-1"]')
    const confirmBtn = wrapper.find('[data-testid="confirm-match-btn-match-close-1"]')
    const closeBtn = wrapper.find('[data-testid="close-match-btn-match-close-1"]')

    expect(rejectBtn.exists()).toBe(true)
    expect(confirmBtn.exists()).toBe(true)
    expect(closeBtn.exists()).toBe(true)

    expect(rejectBtn.classes()).toContain('hover:bg-red-950/40')
    expect(closeBtn.classes()).toContain('hover:bg-neutral-700')

    await closeBtn.trigger('click')
    const closeEmitted = wrapper.emitted('close')
    expect(closeEmitted).toBeTruthy()
    expect(closeEmitted?.[0]?.[0]).toBe('match-close-1')
  })

  it('renders 3 equal buttons for rejected matches and emits close when close is clicked', async () => {
    const sampleMatches: PendingMatchItem[] = [
      {
        id: 'match-close-2',
        status: 'REJECTED',
        rejectionReason: 'Wrong players',
        teamANames: ['P1'],
        teamBNames: ['P2'],
        games: [{ teamAScore: 10, teamBScore: 5 }]
      }
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: sampleMatches }
    })

    const editBtn = wrapper.find('[data-testid="edit-rejection-btn-match-close-2"]')
    const deleteBtn = wrapper.find('[data-testid="delete-rejection-btn-match-close-2"]')
    const closeBtn = wrapper.find('[data-testid="close-match-btn-match-close-2"]')

    expect(editBtn.exists()).toBe(true)
    expect(deleteBtn.exists()).toBe(true)
    expect(closeBtn.exists()).toBe(true)

    expect(editBtn.classes()).toContain('hover:opacity-90')
    expect(deleteBtn.classes()).toContain('hover:bg-red-900')
    expect(closeBtn.classes()).toContain('hover:bg-neutral-700')

    await closeBtn.trigger('click')
    const closeEmitted = wrapper.emitted('close')
    expect(closeEmitted).toBeTruthy()
    expect(closeEmitted?.[0]?.[0]).toBe('match-close-2')
  })
})
