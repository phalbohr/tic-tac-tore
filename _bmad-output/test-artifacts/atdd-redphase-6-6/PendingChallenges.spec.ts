import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PendingChallenges from '@/features/challenge/components/PendingChallenges.vue';
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

describe('PendingChallenges Component ATDD Specifications (Story 6.6)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders incoming challenge cards with challenger info, match type, and action buttons', () => {
    const challengeStore = useChallengeStore();
    challengeStore.incomingChallenges = [
      {
        id: 'chal-100',
        challengerId: 'challenger-1',
        challengerNickname: 'StrikeMaster',
        challengerAvatar: 'https://example.com/avatar.png',
        targetPlayerId: 'me',
        targetPlayerNickname: 'Me',
        targetPlayerAvatar: null,
        targetGroupId: null,
        targetGroupName: null,
        matchType: 'ONE_VS_ONE',
        ruleConfigId: null,
        ruleConfigName: 'Standard Rules',
        message: 'Let us see who is best!',
        status: 'PENDING',
        createdAt: '2026-08-30T12:00:00Z',
        expiresAt: '2026-08-31T12:00:00Z',
      },
    ];

    const wrapper = mount(PendingChallenges);

    expect(wrapper.find('[data-test="challenge-card-chal-100"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('StrikeMaster');
    expect(wrapper.text()).toContain('Let us see who is best!');
    expect(wrapper.find('[data-test="accept-challenge-chal-100"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="decline-challenge-chal-100"]').exists()).toBe(true);
  });

  it('triggers acceptChallenge store action when Accept button is clicked', async () => {
    const challengeStore = useChallengeStore();
    challengeStore.incomingChallenges = [
      {
        id: 'chal-100',
        challengerId: 'challenger-1',
        challengerNickname: 'StrikeMaster',
        challengerAvatar: null,
        targetPlayerId: 'me',
        targetPlayerNickname: 'Me',
        targetPlayerAvatar: null,
        targetGroupId: null,
        targetGroupName: null,
        matchType: 'ONE_VS_ONE',
        ruleConfigId: null,
        ruleConfigName: null,
        message: null,
        status: 'PENDING',
        createdAt: '2026-08-30T12:00:00Z',
        expiresAt: '2026-08-31T12:00:00Z',
      },
    ];
    const acceptSpy = vi.spyOn(challengeStore, 'acceptChallenge').mockResolvedValue({
      challengeId: 'chal-100',
      status: 'ACCEPTED',
      message: 'Challenge accepted',
    });

    const wrapper = mount(PendingChallenges);
    await wrapper.find('[data-test="accept-challenge-chal-100"]').trigger('click');

    expect(acceptSpy).toHaveBeenCalledWith('chal-100');
  });

  it('triggers declineChallenge store action when Decline button is clicked', async () => {
    const challengeStore = useChallengeStore();
    challengeStore.incomingChallenges = [
      {
        id: 'chal-100',
        challengerId: 'challenger-1',
        challengerNickname: 'StrikeMaster',
        challengerAvatar: null,
        targetPlayerId: 'me',
        targetPlayerNickname: 'Me',
        targetPlayerAvatar: null,
        targetGroupId: null,
        targetGroupName: null,
        matchType: 'ONE_VS_ONE',
        ruleConfigId: null,
        ruleConfigName: null,
        message: null,
        status: 'PENDING',
        createdAt: '2026-08-30T12:00:00Z',
        expiresAt: '2026-08-31T12:00:00Z',
      },
    ];
    const declineSpy = vi.spyOn(challengeStore, 'declineChallenge').mockResolvedValue({
      challengeId: 'chal-100',
      status: 'DECLINED',
      message: 'Challenge declined',
    });

    const wrapper = mount(PendingChallenges);
    await wrapper.find('[data-test="decline-challenge-chal-100"]').trigger('click');

    expect(declineSpy).toHaveBeenCalledWith('chal-100');
  });
});
