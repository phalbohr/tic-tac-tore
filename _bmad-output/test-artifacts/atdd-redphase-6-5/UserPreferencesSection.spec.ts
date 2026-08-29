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

describe('UserPreferencesSection.vue — Story 6.5: Pool Notifications Toggle', () => {
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
          groups: [],
          loading: false,
        },
        ruleConfig: {
          presets: [],
          customRules: [],
          loading: false,
        },
      },
    });
  });

  it('[P0] should render Matchmaking Pool Notifications toggle switch (AC 4)', () => {
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

  it('[P0] should toggle pool notifications preference and dispatch updateProfile (AC 4)', async () => {
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
    
    // Toggle from true to false
    await toggle.trigger('click');

    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        poolNotificationsEnabled: false,
      })
    );
  });
});
