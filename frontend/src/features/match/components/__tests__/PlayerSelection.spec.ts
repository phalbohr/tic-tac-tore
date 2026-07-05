import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PlayerSelection from '../PlayerSelection.vue'
import { useMatchDraftStore } from '../../stores/matchDraftStore'

describe('PlayerSelection.vue', () => {
  it('renders player slots based on match type', () => {
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [createTestingPinia()]
      }
    })
    const store = useMatchDraftStore()
    expect(wrapper.findAll('.player-slot')).toHaveLength(2)
  })
})
