import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { usePoolStore } from '@/features/matchmaking/stores/poolStore';
import * as poolService from '@/features/matchmaking/services/poolService';
import type { PoolResponse } from '@/features/matchmaking/types/pool';

vi.mock('@/features/matchmaking/services/poolService');

describe('usePoolStore ATDD Specs — Story 6.4: Active Pools & Join Logic', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  const mockOpenPool: PoolResponse = {
    id: 'pool-123',
    creatorId: 'host-1',
    creatorNickname: 'HostPlayer',
    matchType: 'ONE_VS_ONE',
    startCondition: 'FILL_BASED',
    scheduledTime: null,
    skillLevel: 'OPEN_FOR_ALL',
    status: 'OPEN',
    requiredPlayers: 2,
    currentPlayers: 1,
    participants: [
      {
        userId: 'host-1',
        nickname: 'HostPlayer',
        avatar: 'avatar-1',
        role: 'HOST',
        joinedAt: '2026-08-28T12:00:00Z',
      },
    ],
    createdAt: '2026-08-28T12:00:00Z',
  };

  const mockFilledPool: PoolResponse = {
    ...mockOpenPool,
    status: 'FILLED',
    currentPlayers: 2,
    participants: [
      ...mockOpenPool.participants,
      {
        userId: 'user-2',
        nickname: 'JoinerPlayer',
        avatar: 'avatar-2',
        role: 'PLAYER',
        joinedAt: '2026-08-28T12:05:00Z',
      },
    ],
  };

  it('should fetch active pools and populate activePools state (AC 1)', async () => {
    vi.mocked(poolService.fetchActivePools).mockResolvedValue([mockOpenPool]);

    const store = usePoolStore();
    await store.fetchActivePools();

    expect(store.activePools).toHaveLength(1);
    expect(store.activePools[0].id).toBe('pool-123');
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should join pool, update activePools entry, and set currentPool (AC 2, AC 3, AC 7)', async () => {
    vi.mocked(poolService.fetchActivePools).mockResolvedValue([mockOpenPool]);
    vi.mocked(poolService.joinPool).mockResolvedValue(mockFilledPool);

    const store = usePoolStore();
    await store.fetchActivePools();
    const result = await store.joinPool('pool-123');

    expect(result.status).toBe('FILLED');
    expect(result.currentPlayers).toBe(2);
    expect(store.currentPool?.status).toBe('FILLED');
    expect(store.activePools[0].status).toBe('FILLED');
    expect(store.activePools[0].participants).toHaveLength(2);
  });

  it('should handle join failure (409 Conflict) gracefully and set error message (AC 4, AC 5)', async () => {
    vi.mocked(poolService.joinPool).mockRejectedValue(new Error('User is already a participant in this pool'));

    const store = usePoolStore();

    await expect(store.joinPool('pool-123')).rejects.toThrow('User is already a participant in this pool');
    expect(store.error).toBe('User is already a participant in this pool');
    expect(store.isLoading).toBe(false);
  });
});
