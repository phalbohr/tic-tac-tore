import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PlayerGroupModal from '@/features/group/components/PlayerGroupModal.vue'

/**
 * ATDD Red-Phase Scaffolds for PlayerGroupModal component.
 * Story 6.1: Named Player Groups ("Teams")
 *
 * AC 1: Create/edit named player group (1-50 chars, Favorites flag, member selection)
 * AC 3: Inline modal integration without losing active match draft state
 * UX: Clubhouse No-Line styling (UX-DR3)
 */
describe('PlayerGroupModal.vue (ATDD Red Phase)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders modal with correct title and fields in create mode', () => {
    const wrapper = mount(PlayerGroupModal, {
      props: {
        modelValue: true,
        group: null,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(wrapper.text()).toContain('Create Group')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="checkbox"]').exists()).toBe(true)
  })

  it('populates fields when editing an existing group', () => {
    const existingGroup = {
      id: 'grp-1',
      name: 'Friday Foosball',
      isFavorite: true,
      creatorId: 'user-1',
      members: [{ id: 'user-2', nickname: 'Alice', avatar: 'avatar-1' }],
      createdAt: '2026-08-23T10:00:00Z',
    }

    const wrapper = mount(PlayerGroupModal, {
      props: {
        modelValue: true,
        group: existingGroup,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const nameInput = wrapper.find('input[type="text"]').element as HTMLInputElement
    expect(nameInput.value).toBe('Friday Foosball')
    expect(wrapper.text()).toContain('Alice')
  })

  it('emits save event with trimmed name and member IDs when submitted', async () => {
    const wrapper = mount(PlayerGroupModal, {
      props: {
        modelValue: true,
        group: null,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const nameInput = wrapper.find('input[type="text"]')
    await nameInput.setValue('  Weekend Squad  ')

    const submitBtn = wrapper.find('button[type="submit"], [data-testid="group-save-btn"]')
    await submitBtn.trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()
    const payload = wrapper.emitted('save')?.[0]?.[0] as { name: string; isFavorite: boolean; memberIds: string[] }
    expect(payload.name).toBe('Weekend Squad')
  })

  it('emits update:modelValue with false when cancel button is clicked', async () => {
    const wrapper = mount(PlayerGroupModal, {
      props: {
        modelValue: true,
        group: null,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const cancelBtn = wrapper.find('[data-testid="group-cancel-btn"], button:has-text("Cancel")')
    await cancelBtn.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })
})
