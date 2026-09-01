import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentInviteModal from '@/features/tournament/components/TournamentInviteModal.vue';
import type { TournamentRegistrationDto } from '@/features/tournament/types/tournament';

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, params?: Record<string, unknown>) => {
        if (params?.inviter) return `${params.inviter} invited you to join team for ${params.tournament}`;
        return key;
      },
    }),
  };
});

describe('TournamentInviteModal.vue Component Tests — Story 8.2', () => {
  const mockInvite: TournamentRegistrationDto = {
    id: 'reg-invite-1',
    tournamentId: 'tourn-1',
    tournamentName: 'Winter Duo Clash',
    playerId: 'user-10',
    playerNickname: 'CaptainInviter',
    playerAvatarUrl: 'https://example.com/captain.png',
    partnerId: 'user-me',
    partnerNickname: 'Me',
    partnerAvatarUrl: null,
    status: 'PENDING_CONFIRMATION',
    createdAt: '2026-09-01T11:00:00Z',
  };

  it('should render invite details including inviter name and tournament name (AC 8)', () => {
    const wrapper = mount(TournamentInviteModal, {
      props: {
        isOpen: true,
        invite: mockInvite,
      },
    });

    expect(wrapper.find('[data-testid="tournament-invite-modal"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('CaptainInviter');
    expect(wrapper.text()).toContain('Winter Duo Clash');
    expect(wrapper.find('[data-testid="accept-invite-btn"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="decline-invite-btn"]').exists()).toBe(true);
  });

  it('should emit accept event when Accept button is clicked (AC 3, AC 8)', async () => {
    const wrapper = mount(TournamentInviteModal, {
      props: {
        isOpen: true,
        invite: mockInvite,
      },
    });

    await wrapper.find('[data-testid="accept-invite-btn"]').trigger('click');

    expect(wrapper.emitted('accept')).toBeTruthy();
    expect(wrapper.emitted('accept')?.[0]?.[0]).toEqual(mockInvite);
  });

  it('should emit decline event when Decline button is clicked (AC 4, AC 8)', async () => {
    const wrapper = mount(TournamentInviteModal, {
      props: {
        isOpen: true,
        invite: mockInvite,
      },
    });

    await wrapper.find('[data-testid="decline-invite-btn"]').trigger('click');

    expect(wrapper.emitted('decline')).toBeTruthy();
    expect(wrapper.emitted('decline')?.[0]?.[0]).toEqual(mockInvite);
  });
});
