import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentMatchCard from '@/features/tournament/components/TournamentMatchCard.vue';
import type { TournamentMatchDto } from '@/features/tournament/types/tournament';

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
});
