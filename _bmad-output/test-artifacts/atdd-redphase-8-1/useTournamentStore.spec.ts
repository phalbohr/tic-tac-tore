import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore';
import * as tournamentService from '@/features/tournament/services/tournamentService';
import type { TournamentDto, CreateTournamentPayload } from '@/features/tournament/types/tournament';

vi.mock('@/features/tournament/services/tournamentService');

describe('useTournamentStore ATDD Specs — Story 8.1', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should initialize with default empty state', () => {
    const store = useTournamentStore();

    expect(store.tournaments).toEqual([]);
    expect(store.currentTournament).toBeNull();
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should create tournament successfully and prepend to tournaments list (AC 2, AC 6)', async () => {
    const store = useTournamentStore();
    const payload: CreateTournamentPayload = {
      name: 'Autumn Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfigurationId: 'rule-uuid-1',
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      hasPlayoff: false,
    };
    const mockTournament: TournamentDto = {
      id: 'tourn-uuid-1',
      name: 'Autumn Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rule-uuid-1',
        name: 'ITSF Standard Matchplay',
        goalLimit: 5,
        gameLimit: 3,
        winByTwo: true,
      },
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      roundCount: null,
      hasPlayoff: false,
      status: 'REGISTRATION_OPEN',
      creatorId: 'user-uuid-1',
      creatorNickname: 'OrganizerUser',
      createdAt: '2026-09-01T10:00:00Z',
    };

    vi.mocked(tournamentService.createTournament).mockResolvedValue(mockTournament);

    const result = await store.createTournament(payload);

    expect(tournamentService.createTournament).toHaveBeenCalledWith(payload);
    expect(result).toEqual(mockTournament);
    expect(store.tournaments).toContainEqual(mockTournament);
    expect(store.currentTournament).toEqual(mockTournament);
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should handle creation error gracefully and store error message (AC 4)', async () => {
    const store = useTournamentStore();
    const payload: CreateTournamentPayload = {
      name: 'AB',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfigurationId: 'rule-uuid-1',
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
    };
    const errorMsg = 'Tournament name must be between 3 and 100 characters';

    vi.mocked(tournamentService.createTournament).mockRejectedValue(new Error(errorMsg));

    await expect(store.createTournament(payload)).rejects.toThrow(errorMsg);

    expect(store.isLoading).toBe(false);
    expect(store.error).toBe(errorMsg);
  });

  it('should fetch tournaments list with optional status filter (AC 5)', async () => {
    const store = useTournamentStore();
    const mockList: TournamentDto[] = [
      {
        id: 'tourn-uuid-1',
        name: 'Autumn Cup 2026',
        format: 'CUP',
        mode: 'ONE_VS_ONE_PERSONAL',
        ruleConfiguration: {
          id: 'rule-uuid-1',
          name: 'ITSF Standard Matchplay',
          goalLimit: 5,
          gameLimit: 3,
          winByTwo: true,
        },
        minParticipants: 4,
        maxParticipants: 16,
        registrationDeadline: '2026-09-10T12:00:00Z',
        roundCount: null,
        hasPlayoff: false,
        status: 'REGISTRATION_OPEN',
        creatorId: 'user-uuid-1',
        creatorNickname: 'OrganizerUser',
        createdAt: '2026-09-01T10:00:00Z',
      },
    ];

    vi.mocked(tournamentService.getTournaments).mockResolvedValue(mockList);

    await store.fetchTournaments('REGISTRATION_OPEN');

    expect(tournamentService.getTournaments).toHaveBeenCalledWith('REGISTRATION_OPEN');
    expect(store.tournaments).toEqual(mockList);
    expect(store.isLoading).toBe(false);
  });

  it('should handle fetchTournaments error gracefully and set error state (AC 5)', async () => {
    const store = useTournamentStore();
    const errorMsg = 'Failed to fetch tournaments';

    vi.mocked(tournamentService.getTournaments).mockRejectedValue(new Error(errorMsg));

    await expect(store.fetchTournaments()).rejects.toThrow(errorMsg);

    expect(store.isLoading).toBe(false);
    expect(store.error).toBe(errorMsg);
  });

  it('should fetch tournament by id and set currentTournament (AC 5)', async () => {
    const store = useTournamentStore();
    const mockTournament: TournamentDto = {
      id: 'tourn-uuid-1',
      name: 'Autumn Cup 2026',
      format: 'CUP',
      mode: 'ONE_VS_ONE_PERSONAL',
      ruleConfiguration: {
        id: 'rule-uuid-1',
        name: 'ITSF Standard Matchplay',
        goalLimit: 5,
        gameLimit: 3,
        winByTwo: true,
      },
      minParticipants: 4,
      maxParticipants: 16,
      registrationDeadline: '2026-09-10T12:00:00Z',
      roundCount: null,
      hasPlayoff: false,
      status: 'REGISTRATION_OPEN',
      creatorId: 'user-uuid-1',
      creatorNickname: 'OrganizerUser',
      createdAt: '2026-09-01T10:00:00Z',
    };

    vi.mocked(tournamentService.getTournamentById).mockResolvedValue(mockTournament);

    const result = await store.fetchTournamentById('tourn-uuid-1');

    expect(tournamentService.getTournamentById).toHaveBeenCalledWith('tourn-uuid-1');
    expect(result).toEqual(mockTournament);
    expect(store.currentTournament).toEqual(mockTournament);
  });

  it('should handle fetchTournamentById error gracefully and set error state (AC 5)', async () => {
    const store = useTournamentStore();
    const errorMsg = 'Tournament not found';

    vi.mocked(tournamentService.getTournamentById).mockRejectedValue(new Error(errorMsg));

    await expect(store.fetchTournamentById('invalid-id')).rejects.toThrow(errorMsg);

    expect(store.isLoading).toBe(false);
    expect(store.error).toBe(errorMsg);
  });
});
