import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import TournamentMatchCard from '@/features/tournament/components/TournamentMatchCard.vue';
import type { TournamentMatchDto } from '@/features/tournament/types/tournament';

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const messages: Record<string, string> = {
        'tournament.stub_partner': 'Stub',
        'tournament.match.start': 'Start Match',
        'tournament.match.opponent_busy': 'Opponent Busy',
        'tournament.match.live': 'LIVE',
        'tournament.match.completed': 'COMPLETED',
      };
      return messages[key] || key;
    },
    te: () => true,
  }),
}));

describe('TournamentMatchCard.vue (Story 8.5: Asynchronous Tournament Match Execution)', () => {
  const baseMatch: TournamentMatchDto = {
    id: 'match-101',
    tournamentId: 'tourn-85',
    round: 1,
    matchOrder: 1,
    participant1: {
      id: 'reg-p1',
      tournamentId: 'tourn-85',
      tournamentName: 'Championship',
      playerId: 'user-p1',
      playerNickname: 'Alice',
      status: 'CONFIRMED',
      createdAt: '',
      seed: 1,
    },
    participant2: {
      id: 'reg-p2',
      tournamentId: 'tourn-85',
      tournamentName: 'Championship',
      playerId: 'user-p2',
      playerNickname: 'Bob',
      status: 'CONFIRMED',
      createdAt: '',
      seed: 2,
    },
    seed1: 1,
    seed2: 2,
    status: 'READY',
  };

  it('renders Start Match button when match is READY and current user is a participant (AC4)', () => {
    const wrapper = mount(TournamentMatchCard, {
      props: {
        match: baseMatch,
        currentUserId: 'user-p1',
        isPlayable: true,
      },
    });

    const startButton = wrapper.find('[data-test="start-match-button"]');
    expect(startButton.exists()).toBe(true);
    expect(startButton.text()).toContain('Start Match');
  });

  it('emits start-match event when clicking Start Match button (AC4)', async () => {
    const wrapper = mount(TournamentMatchCard, {
      props: {
        match: baseMatch,
        currentUserId: 'user-p1',
        isPlayable: true,
      },
    });

    const startButton = wrapper.find('[data-test="start-match-button"]');
    await startButton.trigger('click');

    expect(wrapper.emitted('start-match')).toBeTruthy();
    expect(wrapper.emitted('start-match')?.[0]).toEqual(['match-101']);
  });

  it('renders Opponent Busy chip and disables start when opponent is in another active match (AC4)', () => {
    const matchWithBusyOpponent = {
      ...baseMatch,
      isOpponentBusy: true,
      busyParticipantNicknames: ['Bob'],
    };

    const wrapper = mount(TournamentMatchCard, {
      props: {
        match: matchWithBusyOpponent,
        currentUserId: 'user-p1',
        isPlayable: false,
      },
    });

    const busyChip = wrapper.find('[data-test="opponent-busy-badge"]');
    expect(busyChip.exists()).toBe(true);
    expect(busyChip.text()).toContain('Opponent Busy');

    const startButton = wrapper.find('[data-test="start-match-button"]');
    expect(startButton.attributes('disabled')).toBeDefined();
  });

  it('renders LIVE badge when match status is IN_PROGRESS (AC4)', () => {
    const inProgressMatch = {
      ...baseMatch,
      status: 'IN_PROGRESS' as const,
    };

    const wrapper = mount(TournamentMatchCard, {
      props: {
        match: inProgressMatch,
        currentUserId: 'user-p1',
      },
    });

    const liveBadge = wrapper.find('[data-test="match-live-badge"]');
    expect(liveBadge.exists()).toBe(true);
    expect(liveBadge.text()).toContain('LIVE');
  });
});
