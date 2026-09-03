import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TournamentStandings from '@/features/tournament/components/TournamentStandings.vue'
import type { TournamentStandingDto } from '@/features/tournament/types/tournament'

describe.skip('TournamentStandings.vue (ATDD red phase: Story 8.7)', () => {
  const mockStandings: TournamentStandingDto[] = [
    {
      registrationId: 'reg-1',
      userId: 'user-1',
      nickname: 'Alice Champion',
      avatarUrl: 'https://example.com/alice.png',
      matchesPlayed: 5,
      wins: 5,
      losses: 0,
      gamesWon: 10,
      gamesLost: 2,
      gameDifference: 8,
      points: 15,
      isEliminated: false,
      rank: 1,
    },
    {
      registrationId: 'reg-2',
      userId: 'user-2',
      nickname: 'Bob RunnerUp',
      avatarUrl: 'https://example.com/bob.png',
      matchesPlayed: 5,
      wins: 3,
      losses: 2,
      gamesWon: 7,
      gamesLost: 5,
      gameDifference: 2,
      points: 9,
      isEliminated: false,
      rank: 2,
    },
    {
      registrationId: 'reg-3',
      userId: 'user-3',
      nickname: 'Charlie Knockout',
      avatarUrl: '',
      matchesPlayed: 2,
      wins: 0,
      losses: 2,
      gamesWon: 1,
      gamesLost: 4,
      gameDifference: -3,
      points: 0,
      isEliminated: true,
      rank: 3,
    },
  ]

  it('renders standings table with ranks, nicknames, games, and points', () => {
    const wrapper = mount(TournamentStandings, {
      props: {
        standings: mockStandings,
        isCompleted: true,
      },
    })

    expect(wrapper.text()).toContain('Alice Champion')
    expect(wrapper.text()).toContain('Bob RunnerUp')
    expect(wrapper.text()).toContain('Charlie Knockout')

    const firstRow = wrapper.findAll('tbody tr')[0]
    expect(firstRow.text()).toContain('1')
    expect(firstRow.text()).toContain('15') // points
    expect(firstRow.text()).toContain('+8') // game diff
  })

  it('displays winner badge for rank 1 in completed tournaments', () => {
    const wrapper = mount(TournamentStandings, {
      props: {
        standings: mockStandings,
        isCompleted: true,
      },
    })

    const winnerBadge = wrapper.find('[data-testid="standing-badge-winner"]')
    expect(winnerBadge.exists()).toBe(true)
  })

  it('displays eliminated badge for eliminated participants in cup tournaments', () => {
    const wrapper = mount(TournamentStandings, {
      props: {
        standings: mockStandings,
        isCompleted: false,
      },
    })

    const eliminatedBadge = wrapper.find('[data-testid="standing-badge-eliminated"]')
    expect(eliminatedBadge.exists()).toBe(true)
  })

  it('renders Anonymous fallback for deleted GDPR users', () => {
    const standingsWithDeleted: TournamentStandingDto[] = [
      {
        registrationId: 'reg-deleted',
        userId: null,
        nickname: 'Anonymous',
        avatarUrl: null,
        matchesPlayed: 1,
        wins: 0,
        losses: 1,
        gamesWon: 0,
        gamesLost: 2,
        gameDifference: -2,
        points: 0,
        isEliminated: true,
        rank: 1,
      },
    ]

    const wrapper = mount(TournamentStandings, {
      props: {
        standings: standingsWithDeleted,
        isCompleted: false,
      },
    })

    expect(wrapper.text()).toContain('Anonymous')
  })
})
