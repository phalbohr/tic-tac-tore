import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore';
import * as bracketService from '@/features/tournament/services/tournamentBracketService';

vi.mock('@/features/tournament/services/tournamentBracketService');

describe('tournamentStore - Bracket & Matches (ATDD Red Phase - Story 8.3)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('fetchBracket should store bracket data by tournamentId', async () => {
    const tournamentId = 'tourn-123';
    const mockBracket: any = {
      tournamentId,
      tournamentName: 'Summer Cup',
      format: 'CUP',
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
    const mockMatches: any[] = [
      { id: 'm-1', round: 1, matchOrder: 1, status: 'READY' },
    ];

    vi.spyOn(bracketService, 'getTournamentMatches').mockResolvedValue(mockMatches);

    const store = useTournamentStore();
    await store.fetchMatches(tournamentId, 1);

    expect(store.matches[tournamentId]).toEqual(mockMatches);
    expect(bracketService.getTournamentMatches).toHaveBeenCalledWith(tournamentId, 1);
  });

  it('startTournament should trigger lifecycle start and update tournament status in store', async () => {
    const tournamentId = 'tourn-123';
    const mockUpdatedTournament: any = {
      id: tournamentId,
      name: 'Summer Cup',
      status: 'IN_PROGRESS',
    };

    vi.spyOn(bracketService, 'startTournament').mockResolvedValue(mockUpdatedTournament);

    const store = useTournamentStore();
    store.tournaments = [{ id: tournamentId, name: 'Summer Cup', status: 'REGISTRATION_OPEN' } as any];

    await store.startTournament(tournamentId);

    const tournament = store.tournaments.find((t) => t.id === tournamentId);
    expect(tournament?.status).toBe('IN_PROGRESS');
  });
});
