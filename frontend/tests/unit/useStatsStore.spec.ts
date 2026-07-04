import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useStatsStore } from '@/stores/useStatsStore';

describe.skip('[Story 4.1] useStatsStore - Demo Data (ATDD Red Phase)', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('[P0] should enable demo data when user has < 1 confirmed match', () => {
    const store = useStatsStore();
    store.confirmedMatches = 0;
    store.checkDemoDataThreshold();
    expect(store.demoDataActive).toBe(true);
  });

  it('[P0] should disable and hide demo data when user reaches 5 confirmed real matches', () => {
    const store = useStatsStore();
    store.demoDataActive = true;
    store.demoDataHidden = false;
    store.confirmedMatches = 5;
    store.checkDemoDataThreshold();
    
    expect(store.demoDataActive).toBe(false);
    expect(store.demoDataHidden).toBe(true);
  });

  it('[P1] should allow toggling demo data visibility when active', () => {
    const store = useStatsStore();
    store.demoDataActive = true;
    store.demoDataHidden = false;
    
    store.toggleDemoData();
    expect(store.demoDataHidden).toBe(true);
    
    store.toggleDemoData();
    expect(store.demoDataHidden).toBe(false);
  });
});
