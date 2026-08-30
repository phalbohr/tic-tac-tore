import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChallengeModal from '@/features/challenge/components/ChallengeModal.vue'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  }
})

vi.mock('@/services/ruleConfigService', () => ({
  getRuleConfigurations: vi.fn().mockResolvedValue([
    { id: 'rule-1', name: 'Custom Rules', goalLimit: 5 },
    { id: 'rule-2', name: 'Tournament Rules', goalLimit: 10 },
  ]),
}))

describe('ChallengeModal.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders modal with target player information and default 1v1', () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        modelValue: true,
        targetPlayer: { id: 'player-1', nickname: 'Bob', avatar: 'avatar-1' },
      },
      global: {
        stubs: {
          Teleport: true,
          AvatarBase: true,
        },
      },
    })

    expect(wrapper.find('[data-testid="target-player-name"]').text()).toBe('Bob')
    expect(wrapper.find('[data-testid="match-type-1v1"]').classes()).toContain('bg-primary')
  })

  it('renders modal with target group information and default 2v2', async () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        modelValue: true,
        targetGroup: { id: 'group-1', name: 'Champions' },
      },
      global: {
        stubs: {
          Teleport: true,
          AvatarBase: true,
        },
      },
    })
    await flushPromises()

    expect(wrapper.find('[data-testid="target-group-name"]').text()).toBe('Champions')
    expect(wrapper.find('[data-testid="match-type-2v2"]').classes()).toContain('bg-primary')
  })

  it('switches match format when clicking 1v1 or 2v2', async () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        modelValue: true,
        targetPlayer: { id: 'player-1', nickname: 'Bob' },
      },
      global: {
        stubs: {
          Teleport: true,
          AvatarBase: true,
        },
      },
    })
    await flushPromises()

    await wrapper.find('[data-testid="match-type-2v2"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="match-type-2v2"]').classes()).toContain('bg-primary')
    expect(wrapper.find('[data-testid="match-type-1v1"]').classes()).not.toContain('bg-primary')
  })

  it('submits challenge successfully and emits challengeSent', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 'challenge-123',
        challengerId: 'me',
        targetPlayerId: 'player-1',
        matchType: 'ONE_VS_ONE',
        status: 'PENDING',
      }),
    })
    globalThis.fetch = fetchMock as unknown as typeof fetch

    const wrapper = mount(ChallengeModal, {
      props: {
        modelValue: true,
        targetPlayer: { id: 'player-1', nickname: 'Bob' },
      },
      global: {
        stubs: {
          Teleport: true,
          AvatarBase: true,
        },
      },
    })
    await flushPromises()

    const messageInput = wrapper.find('[data-testid="challenge-message-input"]')
    await messageInput.setValue('Ready for a rematch?')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith('/api/v1/challenges', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({
        targetPlayerId: 'player-1',
        matchType: 'ONE_VS_ONE',
        message: 'Ready for a rematch?',
      }),
    }))

    expect(wrapper.emitted('challengeSent')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('closes modal when cancel button is clicked', async () => {
    const wrapper = mount(ChallengeModal, {
      props: {
        modelValue: true,
        targetPlayer: { id: 'player-1', nickname: 'Bob' },
      },
      global: {
        stubs: {
          Teleport: true,
          AvatarBase: true,
        },
      },
    })

    await wrapper.find('[data-testid="challenge-cancel-btn"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })
})
