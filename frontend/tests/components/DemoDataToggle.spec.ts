import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import DemoDataToggle from '@/features/profile/components/DemoDataToggle.vue';
import { createTestingPinia } from '@pinia/testing';
import { useStatsStore } from '@/features/stats/stores/useStatsStore';

describe('[Story 4.1] DemoDataToggle.vue (ATDD Red Phase)', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('[P0] should render toggle as checked when demo mode is active', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.confirmedMatchesCount = 0;
    await wrapper.vm.$nextTick();
    
    expect(wrapper.find('button[role="switch"]').attributes('aria-checked')).toBe('true');
  });

  it('[P0] should render toggle as unchecked when demo mode is not active', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.confirmedMatchesCount = 5;
    await wrapper.vm.$nextTick();
    
    expect(wrapper.find('button[role="switch"]').attributes('aria-checked')).toBe('false');
  });

  it('[P1] should call store toggleDemoMode action when clicked', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.confirmedMatchesCount = 0;
    await wrapper.vm.$nextTick();
    
    await wrapper.find('button[role="switch"]').trigger('click');
    
    expect(store.toggleDemoMode).toHaveBeenCalledWith(false);
  });

  it('[P1] should call store toggleDemoMode action with true when clicked in unchecked state', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn })] }
    });
    const store = useStatsStore();
    store.confirmedMatchesCount = 5;
    await wrapper.vm.$nextTick();
    
    await wrapper.find('button[role="switch"]').trigger('click');
    
    expect(store.toggleDemoMode).toHaveBeenCalledWith(true);
  });
});
