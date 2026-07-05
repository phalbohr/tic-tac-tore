import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import DemoDataToggle from '@/features/profile/components/DemoDataToggle.vue';
import { createTestingPinia } from '@pinia/testing';
import { useStatsStore } from '@/features/stats/stores/useStatsStore';

describe('[Story 4.1] DemoDataToggle.vue (ATDD Red Phase)', () => {
  it('[P0] should render toggle as checked when demo mode is active', () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            stats: { isDemoModeEnabled: true }
          }
        })]
      }
    });
    
    expect(wrapper.find('button[role="switch"]').attributes('aria-checked')).toBe('true');
  });

  it('[P0] should render toggle as unchecked when demo mode is not active', () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            stats: { isDemoModeEnabled: false, confirmedMatchesCount: 5 }
          }
        })]
      }
    });
    
    expect(wrapper.find('button[role="switch"]').attributes('aria-checked')).toBe('false');
  });

  it('[P1] should call store toggleDemoMode action when clicked', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            stats: { isDemoModeEnabled: true }
          }
        })]
      }
    });
    
    const store = useStatsStore();
    await wrapper.find('button[role="switch"]').trigger('click');
    
    expect(store.toggleDemoMode).toHaveBeenCalledWith(false);
  });
  it('[P1] should call store toggleDemoMode action with true when clicked in unchecked state', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn,
          initialState: {
            stats: { isDemoModeEnabled: false, confirmedMatchesCount: 5 }
          }
        })]
      }
    });
    
    const store = useStatsStore();
    await wrapper.find('button[role="switch"]').trigger('click');
    
    expect(store.toggleDemoMode).toHaveBeenCalledWith(true);
  });
});
