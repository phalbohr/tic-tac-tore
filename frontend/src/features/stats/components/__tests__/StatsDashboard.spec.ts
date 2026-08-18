import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import StatsDashboard from '@/features/stats/components/StatsDashboard.vue';
import { useStatsStore } from '@/features/stats/stores/useStatsStore';
import type { PlayerStats } from '@/services/statisticsService';

type PositionalStats = Pick<PlayerStats, 'overall' | 'attacker' | 'defender'>;

describe('[Story 4.3] StatsDashboard.vue (ATDD Red Phase)', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  const mountAndSetup = async (stats: PositionalStats) => {
    const wrapper = mount(StatsDashboard, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.stats = { playerId: 'player-1', playerName: 'Tester', ...stats };
    store.isLoading = false;
    await wrapper.vm.$nextTick();
    return wrapper;
  };

  it('[P0] should render Overall, Attacker, Defender stat cards with matches, W/L, win-rate bar, and percentage label', async () => {
    const wrapper = await mountAndSetup({
      overall: { matches: 10, wins: 6, losses: 4, winRate: 60.0 },
      attacker: { matches: 5, wins: 3, losses: 2, winRate: 60.0 },
      defender: { matches: 5, wins: 3, losses: 2, winRate: 60.0 }
    });

    expect(wrapper.text()).toContain('Overall');
    expect(wrapper.text()).toContain('Attacker');
    expect(wrapper.text()).toContain('Defender');
    expect(wrapper.text()).toContain('10');
    expect(wrapper.text()).toContain('5');
    expect(wrapper.text()).toContain('W: 6 L: 4');
    expect(wrapper.text()).toContain('60.0%');
  });

  it('[P0] should render zeroed stat cards with 0% bars and no NaN when no matches', async () => {
    const wrapper = await mountAndSetup({
      overall: { matches: 0, wins: 0, losses: 0, winRate: 0.0 },
      attacker: { matches: 0, wins: 0, losses: 0, winRate: 0.0 },
      defender: { matches: 0, wins: 0, losses: 0, winRate: 0.0 }
    });

    expect(wrapper.text()).toContain('0');
    expect(wrapper.text()).toContain('W: 0 L: 0');
    expect(wrapper.text()).toContain('0.0%');
    const bars = wrapper.findAll('.ch-stat-bar-fill');
    expect(bars.length).toBe(3);
    bars.forEach(bar => {
      expect(bar.attributes('style')).toContain('width: 0%');
    });
  });

  it('[P1] should cap bar width at 100% when winRate exceeds 100', async () => {
    const wrapper = await mountAndSetup({
      overall: { matches: 5, wins: 5, losses: 0, winRate: 120.0 },
      attacker: { matches: 3, wins: 3, losses: 0, winRate: 120.0 },
      defender: { matches: 2, wins: 2, losses: 0, winRate: 120.0 }
    });

    const bars = wrapper.findAll('.ch-stat-bar-fill');
    expect(bars.length).toBe(3);
    bars.forEach(bar => {
      expect(bar.attributes('style')).toContain('width: 100%');
    });
  });

  it('[P1] should render attacker bar with bg-secondary and overall/defender bars with bg-primary', async () => {
    const wrapper = await mountAndSetup({
      overall: { matches: 5, wins: 3, losses: 2, winRate: 60.0 },
      attacker: { matches: 3, wins: 2, losses: 1, winRate: 66.7 },
      defender: { matches: 2, wins: 1, losses: 1, winRate: 50.0 }
    });

    const bars = wrapper.findAll('.ch-stat-bar-fill');
    expect(bars.length).toBe(3);
    const [overallBar, attackerBar, defenderBar] = bars;
    expect(overallBar?.classes()).toContain('bg-primary');
    expect(attackerBar?.classes()).toContain('bg-secondary');
    expect(defenderBar?.classes()).toContain('bg-primary');
  });

  it('[P1] should render loading skeleton when statsStore.isLoading is true', async () => {
    const wrapper = mount(StatsDashboard, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.stats = null;
    store.isLoading = true;
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.animate-pulse').exists()).toBe(true);
  });

  it('[P1] should render error message when stats is null and not loading', async () => {
    const wrapper = mount(StatsDashboard, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.stats = null;
    store.isLoading = false;
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Unable to load statistics.');
  });
});
