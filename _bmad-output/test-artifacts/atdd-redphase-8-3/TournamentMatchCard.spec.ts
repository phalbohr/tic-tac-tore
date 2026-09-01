import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentMatchCard from '@/features/tournament/components/TournamentMatchCard.vue';

describe('TournamentMatchCard.vue (ATDD Red Phase - Story 8.3)', () => {
  it('renders standard match pairing with seeds and player nicknames', () => {
    const match: any = {
      id: 'm-1',
      round: 1,
      matchOrder: 1,
      participant1: { id: 'reg-1', playerNickname: 'Alice', seed: 1 },
      participant2: { id: 'reg-8', playerNickname: 'Bob', seed: 8 },
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
    const match: any = {
      id: 'm-2',
      round: 1,
      matchOrder: 2,
      participant1: { id: 'reg-2', playerNickname: 'Eve', seed: 2 },
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
