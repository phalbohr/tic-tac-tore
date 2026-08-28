import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { usePoolStore } from '@/features/matchmaking/stores/poolStore';
import * as poolService from '@/features/matchmaking/services/poolService';
import type { PoolResponse, CreatePoolPayload } from '@/features/matchmaking/types/pool';

vi.mock('@/features/matchmaking/services/poolService');

describe('usePoolStore ATDD Specs', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should initialize with default empty state', () => {
    const store = usePoolStore();

    expect(store.activePools).toEqual([]);
    expect(store.currentPool).toBeNull();
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should create pool successfully and add to activePools and currentPool', async () => {
    const store = usePoolStore();
    const payload: CreatePoolPayload = {
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      skillLevel: 'OPEN_FOR_ALL',
    };
    const mockResponse: PoolResponse = {
      id: 'pool-uuid-1',
      creatorId: 'user-uuid-1',
      creatorNickname: 'HostUser',
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      scheduledTime: null,
      skillLevel: 'OPEN_FOR_ALL',
      status: 'OPEN',
      requiredPlayers: 2,
      currentPlayers: 1,
      participants: [
        {
          userId: 'user-uuid-1',
          nickname: 'HostUser',
          avatar: 'avatar-1',
          role: 'HOST',
          joinedAt: '2026-08-28T19:00:00Z',
        },
      ],
      createdAt: '2026-08-28T19:00:00Z',
    };

    vi.mocked(poolService.createPool).mockResolvedValue(mockResponse);

    const result = await store.createPool(payload);

    expect(poolService.createPool).toHaveBeenCalledWith(payload);
    expect(result).toEqual(mockResponse);
    expect(store.currentPool).toEqual(mockResponse);
    expect(store.activePools).toContainEqual(mockResponse);
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should handle creation failure and set error state', async () => {
    const store = usePoolStore();
    const payload: CreatePoolPayload = {
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
    };
    const errorMsg = 'Maximum active pools limit reached (3)';

    vi.mocked(poolService.createPool).mockRejectedValue(new Error(errorMsg));

    await expect(store.createPool(payload)).rejects.toThrow(errorMsg);

    expect(store.isLoading).toBe(false);
    expect(store.error).toBe(errorMsg);
    expect(store.currentPool).toBeNull();
  });

  it('should fetch pool by id and set currentPool', async () => {
    const store = usePoolStore();
    const mockResponse: PoolResponse = {
      id: 'pool-uuid-2',
      creatorId: 'user-uuid-2',
      creatorNickname: 'Alice',
      matchType: 'TWO_VS_TWO',
      startCondition: 'SCHEDULED_TIME',
      scheduledTime: '2026-08-30T15:00:00Z',
      skillLevel: 'INTERMEDIATE',
      status: 'OPEN',
      requiredPlayers: 4,
      currentPlayers: 1,
      participants: [],
      createdAt: '2026-08-28T19:00:00Z',
    };

    vi.mocked(poolService.fetchPoolById).mockResolvedValue(mockResponse);

    const result = await store.fetchPool('pool-uuid-2');

    expect(poolService.fetchPoolById).toHaveBeenCalledWith('pool-uuid-2');
    expect(result).toEqual(mockResponse);
    expect(store.currentPool).toEqual(mockResponse);
  });
});
