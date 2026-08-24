import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import RulePicker from '@/features/match/components/RulePicker.vue';
import { useAuthStore } from '@/stores/auth';
import { useRuleConfigStore } from '@/stores/useRuleConfigStore';

describe.skip('RulePicker.vue — Defaults & Set As Default Action (ATDD RED PHASE — Story 6.2)', () => {
  let pinia: any;

  beforeEach(() => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: {
            id: 'user-1',
            nickname: 'PlayerOne',
            defaultRuleConfigurationId: 'rule-preset-1',
          },
        },
        ruleConfig: {
          presets: [
            { id: 'rule-preset-1', name: 'ITSF Standard Matchplay', type: 'PRESET' },
            { id: 'rule-preset-2', name: 'DTFB Classic', type: 'PRESET' },
          ],
          customRules: [
            { id: 'rule-custom-1', name: 'Office Fast 7', type: 'CUSTOM' },
          ],
          selectedRuleId: 'rule-preset-1',
        },
      },
    });
  });

  it('[P0] should display default badge or indicator on default rule template chip (AC 3)', async () => {
    const wrapper = mount(RulePicker, {
      global: {
        plugins: [pinia],
      },
    });

    const defaultChip = wrapper.find('[data-rule-id="rule-preset-1"]');
    expect(defaultChip.exists()).toBe(true);
    expect(defaultChip.find('[data-test="default-indicator"]').exists()).toBe(true);
  });

  it('[P0] should show "Set as Default" button on non-default rule and trigger profile update when clicked (AC 4)', async () => {
    const wrapper = mount(RulePicker, {
      global: {
        plugins: [pinia],
      },
    });

    const nonDefaultChip = wrapper.find('[data-rule-id="rule-custom-1"]');
    expect(nonDefaultChip.exists()).toBe(true);
    await nonDefaultChip.trigger('click');

    const setDefaultBtn = wrapper.find('[data-test="set-as-default-rule-btn"]');
    expect(setDefaultBtn.exists()).toBe(true);
    await setDefaultBtn.trigger('click');

    const authStore = useAuthStore();
    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultRuleConfigurationId: 'rule-custom-1',
      })
    );
  });
});
