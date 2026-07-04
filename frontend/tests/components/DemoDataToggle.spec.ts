import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import DemoDataToggle from '@/components/DemoDataToggle.vue';
import { createTestingPinia } from '@pinia/testing';
import { useStatsStore } from '@/stores/useStatsStore';

describe.skip('[Story 4.1] DemoDataToggle.vue (ATDD Red Phase)', () => {
  it('[P0] should render toggle when demo data is active', () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          initialState: {
            stats: { demoDataActive: true, demoDataHidden: false }
          }
        })]
      }
    });
    
    expect(wrapper.find('[data-testid="demo-mode-toggle"]').exists()).toBe(true);
  });

  it('[P0] should not render toggle when demo data is not active', () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          initialState: {
            stats: { demoDataActive: false }
          }
        })]
      }
    });
    
    expect(wrapper.find('[data-testid="demo-mode-toggle"]').exists()).toBe(false);
  });

  it('[P1] should call store toggleDemoData action when clicked', async () => {
    const wrapper = mount(DemoDataToggle, {
      global: {
        plugins: [createTestingPinia({
          initialState: {
            stats: { demoDataActive: true, demoDataHidden: false }
          }
        })]
      }
    });
    
    const store = useStatsStore();
    await wrapper.find('[data-testid="demo-mode-toggle"]').trigger('click');
    
    expect(store.toggleDemoData).toHaveBeenCalledOnce();
  });
});
