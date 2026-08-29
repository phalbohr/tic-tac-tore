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
          'cabinet.poolNotifications': 'Matchmaking Pool Notifications',
          'cabinet.poolNotificationsDesc': 'Receive push notifications when new matchmaking pools are created',
          'common.none': 'None',
          'rules.presets': 'Presets',
          'rules.custom': 'Custom Templates',
        };
        return translations[key] || defaultVal || key;
      },
    }),
  };
});

describe('UserPreferencesSection.vue (Story 6.2 & 6.5)', () => {
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
            poolNotificationsEnabled: true,
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

  it('[P0] should render Matchmaking Pool Notifications toggle switch (Story 6.5 AC 4)', () => {
    const wrapper = mount(UserPreferencesSection, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true,
        },
      },
    });

    const toggle = wrapper.find('[data-test="pool-notifications-toggle"]');
    expect(toggle.exists()).toBe(true);
    expect(wrapper.text()).toContain('Matchmaking Pool Notifications');
  });

  it('[P0] should toggle pool notifications preference and dispatch updateProfile (Story 6.5 AC 4)', async () => {
    const wrapper = mount(UserPreferencesSection, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true,
        },
      },
    });

    const authStore = useAuthStore();
    const toggle = wrapper.find('[data-test="pool-notifications-toggle"]');
    
    await toggle.trigger('click');

    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        poolNotificationsEnabled: false,
      })
    );
  });

  it('[P1] should fetch profile on mount if profile is not loaded', () => {
    const freshPinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: null,
        },
      },
    });

    mount(UserPreferencesSection, {
      global: {
        plugins: [freshPinia],
        stubs: {
          'router-link': true,
        },
      },
    });

    const authStore = useAuthStore();
    expect(authStore.fetchProfile).toHaveBeenCalled();
  });
});
