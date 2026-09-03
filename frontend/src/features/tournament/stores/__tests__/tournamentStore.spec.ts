import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'
import * as tournamentService from '@/features/tournament/services/tournamentService'

describe('tournamentStore archive & standings actions', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchStandings should retrieve standings from service and update state', async () => {
    const store = useTournamentStore()
    const mockStandings = [
      {
        registrationId: 'reg-1',
        userId: 'user-1',
        nickname: 'Alice',
        matchesPlayed: 3,
        wins: 3,
        losses: 0,
        gamesWon: 6,
        gamesLost: 1,
        gameDifference: 5,
        points: 9,
        isEliminated: false,
        rank: 1,
      },
    ]

    vi.spyOn(tournamentService, 'getTournamentStandings').mockImplementation(async () => {
      expect(store.isStandingsLoading).toBe(true)
      expect(store.isLoading).toBe(false)
      return mockStandings
    })

    await store.fetchStandings('tourn-uuid-1')

    expect(tournamentService.getTournamentStandings).toHaveBeenCalledWith('tourn-uuid-1')
    expect(store.standings['tourn-uuid-1']).toEqual(mockStandings)
    expect(store.isStandingsLoading).toBe(false)
    expect(store.isLoading).toBe(false)
  })

  it('fetchArchive should retrieve paginated completed tournaments and update state', async () => {
    const store = useTournamentStore()
    const mockArchiveResponse = {
      content: [
        {
          id: 't-arch-1',
          name: 'Spring Cup 2026',
          status: 'COMPLETED' as const,
          format: 'CUP' as const,
          mode: 'ONE_VS_ONE_PERSONAL' as const,
          ruleConfiguration: {
            id: 'rc-1',
            name: 'Default',
            goalLimit: 5,
            gameLimit: 1,
            winByTwo: false,
          },
          minParticipants: 4,
          maxParticipants: 8,
          registrationDeadline: '2026-05-01T12:00:00Z',
          hasPlayoff: false,
          creatorId: 'u-1',
          creatorNickname: 'Host',
          createdAt: '2026-05-01T12:00:00Z',
        },
      ],
      totalPages: 1,
      totalElements: 1,
      number: 0,
      size: 10,
    }

    vi.spyOn(tournamentService, 'getTournamentsPaginated').mockResolvedValue(mockArchiveResponse)

    await store.fetchArchive(0, 10)

    expect(tournamentService.getTournamentsPaginated).toHaveBeenCalledWith('COMPLETED', 0, 10)
    expect(store.archiveTournaments).toHaveLength(1)
    expect(store.archiveTournaments[0]?.id).toBe('t-arch-1')
    expect(store.archiveTotalPages).toBe(1)
  })
})
