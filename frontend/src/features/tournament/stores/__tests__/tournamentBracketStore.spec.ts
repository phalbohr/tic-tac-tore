import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore';
import * as bracketService from '@/features/tournament/services/tournamentBracketService';
import type { TournamentBracketDto, TournamentDto, TournamentMatchDto } from '@/features/tournament/types/tournament';

vi.mock('@/features/tournament/services/tournamentBracketService');

describe('tournamentStore - Bracket & Matches (Story 8.3)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('fetchBracket should store bracket data by tournamentId', async () => {
    const tournamentId = 'tourn-123';
    const mockBracket: TournamentBracketDto = {
      tournamentId,
      tournamentName: 'Summer Cup',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      status: 'IN_PROGRESS',
      totalRounds: 3,
      rounds: [],
      seededParticipants: [],
    };

    vi.spyOn(bracketService, 'getTournamentBracket').mockResolvedValue(mockBracket);

    const store = useTournamentStore();
    await store.fetchBracket(tournamentId);

    expect(store.brackets[tournamentId]).toEqual(mockBracket);
    expect(bracketService.getTournamentBracket).toHaveBeenCalledWith(tournamentId);
  });

  it('fetchMatches should store matches by tournamentId and optional round', async () => {
    const tournamentId = 'tourn-123';
    const mockMatches: TournamentMatchDto[] = [
      { id: 'm-1', tournamentId, round: 1, matchOrder: 1, status: 'READY' },
    ];

    vi.spyOn(bracketService, 'getTournamentMatches').mockResolvedValue(mockMatches);

    const store = useTournamentStore();
    await store.fetchMatches(tournamentId, 1);

    expect(store.matches[tournamentId]).toEqual(mockMatches);
    expect(bracketService.getTournamentMatches).toHaveBeenCalledWith(tournamentId, 1);
  });

  it('startTournament should trigger lifecycle start and update tournament status in store', async () => {
    const tournamentId = 'tourn-123';
    const mockUpdatedTournament: TournamentDto = {
      id: tournamentId,
      name: 'Summer Cup',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rc-1',
        name: 'Standard',
        goalLimit: 5,
        gameLimit: 1,
        winByTwo: false,
      },
      minParticipants: 4,
      maxParticipants: 8,
      registrationDeadline: '',
      hasPlayoff: false,
      status: 'IN_PROGRESS',
      creatorId: 'c-1',
      creatorNickname: 'Creator',
      createdAt: '',
    };

    vi.spyOn(bracketService, 'startTournament').mockResolvedValue(mockUpdatedTournament);

    const store = useTournamentStore();
    store.tournaments = [{ ...mockUpdatedTournament, status: 'REGISTRATION_OPEN' }];

    await store.startTournament(tournamentId);

    const tournament = store.tournaments.find((t) => t.id === tournamentId);
    expect(tournament?.status).toBe('IN_PROGRESS');
  });
});
