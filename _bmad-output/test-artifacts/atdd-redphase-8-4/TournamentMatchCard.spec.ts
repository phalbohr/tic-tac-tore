import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentMatchCard from '@/features/tournament/components/TournamentMatchCard.vue';

describe('TournamentMatchCard.vue (ATDD Red Phase - Story 8.4: 2v2 & Stub Badges)', () => {
  it('renders 2v2 pairing with partners on both sides', () => {
    const match: any = {
      id: 'm-84-1',
      round: 1,
      matchOrder: 1,
      participant1: { id: 'reg-1', playerNickname: 'Alice', seed: 1 },
      participant1Partner: { id: 'reg-2', playerNickname: 'Bob', seed: 2 },
      participant2: { id: 'reg-3', playerNickname: 'Charlie', seed: 3 },
      participant2Partner: { id: 'reg-4', playerNickname: 'Diana', seed: 4 },
      isParticipant1Stub: false,
      isParticipant2Stub: false,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    expect(wrapper.text()).toContain('Alice');
    expect(wrapper.text()).toContain('Bob');
    expect(wrapper.text()).toContain('Charlie');
    expect(wrapper.text()).toContain('Diana');
  });

  it('renders stub badge when isParticipant1Stub is true', () => {
    const match: any = {
      id: 'm-84-2',
      round: 1,
      matchOrder: 1,
      participant1: { id: 'reg-1', playerNickname: 'Alice', seed: 1 },
      participant1Partner: { id: 'reg-2', playerNickname: 'Bob', seed: 2 },
      participant2: { id: 'reg-3', playerNickname: 'Charlie', seed: 3 },
      participant2Partner: { id: 'reg-4', playerNickname: 'Diana', seed: 4 },
      isParticipant1Stub: true,
      isParticipant2Stub: false,
      status: 'READY',
    };

    const wrapper = mount(TournamentMatchCard, {
      props: { match },
    });

    const stubBadges = wrapper.findAll('[data-testid="stub-partner-badge"]');
    expect(stubBadges.length).toBeGreaterThan(0);
    expect(stubBadges[0].text()).toContain('Stub');
  });
});
