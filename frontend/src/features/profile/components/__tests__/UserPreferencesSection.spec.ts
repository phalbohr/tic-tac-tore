import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import UserPreferencesSection from '@/features/profile/components/UserPreferencesSection.vue';
import { useAuthStore } from '@/stores/auth';

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => {
        const translations: Record<string, string> = {
          'cabinet.defaultPreferences': 'Default Match Preferences',
          'cabinet.defaultPlayerGroup': 'Default Player Group',
          'cabinet.defaultRuleTemplate': 'Default Rule Template',
          'common.none': 'None',
          'rules.presets': 'Presets',
          'rules.custom': 'Custom Templates',
        };
        return translations[key] || defaultVal || key;
      },
    }),
  };
});

describe('UserPreferencesSection.vue (Story 6.2)', () => {
  let pinia: ReturnType<typeof createTestingPinia>;

  beforeEach(() => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: {
            id: 'user-1',
            nickname: 'PlayerOne',
            defaultGroupId: 'group-1',
            defaultRuleConfigurationId: 'rule-preset-1',
          },
        },
        playerGroup: {
          groups: [
            { id: 'group-1', name: 'Office Rivals', isFavorite: false, members: [] },
            { id: 'group-2', name: 'Weekend Warriors', isFavorite: true, members: [] },
          ],
          loading: false,
        },
        ruleConfig: {
          presets: [
            { id: 'rule-preset-1', name: 'ITSF Standard Matchplay', type: 'PRESET' },
          ],
          customRules: [
            { id: 'rule-custom-1', name: 'Fast 7', type: 'CUSTOM' },
          ],
          loading: false,
        },
      },
    });
  });

  it('[P0] should render selectors for default player group and rule template with options and "None"', async () => {
    const wrapper = mount(UserPreferencesSection, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true,
        },
      },
    });

    // Verify section header
    expect(wrapper.text()).toContain('Default Match Preferences');

    // Verify group select options
    const groupSelect = wrapper.find('select[data-test="default-group-select"]');
    expect(groupSelect.exists()).toBe(true);
    const groupOptions = groupSelect.findAll('option');
    expect(groupOptions.some((opt) => opt.text().includes('None') || opt.text().includes('Keine'))).toBe(true);
    expect(groupOptions.some((opt) => opt.text().includes('Office Rivals'))).toBe(true);
    expect(groupOptions.some((opt) => opt.text().includes('Weekend Warriors'))).toBe(true);

    // Verify rule template select options
    const ruleSelect = wrapper.find('select[data-test="default-rule-select"]');
    expect(ruleSelect.exists()).toBe(true);
    const ruleOptions = ruleSelect.findAll('option');
    expect(ruleOptions.some((opt) => opt.text().includes('None') || opt.text().includes('Kein'))).toBe(true);
    expect(ruleOptions.some((opt) => opt.text().includes('ITSF Standard Matchplay'))).toBe(true);
    expect(ruleOptions.some((opt) => opt.text().includes('Fast 7'))).toBe(true);
  });

  it('[P0] should dispatch updateProfile when default group or rule template is changed', async () => {
    const wrapper = mount(UserPreferencesSection, {
      global: {
        plugins: [pinia],
      },
    });

    const authStore = useAuthStore();
    const groupSelect = wrapper.find('select[data-test="default-group-select"]');
    await groupSelect.setValue('group-2');

    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultGroupId: 'group-2',
      })
    );
  });

  it('[P0] should allow selecting "None" to clear defaultGroupId or defaultRuleConfigurationId', async () => {
    const wrapper = mount(UserPreferencesSection, {
      global: {
        plugins: [pinia],
      },
    });

    const authStore = useAuthStore();
    const groupSelect = wrapper.find('select[data-test="default-group-select"]');
    await groupSelect.setValue('');

    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultGroupId: null,
      })
    );
  });
});
