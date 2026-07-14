import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PlayerSelection from '../PlayerSelection.vue'

describe('PlayerSelection.vue', () => {
  it('renders player slots based on match type', () => {
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
      }
    })

    expect(wrapper.findAll('.player-slot')).toHaveLength(2)
  })
})
