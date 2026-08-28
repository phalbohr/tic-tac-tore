import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import CreatePoolModal from '@/features/matchmaking/components/CreatePoolModal.vue';
import { usePoolStore } from '@/features/matchmaking/stores/poolStore';

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  };
});

describe('CreatePoolModal Component ATDD Specs', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders modal when isOpen is true with default match type 1v1 and fill-based condition', () => {
    const wrapper = mount(CreatePoolModal, {
      props: {
        isOpen: true,
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    expect(wrapper.find('[data-test="create-pool-modal"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="match-type-1v1"]').classes()).toContain('active');
    expect(wrapper.find('[data-test="condition-fill"]').classes()).toContain('active');
    expect(wrapper.find('[data-test="datetime-picker"]').exists()).toBe(false);
  });

  it('shows datetime picker when SCHEDULED_TIME is selected', async () => {
    const wrapper = mount(CreatePoolModal, {
      props: {
        isOpen: true,
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    await wrapper.find('[data-test="condition-scheduled"]').trigger('click');

    expect(wrapper.find('[data-test="datetime-picker"]').exists()).toBe(true);
  });

  it('calls store.createPool and emits close on successful submission', async () => {
    const poolStore = usePoolStore();
    vi.spyOn(poolStore, 'createPool').mockResolvedValue({
      id: 'pool-1',
      creatorId: 'user-1',
      creatorNickname: 'Host',
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      scheduledTime: null,
      skillLevel: 'OPEN_FOR_ALL',
      status: 'OPEN',
      requiredPlayers: 2,
      currentPlayers: 1,
      participants: [],
      createdAt: '2026-08-28T19:00:00Z',
    });

    const wrapper = mount(CreatePoolModal, {
      props: {
        isOpen: true,
      },
      global: {
        stubs: {
          teleport: true,
        },
      },
    });

    await wrapper.find('[data-test="submit-pool-btn"]').trigger('click');

    expect(poolStore.createPool).toHaveBeenCalledWith({
      matchType: 'ONE_VS_ONE',
      startCondition: 'FILL_BASED',
      skillLevel: 'OPEN_FOR_ALL',
      scheduledTime: null,
    });
    expect(wrapper.emitted('close')).toBeTruthy();
  });
});
