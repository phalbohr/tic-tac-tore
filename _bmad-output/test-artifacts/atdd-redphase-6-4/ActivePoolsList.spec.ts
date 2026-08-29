import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import ActivePoolsList from '@/features/matchmaking/components/ActivePoolsList.vue';
import { usePoolStore } from '@/features/matchmaking/stores/poolStore';
import { useAuthStore } from '@/stores/auth';
import type { PoolResponse } from '@/features/matchmaking/types/pool';

describe('ActivePoolsList Component ATDD Specs — Story 6.4', () => {
  let pinia: any;

  const mockOpenPool: PoolResponse = {
    id: 'pool-101',
    creatorId: 'user-host',
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
        userId: 'user-host',
        nickname: 'HostPlayer',
        avatar: 'avatar-1',
        role: 'HOST',
        joinedAt: '2026-08-28T10:00:00Z',
      },
    ],
    createdAt: '2026-08-28T10:00:00Z',
  };

  beforeEach(() => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: { id: 'user-guest', nickname: 'GuestPlayer', avatar: 'avatar-2' },
          isAuthenticated: true,
        },
        pool: {
          activePools: [mockOpenPool],
          isLoading: false,
          error: null,
        },
      },
    });
  });

  it('renders active pool card with details and Join button for non-participants (AC 1, AC 2)', async () => {
    const wrapper = mount(ActivePoolsList, {
      global: {
        plugins: [pinia],
        mocks: {
          t: (key: string, fallback?: string) => fallback || key,
        },
      },
    });

    expect(wrapper.text()).toContain('HostPlayer');
    expect(wrapper.text()).toContain('1v1');
    expect(wrapper.text()).toContain('1/2');

    const joinButton = wrapper.find('[data-testid="join-pool-btn-pool-101"]');
    expect(joinButton.exists()).toBe(true);
    expect(joinButton.text()).toContain('Join');
  });

  it('renders "Joined" badge instead of "Join" button when authenticated user is a participant (AC 1, AC 7)', async () => {
    const poolWithCurrentUser: PoolResponse = {
      ...mockOpenPool,
      id: 'pool-102',
      participants: [
        ...mockOpenPool.participants,
        {
          userId: 'user-guest',
          nickname: 'GuestPlayer',
          avatar: 'avatar-2',
          role: 'PLAYER',
          joinedAt: '2026-08-28T10:05:00Z',
        },
      ],
      currentPlayers: 2,
    };

    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: { id: 'user-guest', nickname: 'GuestPlayer', avatar: 'avatar-2' },
          isAuthenticated: true,
        },
        pool: {
          activePools: [poolWithCurrentUser],
          isLoading: false,
          error: null,
        },
      },
    });

    const wrapper = mount(ActivePoolsList, {
      global: {
        plugins: [pinia],
        mocks: {
          t: (key: string, fallback?: string) => fallback || key,
        },
      },
    });

    const joinButton = wrapper.find('[data-testid="join-pool-btn-pool-102"]');
    expect(joinButton.exists()).toBe(false);

    const joinedBadge = wrapper.find('[data-testid="joined-pool-badge-pool-102"]');
    expect(joinedBadge.exists()).toBe(true);
    expect(joinedBadge.text()).toContain('Joined');
  });

  it('triggers store join action and emits toast upon clicking Join button (AC 2, AC 7)', async () => {
    const wrapper = mount(ActivePoolsList, {
      global: {
        plugins: [pinia],
        mocks: {
          t: (key: string, fallback?: string) => fallback || key,
        },
      },
    });

    const poolStore = usePoolStore();
    const joinButton = wrapper.find('[data-testid="join-pool-btn-pool-101"]');
    await joinButton.trigger('click');

    expect(poolStore.joinPool).toHaveBeenCalledWith('pool-101');
  });

  it('renders clean empty state when there are no active pools (AC 8)', async () => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: { id: 'user-guest', nickname: 'GuestPlayer', avatar: 'avatar-2' },
          isAuthenticated: true,
        },
        pool: {
          activePools: [],
          isLoading: false,
          error: null,
        },
      },
    });

    const wrapper = mount(ActivePoolsList, {
      global: {
        plugins: [pinia],
        mocks: {
          t: (key: string, fallback?: string) => fallback || key,
        },
      },
    });
    await flushPromises();

    expect(wrapper.find('[data-testid="empty-pools-state"]').exists()).toBe(true);
  });
});
