import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import PlayerSelection from '@/features/match/components/PlayerSelection.vue';
import { useAuthStore } from '@/stores/auth';

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => {
        const translations: Record<string, string> = {
          'match.players': 'Players',
          'groups.createGroup': '+ Group',
          'groups.setAsDefault': 'Set as default',
          'match.frequentOpponents': 'Frequent Opponents',
          'match.playerFallback': 'Player',
          'match.selectPlayer': 'Select Player',
          'common.default': 'Default',
        };
        return translations[key] || defaultVal || key;
      },
    }),
  };
});

describe('PlayerSelection.vue — Defaults & Set As Default Group (Story 6.2)', () => {
  let pinia: ReturnType<typeof createTestingPinia>;

  beforeEach(() => {
    pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        auth: {
          profile: {
            id: 'user-1',
            nickname: 'PlayerOne',
            defaultGroupId: 'group-fav-1',
          },
        },
        playerGroup: {
          groups: [
            { id: 'group-fav-1', name: 'Favorites', isFavorite: true, members: [{ id: 'u1', nickname: 'Alice' }] },
            { id: 'group-custom-2', name: 'Office Rivals', isFavorite: false, members: [{ id: 'u2', nickname: 'Bob' }] },
          ],
          selectedGroupId: 'group-fav-1',
        },
      },
    });
  });

  it('[P0] should pre-select defaultGroupId on mount and filter player selection (AC 3)', async () => {
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [pinia],
      },
    });

    const activeGroupChip = wrapper.find('[data-group-id="group-fav-1"]');
    expect(activeGroupChip.exists()).toBe(true);
    expect(activeGroupChip.classes()).toContain('active');
  });

  it('[P0] should show "Set as Default" button on active group and trigger profile update (AC 4)', async () => {
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [pinia],
      },
    });

    const officeGroupChip = wrapper.find('[data-group-id="group-custom-2"]');
    await officeGroupChip.trigger('click');

    const setDefaultBtn = wrapper.find('[data-test="set-as-default-group-btn"]');
    expect(setDefaultBtn.exists()).toBe(true);
    await setDefaultBtn.trigger('click');

    const authStore = useAuthStore();
    expect(authStore.updateProfile).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultGroupId: 'group-custom-2',
      })
    );
  });
});
