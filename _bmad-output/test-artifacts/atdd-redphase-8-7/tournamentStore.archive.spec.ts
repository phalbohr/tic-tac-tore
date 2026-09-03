import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'
import { tournamentService } from '@/features/tournament/services/tournamentService'

describe.skip('tournamentStore archive & standings actions (ATDD red phase: Story 8.7)', () => {
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

    vi.spyOn(tournamentService, 'getTournamentStandings').mockResolvedValue(mockStandings)

    await store.fetchStandings('tourn-uuid-1')

    expect(tournamentService.getTournamentStandings).toHaveBeenCalledWith('tourn-uuid-1')
    expect(store.standings['tourn-uuid-1']).toEqual(mockStandings)
  })

  it('fetchArchive should retrieve paginated completed tournaments and update state', async () => {
    const store = useTournamentStore()
    const mockArchiveResponse = {
      content: [
        {
          id: 't-arch-1',
          title: 'Spring Cup 2026',
          status: 'COMPLETED' as any,
          format: 'CUP' as any,
          mode: 'ONE_VS_ONE' as any,
          completedAt: '2026-05-01T12:00:00Z',
          participantCount: 8,
        },
      ],
      totalPages: 1,
      totalElements: 1,
    }

    vi.spyOn(tournamentService, 'getTournamentsPaginated').mockResolvedValue(mockArchiveResponse)

    await store.fetchArchive(0, 10)

    expect(tournamentService.getTournamentsPaginated).toHaveBeenCalledWith('COMPLETED', 0, 10)
    expect(store.archiveTournaments).toHaveLength(1)
    expect(store.archiveTournaments[0].id).toBe('t-arch-1')
    expect(store.archiveTotalPages).toBe(1)
  })
})
