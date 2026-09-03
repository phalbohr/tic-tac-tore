import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingMatches, { type PendingMatchItem } from '../PendingMatches.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: Record<string, unknown>) => {
      if (key === 'match.pendingMatch') return `Match ${params?.number ?? 1}`
      if (key === 'match.partialConfirmation')
        return `${params?.confirmed ?? 1} of ${params?.required ?? 2} confirmed`
      const translations: Record<string, string> = {
        'match.pending': 'Pending Confirmation',
        'match.confirm': 'Confirm',
        'match.confirmedTapUndo': 'Match confirmed. Tap to undo.',
        'match.teamA': 'Team A',
        'match.teamB': 'Team B',
        'match.scores': 'Scores',
      }
      return translations[key] || key
    },
  }),
}))

describe('PendingMatches.vue Cooldown Countdown (ATDD Red Phase)', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.clearAllMocks()
  })

  it('[P0] RED: Should render cooldown timer text for PARTIALLY_CONFIRMED match with future expiry', () => {
    const futureExpiry = new Date(Date.now() + 5 * 60 * 1000).toISOString()
    const matches: PendingMatchItem[] = [
      {
        id: 'm-cooldown',
        status: 'PARTIALLY_CONFIRMED',
        cooldownExpiresAt: futureExpiry,
        confirmedByOpponentIds: ['opponent-a'],
        requiredConfirmations: 2,
        teamANames: ['A1'],
        teamBNames: ['B1'],
        games: [{ teamAScore: 10, teamBScore: 8 }],
      },
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: matches },
    })

    expect(wrapper.html()).toContain('Auto-publish in')
  })

  it('[P0] RED: Should render "Auto-publishing soon" when cooldownExpiresAt is in the past', () => {
    const pastExpiry = new Date(Date.now() - 60_000).toISOString()
    const matches: PendingMatchItem[] = [
      {
        id: 'm-expired',
        status: 'PARTIALLY_CONFIRMED',
        cooldownExpiresAt: pastExpiry,
        confirmedByOpponentIds: ['opponent-a'],
        requiredConfirmations: 2,
        teamANames: ['A1'],
        teamBNames: ['B1'],
        games: [{ teamAScore: 10, teamBScore: 8 }],
      },
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: matches },
    })

    expect(wrapper.html()).toContain('Auto-publishing soon')
  })

  it('[P0] RED: Should hide cooldown timer when cooldownExpiresAt is absent', () => {
    const matches: PendingMatchItem[] = [
      {
        id: 'm-no-cooldown',
        status: 'PARTIALLY_CONFIRMED',
        confirmedByOpponentIds: ['opponent-a'],
        requiredConfirmations: 2,
        teamANames: ['A1'],
        teamBNames: ['B1'],
        games: [{ teamAScore: 10, teamBScore: 8 }],
      },
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: matches },
    })

    expect(wrapper.html()).not.toContain('Auto-publish in')
    expect(wrapper.html()).not.toContain('Auto-publishing soon')
  })

  it('[P0] RED: Should display partial confirmation badge text for PARTIALLY_CONFIRMED match', () => {
    const matches: PendingMatchItem[] = [
      {
        id: 'm-partial',
        status: 'PARTIALLY_CONFIRMED',
        confirmedByOpponentIds: ['opponent-a'],
        requiredConfirmations: 2,
        teamANames: ['A1'],
        teamBNames: ['B1'],
        games: [{ teamAScore: 10, teamBScore: 8 }],
      },
    ]

    const wrapper = mount(PendingMatches, {
      props: { pendingMatches: matches },
    })

    expect(wrapper.text()).toContain('1 of 2 confirmed')
  })
})
