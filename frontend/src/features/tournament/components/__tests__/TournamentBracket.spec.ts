import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TournamentBracket from '@/features/tournament/components/TournamentBracket.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: { round?: number }) => {
      if (key === 'tournament.bracket.final') return 'Final'
      if (key === 'tournament.bracket.semifinals') return 'Semifinals'
      if (key === 'tournament.bracket.quarterfinals') return 'Quarterfinals'
      if (key === 'tournament.bracket.round') return `Round ${params?.round}`
      return key
    },
    te: () => true,
  }),
}))

describe('TournamentBracket.vue (Story 8.3)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const mockBracket = {
    tournamentId: 'tourn-1',
    tournamentName: 'Autumn Cup 2026',
    format: 'CUP' as const,
    mode: 'ONE_VS_ONE_PERSONAL' as const,
    status: 'IN_PROGRESS' as const,
    totalRounds: 2,
    rounds: [
      {
        round: 1,
        roundName: 'Semifinals',
        matches: [
          {
            id: 'm-1',
            tournamentId: 'tourn-1',
            round: 1,
            matchOrder: 1,
            participant1: {
              id: 'reg-1',
              playerNickname: 'Alice',
              seed: 1,
              tournamentId: 'tourn-1',
              tournamentName: 'Autumn Cup 2026',
              playerId: 'p-1',
              status: 'CONFIRMED' as const,
              createdAt: '',
            },
            participant2: {
              id: 'reg-4',
              playerNickname: 'Dave',
              seed: 4,
              tournamentId: 'tourn-1',
              tournamentName: 'Autumn Cup 2026',
              playerId: 'p-4',
              status: 'CONFIRMED' as const,
              createdAt: '',
            },
            seed1: 1,
            seed2: 4,
            status: 'READY' as const,
          },
          {
            id: 'm-2',
            tournamentId: 'tourn-1',
            round: 1,
            matchOrder: 2,
            participant1: {
              id: 'reg-2',
              playerNickname: 'Bob',
              seed: 2,
              tournamentId: 'tourn-1',
              tournamentName: 'Autumn Cup 2026',
              playerId: 'p-2',
              status: 'CONFIRMED' as const,
              createdAt: '',
            },
            participant2: null,
            seed1: 2,
            seed2: null,
            status: 'BYE' as const,
            winnerRegistrationId: 'reg-2',
          },
        ],
      },
      {
        round: 2,
        roundName: 'Final',
        matches: [
          {
            id: 'm-3',
            tournamentId: 'tourn-1',
            round: 2,
            matchOrder: 1,
            participant1: null,
            participant2: {
              id: 'reg-2',
              playerNickname: 'Bob',
              seed: 2,
              tournamentId: 'tourn-1',
              tournamentName: 'Autumn Cup 2026',
              playerId: 'p-2',
              status: 'CONFIRMED' as const,
              createdAt: '',
            },
            status: 'PENDING' as const,
          },
        ],
      },
    ],
    seededParticipants: [
      {
        id: 'reg-1',
        playerNickname: 'Alice',
        seed: 1,
        tournamentId: 'tourn-1',
        tournamentName: 'Autumn Cup 2026',
        playerId: 'p-1',
        status: 'CONFIRMED' as const,
        createdAt: '',
      },
      {
        id: 'reg-2',
        playerNickname: 'Bob',
        seed: 2,
        tournamentId: 'tourn-1',
        tournamentName: 'Autumn Cup 2026',
        playerId: 'p-2',
        status: 'CONFIRMED' as const,
        createdAt: '',
      },
      {
        id: 'reg-4',
        playerNickname: 'Dave',
        seed: 4,
        tournamentId: 'tourn-1',
        tournamentName: 'Autumn Cup 2026',
        playerId: 'p-4',
        status: 'CONFIRMED' as const,
        createdAt: '',
      },
    ],
  }

  it('renders all rounds and column headers correctly', () => {
    const wrapper = mount(TournamentBracket, {
      props: {
        bracket: mockBracket,
      },
    })

    expect(wrapper.text()).toContain('Semifinals')
    expect(wrapper.text()).toContain('Final')
  })

  it('displays participant seeds (#1, #2, #4) and nicknames', () => {
    const wrapper = mount(TournamentBracket, {
      props: {
        bracket: mockBracket,
      },
    })

    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).toContain('Dave')
    expect(wrapper.text()).toContain('#1')
    expect(wrapper.text()).toContain('#2')
  })

  it('renders BYE badge on unseeded opponent match slot', () => {
    const wrapper = mount(TournamentBracket, {
      props: {
        bracket: mockBracket,
      },
    })

    expect(wrapper.text()).toContain('BYE')
  })
})
