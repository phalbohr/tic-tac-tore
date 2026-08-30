import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ChallengeModal from '@/features/challenge/components/ChallengeModal.vue';
import { useChallengeStore } from '@/features/challenge/stores/useChallengeStore';

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  };
});

describe('ChallengeModal Component ATDD Specifications (Story 6.6)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders modal with target player details, match type selector, and custom message input', () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        isOpen: true,
        targetPlayer: {
          id: 'player-123',
          nickname: 'SpeedyFoos',
          avatarUrl: 'https://example.com/speedy.png',
        },
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    expect(wrapper.find('[data-test="challenge-modal"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('SpeedyFoos');
    expect(wrapper.find('[data-test="match-type-1v1"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="match-type-2v2"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="challenge-message-input"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="submit-challenge-btn"]').exists()).toBe(true);
  });

  it('allows toggling between 1v1 and 2v2 match types', async () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        isOpen: true,
        targetPlayer: {
          id: 'player-123',
          nickname: 'SpeedyFoos',
          avatarUrl: null,
        },
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    const btn2v2 = wrapper.find('[data-test="match-type-2v2"]');
    await btn2v2.trigger('click');

    expect(btn2v2.classes()).toContain('active');
  });

  it('dispatches createChallenge action to store and emits close on submission', async () => {
    const challengeStore = useChallengeStore();
    const createSpy = vi.spyOn(challengeStore, 'createChallenge').mockResolvedValue({
      id: 'chal-1',
      challengerId: 'me',
      challengerNickname: 'Me',
      challengerAvatar: null,
      targetPlayerId: 'player-123',
      targetPlayerNickname: 'SpeedyFoos',
      targetPlayerAvatar: null,
      targetGroupId: null,
      targetGroupName: null,
      matchType: 'ONE_VS_ONE',
      ruleConfigId: null,
      ruleConfigName: null,
      message: 'Rematch!',
      status: 'PENDING',
      createdAt: '2026-08-30T10:00:00Z',
      expiresAt: '2026-08-31T10:00:00Z',
    });

    const wrapper = mount(ChallengeModal, {
      props: {
        isOpen: true,
        targetPlayer: {
          id: 'player-123',
          nickname: 'SpeedyFoos',
          avatarUrl: null,
        },
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    await wrapper.find('[data-test="challenge-message-input"]').setValue('Rematch!');
    await wrapper.find('[data-test="submit-challenge-btn"]').trigger('click');

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        targetPlayerId: 'player-123',
        message: 'Rematch!',
        matchType: 'ONE_VS_ONE',
      })
    );
    expect(wrapper.emitted('close')).toBeTruthy();
  });
});
