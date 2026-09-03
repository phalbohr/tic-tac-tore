import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TournamentSchedule from '@/features/tournament/components/TournamentSchedule.vue'
import type { TournamentBracketDto } from '@/features/tournament/types/tournament'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const messages: Record<string, string> = {
        'tournament.match.allRounds': 'All Rounds',
        'tournament.match.myMatches': 'My Matches',
        'tournament.match.available': 'Available to Play',
        'tournament.match.start': 'Start Match',
        'tournament.match.opponent_busy': 'Opponent Busy',
      }
      return messages[key] || key
    },
    te: () => true,
  }),
}))

describe('TournamentSchedule.vue (Story 8.5: Asynchronous Tournament Match Execution)', () => {
  const mockBracket: TournamentBracketDto = {
    tournamentId: 'tourn-1',
    tournamentName: 'Championship 2026',
    format: 'CHAMPIONSHIP',
    mode: 'ONE_VS_ONE_PERSONAL',
    status: 'IN_PROGRESS',
    totalRounds: 2,
    seededParticipants: [],
    rounds: [
      {
        round: 1,
        roundName: 'Round 1',
        matches: [
          {
            id: 'm-1',
            tournamentId: 'tourn-1',
            round: 1,
            matchOrder: 1,
            participant1: {
              id: 'reg-1',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-alice',
              playerNickname: 'Alice',
              status: 'CONFIRMED',
              createdAt: '',
            },
            participant2: {
              id: 'reg-2',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-bob',
              playerNickname: 'Bob',
              status: 'CONFIRMED',
              createdAt: '',
            },
            status: 'READY',
            isAvailable: true,
            isOpponentBusy: false,
          },
          {
            id: 'm-2',
            tournamentId: 'tourn-1',
            round: 1,
            matchOrder: 2,
            participant1: {
              id: 'reg-3',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-charlie',
              playerNickname: 'Charlie',
              status: 'CONFIRMED',
              createdAt: '',
            },
            participant2: {
              id: 'reg-4',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-david',
              playerNickname: 'David',
              status: 'CONFIRMED',
              createdAt: '',
            },
            status: 'READY',
            isAvailable: false,
            isOpponentBusy: true,
          },
        ],
      },
      {
        round: 2,
        roundName: 'Round 2',
        matches: [
          {
            id: 'm-3',
            tournamentId: 'tourn-1',
            round: 2,
            matchOrder: 1,
            participant1: {
              id: 'reg-1',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-alice',
              playerNickname: 'Alice',
              status: 'CONFIRMED',
              createdAt: '',
            },
            participant2: {
              id: 'reg-3',
              tournamentId: 'tourn-1',
              tournamentName: 'Championship 2026',
              playerId: 'user-charlie',
              playerNickname: 'Charlie',
              status: 'CONFIRMED',
              createdAt: '',
            },
            status: 'READY',
            isAvailable: true,
            isOpponentBusy: false,
          },
        ],
      },
    ],
  }

  it('renders all rounds and filter chips (AC1, AC4)', () => {
    const wrapper = mount(TournamentSchedule, {
      props: {
        bracket: mockBracket,
        currentUserId: 'user-alice',
      },
    })

    expect(wrapper.find('[data-testid="filter-all"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="filter-my"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="filter-available"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-testid="tournament-match-card"]')).toHaveLength(3)
  })

  it('filters to My Matches when filter-my chip is clicked (AC4)', async () => {
    const wrapper = mount(TournamentSchedule, {
      props: {
        bracket: mockBracket,
        currentUserId: 'user-alice',
      },
    })

    await wrapper.find('[data-testid="filter-my"]').trigger('click')

    const cards = wrapper.findAll('[data-testid="tournament-match-card"]')
    expect(cards).toHaveLength(2) // m-1 and m-3 involve Alice
  })

  it('filters to Available to Play matches when filter-available chip is clicked (AC4)', async () => {
    const wrapper = mount(TournamentSchedule, {
      props: {
        bracket: mockBracket,
        currentUserId: 'user-alice',
      },
    })

    await wrapper.find('[data-testid="filter-available"]').trigger('click')

    const cards = wrapper.findAll('[data-testid="tournament-match-card"]')
    expect(cards).toHaveLength(2) // m-1 and m-3 are available; m-2 is opponent busy
  })
})
