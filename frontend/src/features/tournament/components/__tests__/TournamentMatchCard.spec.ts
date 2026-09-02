import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentMatchCard from '@/features/tournament/components/TournamentMatchCard.vue';
import type { TournamentMatchDto } from '@/features/tournament/types/tournament';

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      if (key === 'tournament.stub_partner') return 'Stub';
      return key;
    },
    te: (key: string) => key === 'tournament.stub_partner',
  }),
}));

describe('TournamentMatchCard.vue (Story 8.3)', () => {
  it('renders standard match pairing with seeds and player nicknames', () => {
    const match: TournamentMatchDto = {
      id: 'm-1',
      tournamentId: 'tourn-1',
      round: 1,
      matchOrder: 1,
      participant1: {
        id: 'reg-1',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-1',
        playerNickname: 'Alice',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 1,
      },
      participant2: {
        id: 'reg-8',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-8',
        playerNickname: 'Bob',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 8,
      },
      seed1: 1,
      seed2: 8,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    expect(wrapper.text()).toContain('Alice');
    expect(wrapper.text()).toContain('Bob');
    expect(wrapper.text()).toContain('#1');
    expect(wrapper.text()).toContain('#8');
  });

  it('renders BYE badge and auto-advancement for unseeded slot', () => {
    const match: TournamentMatchDto = {
      id: 'm-2',
      tournamentId: 'tourn-1',
      round: 1,
      matchOrder: 2,
      participant1: {
        id: 'reg-2',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-2',
        playerNickname: 'Eve',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 2,
      },
      participant2: null,
      seed1: 2,
      seed2: null,
      status: 'BYE',
      winnerRegistrationId: 'reg-2',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    expect(wrapper.text()).toContain('Eve');
    expect(wrapper.text()).toContain('BYE');
  });

  it('renders 2v2 pairing with partners on both sides (Story 8.4)', () => {
    const match: TournamentMatchDto = {
      id: 'm-84-1',
      tournamentId: 'tourn-1',
      round: 1,
      matchOrder: 1,
      participant1: {
        id: 'reg-1',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-1',
        playerNickname: 'Alice',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 1,
      },
      participant1Partner: {
        id: 'reg-2',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-2',
        playerNickname: 'Bob',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 2,
      },
      participant2: {
        id: 'reg-3',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-3',
        playerNickname: 'Charlie',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 3,
      },
      participant2Partner: {
        id: 'reg-4',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-4',
        playerNickname: 'Diana',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 4,
      },
      isParticipant1Stub: false,
      isParticipant2Stub: false,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    expect(wrapper.text()).toContain('Alice & Bob');
    expect(wrapper.text()).toContain('Charlie & Diana');
  });

  it('renders stub badge when isParticipant1Stub or isParticipant2Stub is true (Story 8.4)', () => {
    const match: TournamentMatchDto = {
      id: 'm-84-2',
      tournamentId: 'tourn-1',
      round: 1,
      matchOrder: 1,
      participant1: {
        id: 'reg-1',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-1',
        playerNickname: 'Alice',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 1,
      },
      participant1Partner: {
        id: 'reg-2',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-2',
        playerNickname: 'Bob',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 2,
      },
      participant2: {
        id: 'reg-3',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-3',
        playerNickname: 'Charlie',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 3,
      },
      participant2Partner: {
        id: 'reg-4',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-4',
        playerNickname: 'Diana',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 4,
      },
      isParticipant1Stub: true,
      isParticipant2Stub: false,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    const stubBadges = wrapper.findAll('[data-testid="stub-partner-badge"]');
    expect(stubBadges.length).toBeGreaterThan(0);
    expect(stubBadges[0]!.text()).toContain('Stub');
  });

  it('renders translated stub badge text when isParticipant2Stub is true', () => {
    const match: TournamentMatchDto = {
      id: 'm-84-3',
      tournamentId: 'tourn-1',
      round: 1,
      matchOrder: 1,
      participant1: {
        id: 'reg-1',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-1',
        playerNickname: 'Alice',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 1,
      },
      participant2: {
        id: 'reg-2',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-2',
        playerNickname: 'Bob',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 2,
      },
      participant2Partner: {
        id: 'reg-3',
        tournamentId: 'tourn-1',
        tournamentName: 'Test Cup',
        playerId: 'p-3',
        playerNickname: 'Charlie',
        status: 'CONFIRMED',
        createdAt: '',
        seed: 3,
      },
      isParticipant1Stub: false,
      isParticipant2Stub: true,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    const stubBadges = wrapper.findAll('[data-testid="stub-partner-badge"]');
    expect(stubBadges.length).toBe(1);
    expect(stubBadges[0]!.text()).toBe('Stub');
  });
});
